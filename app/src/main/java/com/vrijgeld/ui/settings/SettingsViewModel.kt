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
import com.vrijgeld.data.repository.AccountRepository
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val engine: CategorizationEngine,
) : ViewModel() {

    val accounts = accountRepo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Account>())

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

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
}
