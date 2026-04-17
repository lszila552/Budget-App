package com.vrijgeld.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.DetectedSubscription
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.SubscriptionRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.annualCost
import com.vrijgeld.domain.monthlyCost
import com.vrijgeld.domain.nextFutureOccurrence
import com.vrijgeld.ui.components.CategoryRingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetUiState(
    val items: List<CategorySpendingItem>         = emptyList(),
    val subscriptions: List<DetectedSubscription> = emptyList(),
    val forecast: List<ForecastItem>              = emptyList(),
    val totalMonthlySubCost: Long                 = 0L
)

data class CategorySpendingItem(val category: Category, val spent: Long) {
    val ringData get() = CategoryRingData(category.name, category.icon, spent, category.monthlyBudget ?: 0L)
}

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
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState = _uiState.asStateFlow()

    fun confirm(id: Long) = viewModelScope.launch { subscriptionRepo.confirm(id) }
    fun dismiss(id: Long) = viewModelScope.launch { subscriptionRepo.dismiss(id) }

    init {
        val cal       = Calendar.getInstance()
        val yearMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

        viewModelScope.launch {
            combine(
                transactionRepo.getForMonth(yearMonth),
                budgetRepo.getExpenseCategories()
            ) { transactions, cats ->
                val byCategory = transactions.filter { it.amount < 0 }
                    .groupBy { it.categoryId }
                    .mapValues { (_, ts) -> ts.sumOf { -it.amount } }
                cats.map { CategorySpendingItem(it, byCategory[it.id] ?: 0L) }
            }.collect { _uiState.value = _uiState.value.copy(items = it) }
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
            val now     = System.currentTimeMillis()
            val cutoff  = now + 30L * 86_400_000L

            combine(
                subscriptionRepo.getAll(),
                budgetRepo.getExpenseCategories()   // just a signal to trigger recompute
            ) { subs, _ ->
                val items = mutableListOf<ForecastItem>()

                subs.filter { it.nextExpectedDate in now..cutoff }
                    .forEach { items += ForecastItem(it.merchantName, it.estimatedAmount, it.nextExpectedDate) }

                transactionRepo.getRecurringOnce().let { recurring ->
                    recurring.groupBy { it.description }
                        .values
                        .forEach { group ->
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
