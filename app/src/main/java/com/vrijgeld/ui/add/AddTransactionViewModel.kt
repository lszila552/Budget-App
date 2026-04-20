package com.vrijgeld.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.ImportSource
import com.vrijgeld.data.model.RecurrenceFrequency
import com.vrijgeld.data.model.Transaction
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddUiState(
    val amountDisplay: String         = "0",
    val isExpense: Boolean            = true,
    val isRecurring: Boolean          = false,
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val selectedCategoryId: Long?     = null,
    val selectedAccountId: Long?      = null,
    val note: String                  = "",
    val saved: Boolean                = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val accountRepo: AccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState = _uiState.asStateFlow()

    val accounts = accountRepo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Account>())

    private val allExpenseCategories = budgetRepo.getExpenseCategories()
    private val allIncomeCategories  = budgetRepo.getIncomeCategories()
    private val recentCategoryIds    = transactionRepo.getRecentCategoryIds()

    val orderedCategories = combine(
        allExpenseCategories,
        allIncomeCategories,
        recentCategoryIds,
        _uiState
    ) { expCats: List<Category>, incCats: List<Category>, recentIds: List<Long>, state: AddUiState ->
        val pool = if (state.isExpense) expCats else incCats
        val recentIdSet: Set<Long> = recentIds.toHashSet()
        val recent = recentIdSet.mapNotNull { id -> pool.find { cat -> cat.id == id } }
        val rest   = pool.filter { cat -> cat.id !in recentIdSet }
        recent + rest
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Category>())

    fun onKey(key: String) {
        val current = _uiState.value.amountDisplay
        val raw     = if (current == "0") "" else current
        val next    = applyKey(raw, key)
        _uiState.value = _uiState.value.copy(amountDisplay = next.ifEmpty { "0" })
    }

    fun toggleExpense(isExpense: Boolean) {
        _uiState.value = _uiState.value.copy(isExpense = isExpense, selectedCategoryId = null)
    }

    fun toggleRecurring(isRecurring: Boolean) {
        _uiState.value = _uiState.value.copy(isRecurring = isRecurring)
    }

    fun setFrequency(freq: RecurrenceFrequency) {
        _uiState.value = _uiState.value.copy(frequency = freq)
    }

    fun selectCategory(id: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = id)
    }

    fun selectAccount(id: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = id)
    }

    fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun save() = viewModelScope.launch {
        val state    = _uiState.value
        val cents    = (state.amountDisplay.toDoubleOrNull() ?: 0.0).times(100).toLong()
            .let { if (state.isExpense) -it else it }
        val accountId = state.selectedAccountId
            ?: accounts.value.firstOrNull()?.id
            ?: return@launch

        transactionRepo.insert(
            Transaction(
                accountId           = accountId,
                categoryId          = state.selectedCategoryId,
                amount              = cents,
                date                = System.currentTimeMillis(),
                description         = state.note.ifBlank { if (state.isExpense) "Expense" else "Income" },
                isRecurring         = state.isRecurring,
                recurrenceFrequency = if (state.isRecurring) state.frequency else null,
                importSource        = ImportSource.MANUAL,
                isReviewed          = true
            )
        )
        val newBalance = transactionRepo.getAccountBalance(accountId)
        accountRepo.updateBalance(accountId, newBalance)
        _uiState.value = AddUiState(saved = true)
    }

    private fun applyKey(current: String, key: String): String = when (key) {
        "⌫"  -> if (current.isEmpty()) "" else current.dropLast(1)
        "."  -> if ("." in current) current else "$current."
        else -> {
            val newVal = current + key
            if ("." in newVal) {
                val parts = newVal.split(".")
                if (parts[1].length > 2) current else newVal
            } else newVal
        }
    }
}
