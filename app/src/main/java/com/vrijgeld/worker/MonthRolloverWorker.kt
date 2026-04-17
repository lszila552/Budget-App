package com.vrijgeld.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vrijgeld.data.db.dao.AccountDao
import com.vrijgeld.data.db.dao.AllocationDao
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.model.CategoryType
import com.vrijgeld.data.model.ImportSource
import com.vrijgeld.data.model.MonthlyAllocation
import com.vrijgeld.data.model.Transaction
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.TransactionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Calendar

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RolloverWorkerEntryPoint {
    fun allocationDao(): AllocationDao
    fun categoryDao(): CategoryDao
    fun transactionRepository(): TransactionRepository
    fun settingsRepository(): SettingsRepository
    fun accountDao(): AccountDao
}

class MonthRolloverWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.DAY_OF_MONTH) != 1) return Result.success()

        val ep         = EntryPointAccessors.fromApplication(context, RolloverWorkerEntryPoint::class.java)
        val allocDao   = ep.allocationDao()
        val catDao     = ep.categoryDao()
        val txRepo     = ep.transactionRepository()
        val settings   = ep.settingsRepository()
        val accountDao = ep.accountDao()

        val prevCal   = Calendar.getInstance().also { it.add(Calendar.MONTH, -1) }
        val lastMonth = "%04d-%02d".format(prevCal.get(Calendar.YEAR), prevCal.get(Calendar.MONTH) + 1)
        val thisMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

        val categories       = catDao.getAllOnce().filter { it.type == CategoryType.EXPENSE && it.isActive }
        val lastAllocations  = allocDao.getForMonthOnce(lastMonth)
        val lastTxs          = txRepo.getForMonthOnce(lastMonth)
        val lastSpentByCat   = lastTxs.filter { it.amount < 0 }
            .groupBy { it.categoryId }
            .mapValues { (_, ts) -> ts.sumOf { -it.amount } }

        val existingThisMonth = allocDao.getForMonthOnce(thisMonth).map { it.categoryId }.toSet()

        val newAllocations = categories
            .filter { it.id !in existingThisMonth }
            .map { cat ->
                val last    = lastAllocations.find { it.categoryId == cat.id }
                val default = cat.monthlyBudget ?: 0L

                val carryOver = if (cat.rollover && last != null) {
                    val lastSpent = lastSpentByCat[cat.id] ?: 0L
                    maxOf(0L, last.allocated + last.carriedOver - lastSpent)
                } else 0L

                MonthlyAllocation(
                    categoryId  = cat.id,
                    yearMonth   = thisMonth,
                    allocated   = last?.allocated ?: default,
                    carriedOver = carryOver
                )
            }

        if (newAllocations.isNotEmpty()) allocDao.upsertAll(newAllocations)

        // Vakantiegeld smoothing drip
        val holdingCents = settings.getVakantiegeldHoldingCents()
        val dripCents    = settings.getVakantiegeldDripCents()
        if (holdingCents > 0 && dripCents > 0) {
            val account = accountDao.getActiveOnce().firstOrNull()
            if (account != null) {
                val vakCatId = catDao.getAllOnce().find { it.name == "Vakantiegeld" }?.id
                txRepo.insert(
                    Transaction(
                        accountId    = account.id,
                        categoryId   = vakCatId,
                        amount       = dripCents,
                        date         = System.currentTimeMillis(),
                        description  = "Vakantiegeld (maandelijkse vrijgave)",
                        importSource = ImportSource.MANUAL,
                        isReviewed   = true
                    )
                )
                val remaining = maxOf(0L, holdingCents - dripCents)
                settings.set("vakantiegeld_holding_cents", remaining.toString())
            }
        }

        return Result.success()
    }
}
