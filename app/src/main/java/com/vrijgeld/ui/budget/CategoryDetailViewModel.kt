package com.vrijgeld.ui.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.Transaction
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.previousYearMonths
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategoryDetailUiState(
    val category: Category?                    = null,
    val transactions: List<Transaction>        = emptyList(),
    val dailySpend: List<Pair<Int, Long>>      = emptyList(),   // (day, cents)
    val monthlyComparison: List<Pair<String, Long>> = emptyList() // (label, cents)
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val categoryDao: CategoryDao,
) : ViewModel() {

    private val categoryId = savedStateHandle.get<Long>("categoryId")!!

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        val cal        = Calendar.getInstance()
        val yearMonth  = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val prevMonths = previousYearMonths(yearMonth, 3)
        val allMonths  = prevMonths + yearMonth

        val cat = categoryDao.getAllOnce().find { it.id == categoryId }
        _uiState.value = _uiState.value.copy(category = cat)

        // Reactive: update when transactions change
        transactionRepo.getByCategory(categoryId, yearMonth).collectLatest { txs ->
            // Daily spend for sparkline
            val dailyMap = txs.filter { it.amount < 0 }
                .groupBy {
                    val c = Calendar.getInstance().apply { timeInMillis = it.date }
                    c.get(Calendar.DAY_OF_MONTH)
                }
                .mapValues { (_, ts) -> ts.sumOf { -it.amount } }

            val dailySpend = dailyMap.entries
                .sortedBy { it.key }
                .map { it.key to it.value }

            // 3-month comparison (including current)
            val historicalTxs = transactionRepo.getByCategoryYearMonths(categoryId, allMonths)
            val monthlyComparison = allMonths.map { ym ->
                val spent = historicalTxs
                    .filter { tx ->
                        val c = Calendar.getInstance().apply { timeInMillis = tx.date }
                        "%04d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1) == ym
                    }
                    .sumOf { -it.amount }
                ym to spent
            }

            _uiState.value = _uiState.value.copy(
                transactions       = txs,
                dailySpend         = dailySpend,
                monthlyComparison  = monthlyComparison
            )
        }
    }
}
