package com.vrijgeld.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.MonthlyAllocation
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AllocationDraft(
    val categoryId: Long,
    val name: String,
    val icon: String,
    val isSinkingFund: Boolean,
    val defaultBudget: Long,
    var amountText: String = ""
)

data class AllocationWorkflowState(
    val totalIncome: Long                = 0L,
    val drafts: List<AllocationDraft>    = emptyList(),
    val applied: Boolean                 = false,
    val yearMonth: String                = ""
) {
    val totalAllocated: Long get() = drafts.sumOf { it.amountText.toDoubleOrNull()?.times(100)?.toLong() ?: 0L }
    val unallocated: Long    get() = totalIncome - totalAllocated
}

@HiltViewModel
class AllocationViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository,
    private val transactionRepo: TransactionRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AllocationWorkflowState())
    val state = _state.asStateFlow()

    private val cal       = Calendar.getInstance()
    private val yearMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    private val prevCal   = Calendar.getInstance().also { it.add(Calendar.MONTH, -1) }
    private val lastMonth = "%04d-%02d".format(prevCal.get(Calendar.YEAR), prevCal.get(Calendar.MONTH) + 1)

    init { load() }

    private fun load() = viewModelScope.launch {
        val txs      = transactionRepo.getForMonthOnce(yearMonth)
        val holding  = settingsRepo.getVakantiegeldHoldingCents()

        val rawIncome  = txs.filter { it.amount > 0 }.sumOf { it.amount }
        val settingsIncome = settingsRepo.getMonthlyIncome()
        val totalIncome = (if (rawIncome > 0L) rawIncome else settingsIncome) - holding

        val cats     = budgetRepo.getExpenseCategoriesOnce()
        val lastAlloc = budgetRepo.getAllocationsForMonthOnce(lastMonth).associateBy { it.categoryId }
        val thisAlloc = budgetRepo.getAllocationsForMonthOnce(yearMonth).associateBy { it.categoryId }

        val drafts = cats.map { cat ->
            val existing    = thisAlloc[cat.id]
            val lastAmount  = lastAlloc[cat.id]?.allocated
            val defaultAmt  = existing?.allocated ?: lastAmount ?: cat.monthlyBudget ?: 0L
            AllocationDraft(
                categoryId   = cat.id,
                name         = cat.name,
                icon         = cat.icon,
                isSinkingFund = cat.isSinkingFund,
                defaultBudget = cat.monthlyBudget ?: 0L,
                amountText   = if (defaultAmt > 0) "%.2f".format(defaultAmt / 100.0) else ""
            )
        }

        _state.value = AllocationWorkflowState(
            totalIncome = totalIncome,
            drafts      = drafts,
            yearMonth   = yearMonth
        )
    }

    fun updateDraft(categoryId: Long, text: String) {
        val updated = _state.value.drafts.map {
            if (it.categoryId == categoryId) it.copy(amountText = text) else it
        }
        _state.value = _state.value.copy(drafts = updated)
    }

    fun apply() = viewModelScope.launch {
        val allocations = _state.value.drafts.mapNotNull { draft ->
            val cents = draft.amountText.toDoubleOrNull()?.times(100)?.toLong() ?: return@mapNotNull null
            if (cents == 0L && !draft.isSinkingFund) return@mapNotNull null
            MonthlyAllocation(
                categoryId  = draft.categoryId,
                yearMonth   = yearMonth,
                allocated   = cents
            )
        }
        budgetRepo.upsertAllAllocations(allocations)
        _state.value = _state.value.copy(applied = true)
    }
}
