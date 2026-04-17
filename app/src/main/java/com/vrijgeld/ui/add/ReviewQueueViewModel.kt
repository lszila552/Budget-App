package com.vrijgeld.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.Transaction
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val current: Transaction? = null,
    val remaining: Int        = 0,
    val done: Boolean         = false
)

@HiltViewModel
class ReviewQueueViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
) : ViewModel() {

    private val _queue = MutableStateFlow<List<Transaction>>(emptyList())
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    val categories = budgetRepo.getExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Category>())

    init {
        viewModelScope.launch {
            transactionRepo.getUncategorizedImported().collect { txs ->
                _queue.value = txs
                _uiState.value = ReviewUiState(
                    current   = txs.firstOrNull(),
                    remaining = txs.size,
                    done      = txs.isEmpty()
                )
            }
        }
    }

    fun assign(category: Category) = viewModelScope.launch {
        val tx = _uiState.value.current ?: return@launch
        transactionRepo.update(tx.copy(categoryId = category.id, isReviewed = true))
    }

    fun skip() = viewModelScope.launch {
        val tx = _uiState.value.current ?: return@launch
        transactionRepo.update(tx.copy(isReviewed = true))
    }
}
