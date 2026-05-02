package com.vrijgeld.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.DetectedSubscription
import com.vrijgeld.data.model.MonthlyAllocation
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.SubscriptionRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.EnvelopeBudgetEngine
import com.vrijgeld.domain.EnvelopeState
import com.vrijgeld.domain.SpendingInsight
import com.vrijgeld.domain.SpendingInsightsEngine
import com.vrijgeld.domain.annualCost
import com.vrijgeld.domain.currentBudgetPeriod
import com.vrijgeld.domain.monthlyCost
import com.vrijgeld.domain.nextFutureOccurrence
import com.vrijgeld.domain.previousBudgetPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val regularEnvelopes: List<EnvelopeState>     = emptyList(),
    val sinkingEnvelopes: List<EnvelopeState>     = emptyList(),
    val unallocatedIncome: Long                   = 0L,
    val totalIncome: Long                         = 0L,
    val overspendTarget: EnvelopeState?           = null,
    val subscriptions: List<DetectedSubscription> = emptyList(),
    val forecast: List<ForecastItem>              = emptyList(),
    val totalMonthlySubCost: Long                 = 0L,
    val lastMonthSpentByCategory: Map<Long, Long> = emptyMap(),
    val insights: List<SpendingInsight>           = emptyList(),
)

data class ForecastItem(
    val name: String,
    val amountCents: Long,
    val expectedDate: Long
) {
    val daysUntil: Int
        get() = ((expectedDate - System.currentTimeMillis()) / 86_400_000L).toInt().coerceAtLeast(0)
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val subscriptionRepo: SubscriptionRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState = _uiState.asStateFlow()

    private val period        = currentBudgetPeriod()
    val yearMonth: String     = period.yearMonth
    private val lastPeriod    = previousBudgetPeriod(period)

    fun confirm(id: Long) = viewModelScope.launch { subscriptionRepo.confirm(id) }
    fun dismiss(id: Long) = viewModelScope.launch { subscriptionRepo.dismiss(id) }
    fun delete(id: Long)  = viewModelScope.launch { subscriptionRepo.delete(id) }
    fun setOverspendTarget(env: EnvelopeState?) { _uiState.value = _uiState.value.copy(overspendTarget = env) }

    fun transferAllocation(sourceId: Long, targetId: Long) = viewModelScope.launch {
        val target    = _uiState.value.overspendTarget ?: return@launch
        val amount    = target.overspendAmount
        val current   = budgetRepo.getAllocationsForMonthOnce(yearMonth).toMutableList()
        val updated   = EnvelopeBudgetEngine.transferAllocation(current, sourceId, targetId, amount, yearMonth)
        budgetRepo.upsertAllAllocations(updated)
        _uiState.value = _uiState.value.copy(overspendTarget = null)
    }

    init {
        viewModelScope.launch {
            combine(
                transactionRepo.getByDateRange(period.startMs, period.endMs),
                budgetRepo.getAllocationsForMonth(yearMonth),
                budgetRepo.getExpenseCategories()
            ) { txs, allocations, cats ->
                val spent = txs.filter { it.amount < 0 && it.categoryId != null }
                    .groupBy { it.categoryId!! }
                    .mapValues { (_, ts) -> ts.sumOf { -it.amount } }

                val totalIncome     = settingsRepo.getMonthlyIncome()
                val holding         = settingsRepo.getVakantiegeldHoldingCents()
                val effectiveIncome = totalIncome - holding

                val regular  = cats.filter { !it.isSinkingFund }
                val sinking  = cats.filter { it.isSinkingFund }

                val regularEnv = EnvelopeBudgetEngine.build(regular, allocations, spent)
                    .sortedByDescending { it.percentUsed }
                val sinkingEnv = EnvelopeBudgetEngine.build(sinking, allocations, spent)

                val unallocated = EnvelopeBudgetEngine.unallocated(effectiveIncome,
                    regularEnv + sinkingEnv)

                Triple(regularEnv to sinkingEnv, unallocated, effectiveIncome)
            }.collect { (envelopes, unallocated, income) ->
                val (regular, sinking) = envelopes
                _uiState.value = _uiState.value.copy(
                    regularEnvelopes  = regular,
                    sinkingEnvelopes  = sinking,
                    unallocatedIncome = unallocated,
                    totalIncome       = income
                )
            }
        }

        // Last-month spending + spending insights
        viewModelScope.launch {
            val lastMonthTxs = transactionRepo.getByDateRangeOnce(lastPeriod.startMs, lastPeriod.endMs)
            val lastMonthSpent = lastMonthTxs.filter { it.amount < 0 && it.categoryId != null }
                .groupBy { it.categoryId!! }
                .mapValues { (_, ts) -> ts.sumOf { -it.amount } }

            // Collect insights from 6 months of data
            val sixMonthsAgo = System.currentTimeMillis() - 180L * 86_400_000L
            val allRecentTxs = transactionRepo.getRecentExpensesSince(sixMonthsAgo)

            val insights = listOfNotNull(
                SpendingInsightsEngine.lifestyleInflationAlert(allRecentTxs, yearMonth),
                SpendingInsightsEngine.smallPurchasesAlert(allRecentTxs, yearMonth),
            )

            _uiState.value = _uiState.value.copy(
                lastMonthSpentByCategory = lastMonthSpent,
                insights                 = insights
            )
        }

        viewModelScope.launch {
            subscriptionRepo.getAll().collect { subs ->
                _uiState.value = _uiState.value.copy(
                    subscriptions       = subs.sortedByDescending { it.annualCost() },
                    totalMonthlySubCost = subs.sumOf { it.monthlyCost() }
                )
            }
        }

        viewModelScope.launch {
            val now    = System.currentTimeMillis()
            val cutoff = now + 30L * 86_400_000L

            combine(
                subscriptionRepo.getAll(),
                budgetRepo.getExpenseCategories()
            ) { subs, _ ->
                val items = mutableListOf<ForecastItem>()

                subs.filter { it.nextExpectedDate in now..cutoff }
                    .forEach { items += ForecastItem(it.merchantName, it.estimatedAmount, it.nextExpectedDate) }

                transactionRepo.getRecurringOnce().let { recurring ->
                    recurring.groupBy { it.description }.values.forEach { group ->
                        val latest = group.maxByOrNull { it.date } ?: return@forEach
                        val freq   = latest.recurrenceFrequency ?: return@forEach
                        val next   = nextFutureOccurrence(latest.date, freq)
                        if (next in now..cutoff) {
                            items += ForecastItem(latest.description, -latest.amount, next)
                        }
                    }
                }

                items.sortedBy { it.expectedDate }
            }.collect { _uiState.value = _uiState.value.copy(forecast = it) }
        }
    }
}
