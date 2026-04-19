package com.vrijgeld.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.model.CategoryType
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.FIXED_CATEGORY_NAMES
import com.vrijgeld.domain.SafeToSpendCalculator
import com.vrijgeld.domain.SavingsRateCalculator
import com.vrijgeld.ui.components.CategoryRingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StsBreakdown(
    val incomeThisMonth: Long,
    val fixedRemaining: Long,
    val savingsAllocations: Long,
    val variableSpent: Long,
    val safeToSpend: Long
)

data class HomeUiState(
    val safeToSpendToday: Long               = 0L,
    val safeToSpendMonth: Long               = 0L,
    val savingsRate: Float                   = 0f,
    val targetSavingsRate: Float             = 0.40f,
    val ringCategories: List<CategoryRingData> = emptyList(),
    val breakdown: StsBreakdown?             = null
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
        val cal         = Calendar.getInstance()
        val yearMonth   = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val dayOfMonth  = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val income     = settingsRepo.getMonthlyIncome()
        val targetRate = settingsRepo.getTargetSavingsRate() / 100f
        val cats       = categoryDao.getAllOnce()
        val catByName  = cats.associateBy { it.name }

        val fixedCats   = cats.filter { it.name in FIXED_CATEGORY_NAMES && it.type == CategoryType.EXPENSE }
        val sinkingCats = cats.filter { it.isSinkingFund && it.type == CategoryType.EXPENSE }
        val fixedIds    = fixedCats.map { it.id }.toSet()
        val sinkingIds  = sinkingCats.map { it.id }.toSet()

        val sinkingContributions = sinkingCats.sumOf { it.sinkingFundTarget?.div(12) ?: 0L }

        transactionRepo.getForMonth(yearMonth).collect { transactions ->
            val expenses    = transactions.filter { it.amount < 0 }
            val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
                .let { if (it > 0L) it else income }

            val fixedBudgets = fixedCats.associate { it.id to (it.monthlyBudget ?: 0L) }
            val fixedSpent   = fixedCats.associate { cat ->
                cat.id to expenses.filter { it.categoryId == cat.id }.sumOf { -it.amount }
            }
            val variableSpent = expenses
                .filter { tx -> val cid = tx.categoryId; cid == null || (cid !in fixedIds && cid !in sinkingIds) }
                .sumOf { -it.amount }

            val input = SafeToSpendCalculator.CalcInput(
                incomeThisMonth      = totalIncome,
                fixedBudgets         = fixedBudgets,
                fixedSpent           = fixedSpent,
                sinkingContributions = sinkingContributions,
                variableSpent        = variableSpent,
                dayOfMonth           = dayOfMonth,
                daysInMonth          = daysInMonth
            )
            val sts  = safeToSpendCalc.calculate(input)
            val rate = savingsRateCalc.calculate(totalIncome, expenses.sumOf { -it.amount })

            val fixedRemaining = fixedBudgets.entries.sumOf { (id, budget) ->
                maxOf(0L, budget - (fixedSpent[id] ?: 0L))
            }

            val rings = ringCategoryNames.mapNotNull { name ->
                val cat   = catByName[name] ?: return@mapNotNull null
                val spent = expenses.filter { it.categoryId == cat.id }.sumOf { -it.amount }
                CategoryRingData(cat.name, cat.icon, spent, cat.monthlyBudget ?: 0L)
            }

            _uiState.value = HomeUiState(
                safeToSpendToday  = sts.daily,
                safeToSpendMonth  = sts.monthly,
                savingsRate       = rate,
                targetSavingsRate = targetRate,
                ringCategories    = rings,
                breakdown         = StsBreakdown(
                    incomeThisMonth    = totalIncome,
                    fixedRemaining     = fixedRemaining,
                    savingsAllocations = sinkingContributions,
                    variableSpent      = variableSpent,
                    safeToSpend        = sts.monthly
                )
            )
        }
    }
}
