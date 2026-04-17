package com.vrijgeld.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.ImportSource
import com.vrijgeld.data.model.Transaction
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val accountRepo: AccountRepository,
) : ViewModel() {

    val categories = budgetRepo.getExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Category>())

    val accounts = accountRepo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Account>())

    fun save(
        accountId: Long,
        categoryId: Long?,
        amountCents: Long,
        description: String,
        date: Long,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        transactionRepo.insert(
            Transaction(
                accountId    = accountId,
                categoryId   = categoryId,
                amount       = amountCents,
                date         = date,
                description  = description,
                importSource = ImportSource.MANUAL,
                isReviewed   = true
            )
        )
        onDone()
    }
}
