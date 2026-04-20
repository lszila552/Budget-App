package com.vrijgeld.ui.wealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.AccountType
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.CategoryType
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SavingsCategoryItem(
    val category: Category,
    val thisMonthAllocation: Long,
    val totalSaved: Long = 0L
)

data class SavingsAccountItem(
    val account: Account,
    val categories: List<SavingsCategoryItem>,
    val expanded: Boolean = true
)

data class SavingsUiState(
    val accountItems: List<SavingsAccountItem> = emptyList(),
    val unlinkedCategories: List<SavingsCategoryItem> = emptyList(),
    val showAddAccountDialog: Boolean = false,
    val showAddCategoryDialog: Boolean = false,
    val addCategoryForAccountId: Long? = null,
    val editCategory: Category? = null,
    val yearMonth: String = ""
)

@HiltViewModel
class SavingsViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val budgetRepo: BudgetRepository
) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val yearMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

    private val _uiState = MutableStateFlow(SavingsUiState(yearMonth = yearMonth))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                accountRepo.getActive(),
                budgetRepo.getSavingsCategories()
            ) { accounts: List<Account>, savingsCats: List<Category> ->
                val allocations = budgetRepo.getAllocationsForMonthOnce(yearMonth)
                    .associateBy { it.categoryId }

                val savingsAccounts = accounts.filter {
                    it.type == AccountType.SAVINGS || it.type == AccountType.INVESTMENT
                }

                val accountItems = savingsAccounts.map { account ->
                    val cats = savingsCats.filter { it.accountId == account.id }
                    SavingsAccountItem(
                        account    = account,
                        categories = cats.map { cat ->
                            SavingsCategoryItem(
                                category             = cat,
                                thisMonthAllocation  = allocations[cat.id]?.allocated ?: 0L,
                                totalSaved           = budgetRepo.getTotalAllocated(cat.id)
                            )
                        },
                        expanded   = _uiState.value.accountItems
                            .find { it.account.id == account.id }?.expanded ?: true
                    )
                }

                val savingsAccountIds = savingsAccounts.map { it.id }.toSet()
                val unlinked = savingsCats
                    .filter { it.accountId == null || it.accountId !in savingsAccountIds }
                    .map { cat ->
                        SavingsCategoryItem(
                            category            = cat,
                            thisMonthAllocation = allocations[cat.id]?.allocated ?: 0L,
                            totalSaved          = budgetRepo.getTotalAllocated(cat.id)
                        )
                    }

                _uiState.value = _uiState.value.copy(
                    accountItems       = accountItems,
                    unlinkedCategories = unlinked
                )
            }.collect {}
        }
    }

    fun toggleAccount(accountId: Long) {
        val updated = _uiState.value.accountItems.map { item ->
            if (item.account.id == accountId) item.copy(expanded = !item.expanded) else item
        }
        _uiState.value = _uiState.value.copy(accountItems = updated)
    }

    fun showAddAccount() {
        _uiState.value = _uiState.value.copy(showAddAccountDialog = true)
    }

    fun dismissAddAccount() {
        _uiState.value = _uiState.value.copy(showAddAccountDialog = false)
    }

    fun addAccount(name: String, type: AccountType, iban: String) = viewModelScope.launch {
        val account = Account(
            name           = name.trim(),
            type           = type,
            currentBalance = 0L,
            iban           = iban.trim().ifEmpty { null },
            sortOrder      = _uiState.value.accountItems.size
        )
        accountRepo.insert(account)
        _uiState.value = _uiState.value.copy(showAddAccountDialog = false)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        accountRepo.delete(account)
    }

    fun showAddCategory(accountId: Long?) {
        _uiState.value = _uiState.value.copy(
            showAddCategoryDialog  = true,
            addCategoryForAccountId = accountId
        )
    }

    fun showEditCategory(category: Category) {
        _uiState.value = _uiState.value.copy(editCategory = category)
    }

    fun dismissCategoryDialog() {
        _uiState.value = _uiState.value.copy(
            showAddCategoryDialog   = false,
            addCategoryForAccountId = null,
            editCategory            = null
        )
    }

    fun saveCategory(name: String, icon: String, monthlyTarget: String, accountId: Long?) =
        viewModelScope.launch {
            val cents = monthlyTarget.toDoubleOrNull()?.times(100)?.toLong()
            val existing = _uiState.value.editCategory
            val category = if (existing != null) {
                existing.copy(
                    name         = name.trim(),
                    icon         = icon.trim().ifEmpty { "💰" },
                    monthlyBudget = cents,
                    accountId    = accountId
                )
            } else {
                val maxSort = budgetRepo.getSavingsCategoriesOnce().maxOfOrNull { it.sortOrder } ?: -1
                Category(
                    name         = name.trim(),
                    icon         = icon.trim().ifEmpty { "💰" },
                    type         = CategoryType.SAVINGS,
                    monthlyBudget = cents,
                    accountId    = accountId,
                    sortOrder    = maxSort + 1
                )
            }
            if (existing != null) budgetRepo.updateCategory(category)
            else budgetRepo.insertCategory(category)
            dismissCategoryDialog()
        }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        budgetRepo.deleteCategory(category)
    }
}
