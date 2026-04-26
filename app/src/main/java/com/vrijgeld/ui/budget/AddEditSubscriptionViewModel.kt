package com.vrijgeld.ui.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.DetectedSubscription
import com.vrijgeld.data.model.RecurrenceFrequency
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.BudgetRepository
import com.vrijgeld.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SubFormState(
    val name: String                   = "",
    val amountText: String             = "",
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val nextDateMillis: Long           = defaultNextDate(),
    val categoryId: Long?              = null,
    val accountId: Long?               = null,
    val saved: Boolean                 = false,
    val deleted: Boolean               = false
)

private fun defaultNextDate(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, 1)
    return cal.timeInMillis
}

@HiltViewModel
class AddEditSubscriptionViewModel @Inject constructor(
    private val subscriptionRepo: SubscriptionRepository,
    private val budgetRepo: BudgetRepository,
    private val accountRepo: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val subscriptionId: Long = savedStateHandle.get<Long>("subscriptionId") ?: -1L
    val isEditing: Boolean   = subscriptionId != -1L

    private val _state = MutableStateFlow(SubFormState())
    val state = _state.asStateFlow()

    val expenseCategories = budgetRepo.getExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Category>())

    val accounts = accountRepo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Account>())

    init {
        if (isEditing) {
            viewModelScope.launch {
                val sub = subscriptionRepo.getById(subscriptionId) ?: return@launch
                _state.value = SubFormState(
                    name          = sub.merchantName,
                    amountText    = "%.2f".format(sub.estimatedAmount / 100.0),
                    frequency     = sub.frequency,
                    nextDateMillis = sub.nextExpectedDate,
                    categoryId    = sub.categoryId,
                    accountId     = sub.accountId
                )
            }
        }
    }

    fun setName(v: String)         { _state.value = _state.value.copy(name = v) }
    fun setAmount(v: String)       { _state.value = _state.value.copy(amountText = v) }
    fun setFrequency(v: RecurrenceFrequency) { _state.value = _state.value.copy(frequency = v) }
    fun setNextDate(v: Long)       { _state.value = _state.value.copy(nextDateMillis = v) }
    fun setCategory(v: Long?)      { _state.value = _state.value.copy(categoryId = v) }
    fun setAccount(v: Long?)       { _state.value = _state.value.copy(accountId = v) }

    fun save() = viewModelScope.launch {
        val s          = _state.value
        val name       = s.name.trim()
        val amountCents = (s.amountText.toDoubleOrNull() ?: return@launch).times(100).toLong()
        if (name.isEmpty() || amountCents <= 0L) return@launch

        val existing = if (isEditing) subscriptionRepo.getById(subscriptionId) else null
        subscriptionRepo.upsert(
            DetectedSubscription(
                id               = if (isEditing) subscriptionId else 0L,
                merchantName     = name,
                estimatedAmount  = amountCents,
                frequency        = s.frequency,
                nextExpectedDate = s.nextDateMillis,
                lastSeenDate     = existing?.lastSeenDate ?: System.currentTimeMillis(),
                occurrenceCount  = existing?.occurrenceCount ?: 1,
                isConfirmed      = true,
                isDismissed      = false,
                categoryId       = s.categoryId,
                accountId        = s.accountId
            )
        )
        _state.value = _state.value.copy(saved = true)
    }

    fun delete() = viewModelScope.launch {
        if (isEditing) {
            subscriptionRepo.delete(subscriptionId)
            _state.value = _state.value.copy(deleted = true)
        }
    }
}
