package com.vrijgeld.ui.wealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.AccountType
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.SubscriptionRepository
import com.vrijgeld.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

data class CashFlowPoint(
    val dayOffset: Int,
    val balance: Long,
    val lower: Long,
    val upper: Long
)

data class UpcomingBill(val name: String, val amountCents: Long, val daysUntil: Int)

data class CashFlowUiState(
    val points: List<CashFlowPoint> = emptyList(),
    val upcomingBills: List<UpcomingBill> = emptyList()
)

@HiltViewModel
class CashFlowForecastViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    private val subscriptionRepo: SubscriptionRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashFlowUiState())
    val uiState = _uiState.asStateFlow()

    init { compute() }

    private fun compute() = viewModelScope.launch {
        val cal        = Calendar.getInstance()
        val now        = cal.timeInMillis
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val yearMonth  = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

        val accounts        = accountRepo.getActiveOnce()
        val checkingBalance = accounts
            .filter { it.type == AccountType.CHECKING || it.type == AccountType.SAVINGS }
            .sumOf { it.currentBalance }

        val recentTxs     = transactionRepo.getForMonthOnce(yearMonth)
        val variableSpend = recentTxs.filter { it.amount < 0 }.sumOf { -it.amount }
        val avgDailySpend = if (dayOfMonth > 0 && variableSpend > 0)
            variableSpend / dayOfMonth else settingsRepo.getMonthlyIncome() / 30L

        val incomeTxs  = recentTxs.filter { it.amount > 0 }
        val salaryDay  = if (incomeTxs.isNotEmpty()) {
            val c = Calendar.getInstance().apply { timeInMillis = incomeTxs.first().date }
            c.get(Calendar.DAY_OF_MONTH)
        } else 25
        val expectedIncome = settingsRepo.getMonthlyIncome()

        val cutoff = now + 30L * 86_400_000L
        val subs   = subscriptionRepo.getUpcomingOnce(now, cutoff)

        val upcomingBills = subs.map { sub ->
            UpcomingBill(
                name        = sub.merchantName,
                amountCents = sub.estimatedAmount,
                daysUntil   = ((sub.nextExpectedDate - now) / 86_400_000L).toInt().coerceAtLeast(0)
            )
        }.sortedBy { it.daysUntil }

        // 30-day projection
        val points  = mutableListOf<CashFlowPoint>()
        var balance = checkingBalance

        for (offset in 0..30) {
            val projCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_MONTH, offset)
            }
            val projDayOfMonth = projCal.get(Calendar.DAY_OF_MONTH)

            if (offset > 0 && projDayOfMonth == salaryDay) balance += expectedIncome

            subs.filter { sub ->
                val subCal = Calendar.getInstance().apply { timeInMillis = sub.nextExpectedDate }
                subCal.get(Calendar.DAY_OF_MONTH) == projDayOfMonth &&
                    abs(sub.nextExpectedDate - projCal.timeInMillis) < 2L * 86_400_000L
            }.forEach { balance -= it.estimatedAmount }

            balance -= avgDailySpend

            val variance = (avgDailySpend * offset * 20L) / 100L
            points += CashFlowPoint(
                dayOffset = offset,
                balance   = balance,
                lower     = balance - variance,
                upper     = balance + variance
            )
        }

        _uiState.value = CashFlowUiState(points = points, upcomingBills = upcomingBills)
    }
}
