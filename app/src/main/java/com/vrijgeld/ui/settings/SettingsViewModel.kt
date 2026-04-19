package com.vrijgeld.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.importer.Camt053Parser
import com.vrijgeld.data.importer.CategorizationEngine
import com.vrijgeld.data.importer.CsvParser
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.ImportSource
import com.vrijgeld.data.model.MonthlyAllocation
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
import com.vrijgeld.ui.theme.AppPreferences
import com.vrijgeld.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val count: Int, val categorized: Int = 0) : ImportState()
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
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val appPrefs = AppPreferences(appContext)

    val accounts = accountRepo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Account>())

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    private val _notifPrefs = MutableStateFlow(NotifPrefs())
    val notifPrefs = _notifPrefs.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _theme = MutableStateFlow(appPrefs.theme)
    val theme = _theme.asStateFlow()

    private val _accentIndex = MutableStateFlow(appPrefs.accentIndex)
    val accentIndex = _accentIndex.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(appPrefs.biometricEnabled)
    val biometricEnabled = _biometricEnabled.asStateFlow()

    private val _exportState = MutableStateFlow<ImportState>(ImportState.Idle)
    val exportState = _exportState.asStateFlow()

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

    fun setTheme(theme: AppTheme) {
        appPrefs.theme = theme
        _theme.value   = theme
    }

    fun setAccent(index: Int) {
        appPrefs.accentIndex = index
        _accentIndex.value   = index
    }

    fun setBiometric(enabled: Boolean) {
        appPrefs.biometricEnabled = enabled
        _biometricEnabled.value   = enabled
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

    fun importFile(uri: Uri, context: Context, fallbackAccountId: Long) = viewModelScope.launch {
        _importState.value = ImportState.Loading
        runCatching {
            val allAccounts = accountRepo.getActiveOnce()
            val isCsv = isCsvUri(uri, context)

            val parsed = if (isCsv) {
                val lines = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.readLines() ?: emptyList()
                }
                CsvParser().parse(lines)
            } else {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)
                        ?.use { Camt053Parser().parse(it) } ?: emptyList()
                }
            }

            var count = 0
            var categorized = 0
            parsed.forEach { p ->
                if (transactionRepo.getByImportHash(p.importHash) != null) return@forEach
                val accountId = p.ownIban
                    ?.let { iban -> allAccounts.find { it.iban == iban }?.id }
                    ?: fallbackAccountId
                val stub = Transaction(
                    accountId        = accountId,
                    amount           = p.amountCents,
                    date             = p.dateMillis,
                    description      = p.description,
                    merchantName     = p.merchantName,
                    counterpartyIban = p.counterpartyIban
                )
                val catId = engine.categorize(stub)
                if (catId != null) categorized++
                transactionRepo.insert(stub.copy(
                    categoryId   = catId,
                    importSource = p.importSource,
                    importHash   = p.importHash
                ))
                count++
            }
            _importState.value = ImportState.Success(count, categorized)
        }.onFailure { _importState.value = ImportState.Error(it.message ?: "Import failed") }
    }

    private fun isCsvUri(uri: Uri, context: Context): Boolean {
        val mime    = context.contentResolver.getType(uri) ?: ""
        val segment = uri.lastPathSegment?.lowercase() ?: uri.toString().lowercase()
        return mime.contains("csv") || segment.endsWith(".csv")
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

    fun exportJson(context: Context, uri: Uri) = viewModelScope.launch {
        _exportState.value = ImportState.Loading
        runCatching {
            val txs    = transactionRepo.getAllOnce()
            val allocs = budgetRepo.getAllAllocationsOnce()

            val json = JSONObject().apply {
                put("version", 1)
                put("transactions", JSONArray(txs.map { it.toJson() }))
                put("allocations",  JSONArray(allocs.map { it.toJson() }))
            }

            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(json.toString(2).toByteArray())
                }
            }
            _exportState.value = ImportState.Success(txs.size)
        }.onFailure { _exportState.value = ImportState.Error(it.message ?: "Export failed") }
    }

    fun importJson(context: Context, uri: Uri) = viewModelScope.launch {
        _exportState.value = ImportState.Loading
        runCatching {
            val raw = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Cannot open file")
            }
            val json   = JSONObject(String(raw))
            val txArr  = json.getJSONArray("transactions")
            val alcArr = json.getJSONArray("allocations")

            val txs = (0 until txArr.length()).map { transactionFromJson(txArr.getJSONObject(it)) }
            transactionRepo.insertAll(txs)

            val alcs = (0 until alcArr.length()).map { allocationFromJson(alcArr.getJSONObject(it)) }
            budgetRepo.upsertAllAllocations(alcs)

            _exportState.value = ImportState.Success(txs.size)
        }.onFailure { _exportState.value = ImportState.Error(it.message ?: "Import failed") }
    }

    fun resetExportState() { _exportState.value = ImportState.Idle }
}

private fun Transaction.toJson() = JSONObject().apply {
    put("id",          id)
    put("accountId",   accountId)
    put("categoryId",  categoryId)
    put("amount",      amount)
    put("date",        date)
    put("description", description)
    put("merchantName",       merchantName)
    put("counterpartyIban",   counterpartyIban)
    put("isRecurring",        isRecurring)
    put("recurrenceFrequency",recurrenceFrequency?.name)
    put("importSource",       importSource?.name)
    put("importHash",         importHash)
    put("isReviewed",         isReviewed)
    put("note",               note)
}

private fun transactionFromJson(o: JSONObject) = Transaction(
    accountId          = o.getLong("accountId"),
    categoryId         = if (o.isNull("categoryId")) null else o.getLong("categoryId"),
    amount             = o.getLong("amount"),
    date               = o.getLong("date"),
    description        = o.getString("description"),
    merchantName       = if (o.isNull("merchantName")) null else o.getString("merchantName"),
    counterpartyIban   = if (o.isNull("counterpartyIban")) null else o.getString("counterpartyIban"),
    isRecurring        = o.optBoolean("isRecurring", false),
    importSource       = if (o.isNull("importSource")) null else
        runCatching { ImportSource.valueOf(o.getString("importSource")) }.getOrNull(),
    importHash         = if (o.isNull("importHash")) null else o.getString("importHash"),
    isReviewed         = o.optBoolean("isReviewed", false),
    note               = if (o.isNull("note")) null else o.getString("note"),
)

private fun MonthlyAllocation.toJson() = JSONObject().apply {
    put("categoryId",  categoryId)
    put("yearMonth",   yearMonth)
    put("allocated",   allocated)
    put("carriedOver", carriedOver)
}

private fun allocationFromJson(o: JSONObject) = MonthlyAllocation(
    categoryId  = o.getLong("categoryId"),
    yearMonth   = o.getString("yearMonth"),
    allocated   = o.getLong("allocated"),
    carriedOver = o.optLong("carriedOver", 0L)
)
