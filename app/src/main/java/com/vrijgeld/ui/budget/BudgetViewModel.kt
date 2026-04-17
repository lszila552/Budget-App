package com.vrijgeld.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.ui.components.CategoryRingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetUiState(val items: List<CategorySpendingItem> = emptyList())

data class CategorySpendingItem(val category: Category, val spent: Long) {
    val ringData get() = CategoryRingData(category.name, category.icon, spent, category.monthlyBudget ?: 0L)
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState = _uiState.asStateFlow()

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
                BudgetUiState(cats.map { CategorySpendingItem(it, byCategory[it.id] ?: 0L) })
            }.collect { _uiState.value = it }
        }
    }
}
