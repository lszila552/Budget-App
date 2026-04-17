package com.vrijgeld.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.SafeToSpendCalculator
import com.vrijgeld.domain.SavingsRateCalculator
import com.vrijgeld.ui.components.CategoryRingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val safeToSpendToday: Long  = 0L,
    val safeToSpendMonth: Long  = 0L,
    val savingsRate: Float      = 0f,
    val targetSavingsRate: Float = 0.40f,
    val ringCategories: List<CategoryRingData> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val settingsRepo: SettingsRepository,
    private val categoryDao: CategoryDao,
    private val safeToSpendCalc: SafeToSpendCalculator,
    private val savingsRateCalc: SavingsRateCalculator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val ringCategoryNames = listOf("Dining Out", "Groceries", "Entertainment", "Gifts", "Big Purchases")

    init { load() }

    private fun load() = viewModelScope.launch {
        val cal        = Calendar.getInstance()
        val yearMonth  = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val income     = settingsRepo.getMonthlyIncome()
        val targetRate = settingsRepo.getTargetSavingsRate() / 100f
        val cats       = categoryDao.getAllOnce().associateBy { it.name }

        transactionRepo.getForMonth(yearMonth).collect { transactions ->
            val expenses = transactions.filter { it.amount < 0 }
            val totalExpenses = expenses.sumOf { -it.amount }
            val totalIncome   = transactions.filter { it.amount > 0 }.sumOf { it.amount }
                .let { if (it > 0) it else income }

            val sts  = safeToSpendCalc.calculate(income, totalExpenses, dayOfMonth, daysInMonth)
            val rate = savingsRateCalc.calculate(totalIncome, totalExpenses)

            val rings = ringCategoryNames.mapNotNull { name ->
                val cat = cats[name] ?: return@mapNotNull null
                val spent = expenses.filter { it.categoryId == cat.id }.sumOf { -it.amount }
                CategoryRingData(cat.name, cat.icon, spent, cat.monthlyBudget ?: 0L)
            }

            _uiState.value = HomeUiState(
                safeToSpendToday  = sts.daily,
                safeToSpendMonth  = sts.monthly,
                savingsRate       = rate,
                targetSavingsRate = targetRate,
                ringCategories    = rings
            )
        }
    }
}
