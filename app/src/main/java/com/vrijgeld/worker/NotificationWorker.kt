package com.vrijgeld.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vrijgeld.data.db.dao.AccountDao
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.repository.SettingsRepository
import com.vrijgeld.data.repository.SubscriptionRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.NotificationHelper
import com.vrijgeld.domain.nextFutureOccurrence
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.math.pow
import kotlin.math.sqrt

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationWorkerEntryPoint {
    fun transactionRepository(): TransactionRepository
    fun subscriptionRepository(): SubscriptionRepository
    fun settingsRepository(): SettingsRepository
    fun categoryDao(): CategoryDao
    fun accountDao(): AccountDao
    fun notificationHelper(): NotificationHelper
}

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val MS_PER_DAY = 86_400_000L
        private const val SIX_MONTHS_MS = 180L * MS_PER_DAY
    }

    override suspend fun doWork(): Result {
        val ep      = EntryPointAccessors.fromApplication(context, NotificationWorkerEntryPoint::class.java)
        val txRepo  = ep.transactionRepository()
        val subRepo = ep.subscriptionRepository()
        val prefs   = ep.settingsRepository()
        val catDao  = ep.categoryDao()
        val accDao  = ep.accountDao()
        val notif   = ep.notificationHelper()

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val isMonday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

        // 1. Weekly pace check
        if (isMonday && prefs.getNotifWeeklyPace()) {
            runCatching { checkWeeklyPace(txRepo, catDao, prefs, notif, cal) }
        }

        // 2. Subscription renewal (3 days out)
        if (prefs.getNotifSubscriptionRenewal()) {
            val upcoming = subRepo.getUpcomingOnce(now, now + 3 * MS_PER_DAY)
            upcoming.forEach { sub ->
                val daysUntil = ((sub.nextExpectedDate - now) / MS_PER_DAY).toInt().coerceAtLeast(0)
                notif.notifySubscriptionRenewal(sub.merchantName, sub.estimatedAmount, daysUntil)
            }
        }

        // 3. Bill due + low balance (2 days out)
        if (prefs.getNotifBillLowBalance()) {
            val dueSoon = subRepo.getUpcomingOnce(now, now + 2 * MS_PER_DAY)
            if (dueSoon.isNotEmpty()) {
                val totalBalance = accDao.getActiveOnce()
                    .sumOf { txRepo.getAccountBalance(it.id) }
                dueSoon.forEach { sub ->
                    if (totalBalance < sub.estimatedAmount * 1.5) {
                        val days = ((sub.nextExpectedDate - now) / MS_PER_DAY).toInt().coerceAtLeast(0)
                        notif.notifyBillDueLowBalance(sub.merchantName, sub.estimatedAmount, days)
                    }
                }
            }
        }

        // 4. Unusual transaction (today's expenses vs 6-month category average)
        if (prefs.getNotifUnusualTx()) {
            runCatching { checkUnusualTransactions(txRepo, catDao, notif, now) }
        }

        return Result.success()
    }

    private suspend fun checkWeeklyPace(
        txRepo: TransactionRepository,
        catDao: CategoryDao,
        prefs: SettingsRepository,
        notif: NotificationHelper,
        cal: Calendar
    ) {
        val yearMonth   = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val dayOfMonth  = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val txs         = txRepo.getForMonth(yearMonth).first()
        val cats        = catDao.getAllOnce().filter { it.monthlyBudget != null && it.monthlyBudget > 0 }

        data class PaceEntry(val name: String, val spent: Long, val budget: Long, val pct: Int)

        val paceEntries = cats.mapNotNull { cat ->
            val budget  = cat.monthlyBudget ?: return@mapNotNull null
            val spent   = txs.filter { it.categoryId == cat.id && it.amount < 0 }.sumOf { -it.amount }
            val proratedBudget = budget * dayOfMonth / daysInMonth
            if (proratedBudget <= 0) return@mapNotNull null
            val pct = (spent * 100 / proratedBudget).toInt()
            PaceEntry(cat.name, spent, budget, pct)
        }

        val worst = paceEntries.maxByOrNull { it.pct } ?: return
        if (worst.pct >= 80) {
            notif.notifyWeeklyPace(worst.name, worst.spent, worst.budget, worst.pct)
        }
    }

    private suspend fun checkUnusualTransactions(
        txRepo: TransactionRepository,
        catDao: CategoryDao,
        notif: NotificationHelper,
        now: Long
    ) {
        val todayStart  = now - MS_PER_DAY
        val todayTxs    = txRepo.getRecentExpensesSince(todayStart)
        val cats        = catDao.getAllOnce().associateBy { it.id }
        val since       = now - 6 * 30 * MS_PER_DAY

        val catGroups = todayTxs.groupBy { it.categoryId }
        catGroups.forEach { (catId, newTxs) ->
            catId ?: return@forEach
            val history = txRepo.getCategoryExpensesSince(catId, since)
            if (history.size < 5) return@forEach

            val amounts = history.map { -it.amount.toDouble() }
            val mean    = amounts.average()
            val stdDev  = sqrt(amounts.map { (it - mean).pow(2) }.average())
            val threshold = mean + 2 * stdDev

            newTxs.forEach { tx ->
                if (-tx.amount > threshold) {
                    val catName = cats[catId]?.name ?: "Unknown"
                    notif.notifyUnusualTransaction(tx.description, -tx.amount, catName)
                }
            }
        }
    }
}
