package com.vrijgeld.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.FIXED_CATEGORY_NAMES
import com.vrijgeld.domain.SafeToSpendCalculator
import com.vrijgeld.widget.KEY_DAILY_STS
import com.vrijgeld.widget.SafeToSpendWidget
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import kotlinx.coroutines.flow.first
import java.util.Calendar

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetWorkerEntryPoint {
    fun transactionRepository(): TransactionRepository
    fun settingsRepository(): SettingsRepository
    fun categoryDao(): CategoryDao
    fun safeToSpendCalculator(): SafeToSpendCalculator
}

class WidgetUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ep   = EntryPointAccessors.fromApplication(context, WidgetWorkerEntryPoint::class.java)
        val txRepo   = ep.transactionRepository()
        val settings = ep.settingsRepository()
        val catDao   = ep.categoryDao()
        val calc     = ep.safeToSpendCalculator()

        val cal         = Calendar.getInstance()
        val yearMonth   = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val dayOfMonth  = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val income   = settings.getMonthlyIncome()
        val cats     = catDao.getAllOnce()
        val fixedCats   = cats.filter { it.name in FIXED_CATEGORY_NAMES }
        val sinkingCats = cats.filter { it.isSinkingFund }
        val fixedIds    = fixedCats.map { it.id }.toSet()
        val sinkingIds  = sinkingCats.map { it.id }.toSet()
        val sinkingContribs = sinkingCats.sumOf { it.sinkingFundTarget?.div(12) ?: 0L }

        val txs      = txRepo.getForMonth(yearMonth).first()
        val expenses = txs.filter { it.amount < 0 }
        val totalIncome = txs.filter { it.amount > 0 }.sumOf { it.amount }.let { if (it > 0L) it else income }

        val fixedBudgets = fixedCats.associate { it.id to (it.monthlyBudget ?: 0L) }
        val fixedSpent   = fixedCats.associate { cat ->
            cat.id to expenses.filter { it.categoryId == cat.id }.sumOf { -it.amount }
        }
        val variableSpent = expenses
            .filter { tx -> val cid = tx.categoryId; cid == null || (cid !in fixedIds && cid !in sinkingIds) }
            .sumOf { -it.amount }

        val sts = calc.calculate(
            SafeToSpendCalculator.CalcInput(
                incomeThisMonth      = totalIncome,
                fixedBudgets         = fixedBudgets,
                fixedSpent           = fixedSpent,
                sinkingContributions = sinkingContribs,
                variableSpent        = variableSpent,
                dayOfMonth           = dayOfMonth,
                daysInMonth          = daysInMonth
            )
        )

        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(SafeToSpendWidget::class.java).forEach { id ->
            updateAppWidgetState(context, id) { prefs ->
                prefs[KEY_DAILY_STS] = sts.daily
            }
            SafeToSpendWidget().update(context, id)
        }

        return Result.success()
    }
}
