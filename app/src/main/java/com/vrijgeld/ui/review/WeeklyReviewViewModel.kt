package com.vrijgeld.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.EnvelopeBudgetEngine
import com.vrijgeld.domain.previousYearMonths
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TopCategoryItem(val icon: String, val name: String, val spentCents: Long, val budgetCents: Long)
data class AnomalyItem(val description: String, val amountCents: Long, val normalCents: Long)

data class WeeklyReviewUiState(
    val topCategories: List<TopCategoryItem> = emptyList(),
    val budgetPacePercent: Float             = 0f,
    val daysIntoMonth: Int                   = 0,
    val anomalies: List<AnomalyItem>         = emptyList(),
    val savingsRateThisMonth: Float          = 0f,
    val savingsRateLastMonth: Float          = 0f,
    val reflectionNote: String               = "",
    val isLoading: Boolean                   = true,
)

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyReviewUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        val cal       = Calendar.getInstance()
        val yearMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val lastYM    = previousYearMonths(yearMonth, 1).first()
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val txs       = transactionRepo.getForMonthOnce(yearMonth)
        val lastTxs   = transactionRepo.getForMonthOnce(lastYM)
        val allocs    = budgetRepo.getAllocationsForMonthOnce(yearMonth)
        val cats      = budgetRepo.getExpenseCategoriesOnce()

        val spentByCat = txs.filter { it.amount < 0 && it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { (_, ts) -> ts.sumOf { -it.amount } }

        val envelopes = EnvelopeBudgetEngine.build(cats, allocs, spentByCat)

        // Top 3 categories by spend
        val top3 = envelopes
            .filter { it.spent > 0 }
            .sortedByDescending { it.spent }
            .take(3)
            .map { env ->
                TopCategoryItem(
                    icon       = env.category.icon,
                    name       = env.category.name,
                    spentCents = env.spent,
                    budgetCents = env.available
                )
            }

        // Budget pace: expected pct used = dayOfMonth/daysInMonth
        val totalBudget = envelopes.sumOf { it.available }
        val totalSpent  = envelopes.sumOf { it.spent }
        val pacePct     = if (totalBudget > 0) (totalSpent.toFloat() / totalBudget) else 0f

        // Simple anomalies: envelopes >2x average daily pace by this point
        val expectedPct = dayOfMonth.toFloat() / daysInMonth
        val anomalies = envelopes
            .filter { it.available > 0 && (it.spent.toFloat() / it.available) > expectedPct * 2 }
            .map { env ->
                AnomalyItem(
                    description = "${env.category.icon} ${env.category.name}",
                    amountCents = env.spent,
                    normalCents = (env.available * expectedPct).toLong()
                )
            }

        // Savings rates
        fun savingsRate(txList: List<com.vrijgeld.data.model.Transaction>): Float {
            val income   = txList.filter { it.amount > 0 }.sumOf { it.amount }
            val expenses = txList.filter { it.amount < 0 }.sumOf { -it.amount }
            return if (income > 0) ((income - expenses).toFloat() / income).coerceIn(-1f, 1f) else 0f
        }

        _uiState.value = WeeklyReviewUiState(
            topCategories          = top3,
            budgetPacePercent      = pacePct,
            daysIntoMonth          = dayOfMonth,
            anomalies              = anomalies,
            savingsRateThisMonth   = savingsRate(txs),
            savingsRateLastMonth   = savingsRate(lastTxs),
            reflectionNote         = _uiState.value.reflectionNote,
            isLoading              = false
        )
    }

    fun setReflectionNote(text: String) {
        _uiState.value = _uiState.value.copy(reflectionNote = text)
    }
}
