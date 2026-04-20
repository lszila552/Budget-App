package com.vrijgeld.ui.wealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.AccountType
import com.vrijgeld.data.model.NetWorthSnapshot
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.NetWorthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NetWorthUiState(
    val snapshots: List<NetWorthSnapshot>                      = emptyList(),
    val assetAccounts: Map<AccountType, List<Account>>         = emptyMap(),
    val liabilityAccounts: List<Account>                       = emptyList(),
    val latestNetWorth: Long                                   = 0L,
    val previousNetWorth: Long                                 = 0L,
    val expandedTypes: Set<AccountType>                        = AccountType.entries.toSet()
) {
    val monthlyDelta: Long get() = latestNetWorth - previousNetWorth
}

@HiltViewModel
class NetWorthViewModel @Inject constructor(
    private val netWorthRepo: NetWorthRepository,
    private val accountRepo: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetWorthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                netWorthRepo.getAll(),
                accountRepo.getActive()
            ) { snapshots, accounts ->
                val active = accounts.filter { it.includeInNetWorth }

                val assetTypes    = listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.INVESTMENT, AccountType.PROPERTY)
                val assetAccounts = assetTypes
                    .associateWith { type -> active.filter { it.type == type } }
                    .filterValues { it.isNotEmpty() }

                val liabilities = active.filter { it.type == AccountType.LIABILITY }

                // Live net worth: computed from current account balances so it updates
                // immediately when a transaction is added or an import runs.
                val liveAssets   = active.filter { it.type != AccountType.LIABILITY }.sumOf { it.currentBalance }
                val liveLiabs    = liabilities.sumOf { it.currentBalance }
                val liveNetWorth = liveAssets - liveLiabs

                // Previous month from snapshots for the monthly delta badge
                val previous = snapshots.firstOrNull()?.netWorth ?: 0L

                _uiState.value = _uiState.value.copy(
                    snapshots         = snapshots,
                    assetAccounts     = assetAccounts,
                    liabilityAccounts = liabilities,
                    latestNetWorth    = liveNetWorth,
                    previousNetWorth  = previous
                )
            }.collect {}
        }
    }

    fun toggleType(type: AccountType) {
        val current = _uiState.value.expandedTypes
        _uiState.value = _uiState.value.copy(
            expandedTypes = if (type in current) current - type else current + type
        )
    }
}
