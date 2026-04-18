package com.vrijgeld.ui.wealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.model.AccountType
import com.vrijgeld.data.repository.AccountRepository
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.FIRECalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class FireUiState(
    val fiNumber: Long            = 0L,
    val currentPortfolio: Long    = 0L,   // INVESTMENT accounts
    val liquidAssets: Long        = 0L,   // CHECKING + SAVINGS
    val progress: Float           = 0f,
    val savingsRate: Float        = 0f,
    val yearsToFi: Double         = 0.0,
    val coastFi: Long             = 0L,
    val ffRunway: Double          = 0.0,
    val scenarioYearsToFi: Double = 0.0,
    val scenarioExtra: Long       = 20_000L,  // €200/month extra
    val annualExpenses: Long      = 3_000_000L,
    val swrPercent: Float         = 4f,
    val realReturn: Double        = 5.0,
    val birthYear: Int            = 1990,
    val aowMonthly: Long          = 0L,
    val pensionMonthly: Long      = 0L,
    val monthlyExpenses: Long     = 0L,
    val monthlyIncome: Long       = 0L,
    val annualSavings: Long       = 0L
) {
    val currentAge: Int get() = Calendar.getInstance().get(Calendar.YEAR) - birthYear
    val gapMonthly: Long get() = maxOf(0L, annualExpenses / 12 - aowMonthly - pensionMonthly)
}

@HiltViewModel
class FireViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FireUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        val cal       = Calendar.getInstance()
        val yearMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

        val annualExpenses = settingsRepo.getFiAnnualExpenses()
        val swrPercent     = settingsRepo.getFiSwr()
        val realReturn     = settingsRepo.getFiAssumedReturn()
        val birthYear      = settingsRepo.getBirthYear()
        val aowMonthly     = settingsRepo.getAowMonthly()
        val pensionMonthly = settingsRepo.getPensionMonthly()
        val monthlyIncome  = settingsRepo.getMonthlyIncome()

        val fiNumber = FIRECalculator.fiNumber(annualExpenses, swrPercent)

        accountRepo.getActive().collect { accounts ->
            val portfolio    = accounts.filter { it.type == AccountType.INVESTMENT }.sumOf { it.currentBalance }
            val liquidAssets = accounts.filter { it.type == AccountType.CHECKING || it.type == AccountType.SAVINGS }.sumOf { it.currentBalance }

            val txs           = transactionRepo.getForMonthOnce(yearMonth)
            val monthlyExpenses = txs.filter { it.amount < 0 }.sumOf { -it.amount }
            val annualSavings = maxOf(0L, (monthlyIncome - monthlyExpenses) * 12L)

            val progress      = if (fiNumber > 0) (portfolio.toFloat() / fiNumber).coerceIn(0f, 1f) else 0f
            val savingsRate   = FIRECalculator.savingsRate(monthlyIncome, monthlyExpenses)
            val currentAge    = cal.get(Calendar.YEAR) - birthYear
            val coastFi       = FIRECalculator.coastFi(fiNumber, currentAge, 67, realReturn)
            val ffRunway      = FIRECalculator.ffRunway(liquidAssets, monthlyExpenses)
            val yearsToFi     = FIRECalculator.yearsToFi(fiNumber, portfolio, annualSavings, realReturn)
            val scenarioYears = FIRECalculator.yearsToFiWithExtra(fiNumber, portfolio, annualSavings, 20_000L, realReturn)

            _uiState.value = FireUiState(
                fiNumber          = fiNumber,
                currentPortfolio  = portfolio,
                liquidAssets      = liquidAssets,
                progress          = progress,
                savingsRate       = savingsRate,
                yearsToFi         = yearsToFi,
                coastFi           = coastFi,
                ffRunway          = ffRunway,
                scenarioYearsToFi = scenarioYears,
                scenarioExtra     = 20_000L,
                annualExpenses    = annualExpenses,
                swrPercent        = swrPercent,
                realReturn        = realReturn,
                birthYear         = birthYear,
                aowMonthly        = aowMonthly,
                pensionMonthly    = pensionMonthly,
                monthlyExpenses   = monthlyExpenses,
                monthlyIncome     = monthlyIncome,
                annualSavings     = annualSavings
            )
        }
    }

    fun saveSettings(
        annualExpenses: String,
        swr: String,
        realReturn: String,
        birthYear: String,
        aow: String,
        pension: String
    ) = viewModelScope.launch {
        annualExpenses.toDoubleOrNull()?.let { settingsRepo.set("fi_annual_expenses", (it * 100).toLong().toString()) }
        swr.toFloatOrNull()?.let { settingsRepo.set("fi_swr", it.toString()) }
        realReturn.toDoubleOrNull()?.let { settingsRepo.set("fi_assumed_return", it.toString()) }
        birthYear.toIntOrNull()?.let { settingsRepo.set("birth_year", it.toString()) }
        aow.toDoubleOrNull()?.let { settingsRepo.set("aow_monthly", (it * 100).toLong().toString()) }
        pension.toDoubleOrNull()?.let { settingsRepo.set("occupational_pension_monthly", (it * 100).toLong().toString()) }
        load()
    }
}
