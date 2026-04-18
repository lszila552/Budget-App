package com.vrijgeld.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.`import`.CategorizationEngine
import com.vrijgeld.data.`import`.Camt053Parser
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.ImportSource
import com.vrijgeld.data.model.Transaction
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.KEY_NOTIF_BILL_LOW_BALANCE
import com.vrijgeld.data.repository.KEY_NOTIF_SUBSCRIPTION_RENEWAL
import com.vrijgeld.data.repository.KEY_NOTIF_UNUSUAL_TX
import com.vrijgeld.data.repository.KEY_NOTIF_WEEKLY_PACE
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val count: Int) : ImportState()
    data class Error(val message: String) : ImportState()
}

data class NotifPrefs(
    val weeklyPace:          Boolean = true,
    val billLowBalance:      Boolean = true,
    val unusualTx:           Boolean = true,
    val subscriptionRenewal: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val budgetRepo: BudgetRepository,
    private val engine: CategorizationEngine,
) : ViewModel() {

    val accounts = accountRepo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Account>())

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    private val _notifPrefs = MutableStateFlow(NotifPrefs())
    val notifPrefs = _notifPrefs.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            _categories.value = budgetRepo.getExpenseCategoriesOnce()
        }
        viewModelScope.launch {
            _notifPrefs.value = NotifPrefs(
                weeklyPace          = settingsRepo.getNotifWeeklyPace(),
                billLowBalance      = settingsRepo.getNotifBillLowBalance(),
                unusualTx           = settingsRepo.getNotifUnusualTx(),
                subscriptionRenewal = settingsRepo.getNotifSubscriptionRenewal()
            )
        }
    }

    fun setNotifPref(key: String, enabled: Boolean) = viewModelScope.launch {
        settingsRepo.set(key, if (enabled) "true" else "false")
        _notifPrefs.value = NotifPrefs(
            weeklyPace          = settingsRepo.getNotifWeeklyPace(),
            billLowBalance      = settingsRepo.getNotifBillLowBalance(),
            unusualTx           = settingsRepo.getNotifUnusualTx(),
            subscriptionRenewal = settingsRepo.getNotifSubscriptionRenewal()
        )
    }

    fun importCamt053(uri: Uri, context: Context, accountId: Long) = viewModelScope.launch {
        _importState.value = ImportState.Loading
        runCatching {
            val parsed = context.contentResolver.openInputStream(uri)
                ?.use { Camt053Parser().parse(it) } ?: emptyList()

            var count = 0
            parsed.forEach { p ->
                if (transactionRepo.getByImportHash(p.importHash) != null) return@forEach
                val stub = Transaction(accountId = accountId, amount = p.amountCents,
                    date = p.dateMillis, description = p.description,
                    merchantName = p.merchantName, counterpartyIban = p.counterpartyIban)
                val catId = engine.categorize(stub)
                transactionRepo.insert(stub.copy(categoryId = catId,
                    importSource = ImportSource.CAMT053, importHash = p.importHash))
                count++
            }
            _importState.value = ImportState.Success(count)
        }.onFailure { _importState.value = ImportState.Error(it.message ?: "Import failed") }
    }

    fun resetImportState() { _importState.value = ImportState.Idle }

    fun updateAccountBalance(accountId: Long, amountText: String) = viewModelScope.launch {
        val cents = amountText.toDoubleOrNull()?.times(100)?.toLong() ?: return@launch
        accountRepo.updateBalance(accountId, cents)
    }

    fun updateCategoryBudget(category: Category, amountText: String) = viewModelScope.launch {
        val cents = amountText.toDoubleOrNull()?.times(100)?.toLong() ?: return@launch
        val updated = category.copy(monthlyBudget = cents)
        budgetRepo.updateCategory(updated)
        _categories.value = budgetRepo.getExpenseCategoriesOnce()
    }

    fun updateSinkingFundTarget(category: Category, amountText: String) = viewModelScope.launch {
        val cents = amountText.toDoubleOrNull()?.times(100)?.toLong() ?: return@launch
        val updated = category.copy(sinkingFundTarget = cents)
        budgetRepo.updateCategory(updated)
        _categories.value = budgetRepo.getExpenseCategoriesOnce()
    }
}
