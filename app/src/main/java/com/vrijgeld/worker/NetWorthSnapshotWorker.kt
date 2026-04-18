package com.vrijgeld.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vrijgeld.data.db.dao.AccountDao
import com.vrijgeld.data.db.dao.NetWorthSnapshotDao
import com.vrijgeld.data.model.AccountType
import com.vrijgeld.data.model.NetWorthSnapshot
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.json.JSONObject
import java.util.Calendar

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NetWorthWorkerEntryPoint {
    fun accountDao(): AccountDao
    fun netWorthSnapshotDao(): NetWorthSnapshotDao
}

class NetWorthSnapshotWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.DAY_OF_MONTH) != 1) return Result.success()

        val ep           = EntryPointAccessors.fromApplication(context, NetWorthWorkerEntryPoint::class.java)
        val accountDao   = ep.accountDao()
        val snapshotDao  = ep.netWorthSnapshotDao()

        val yearMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val accounts  = accountDao.getActiveOnce().filter { it.includeInNetWorth }

        val totalAssets      = accounts.filter { it.type != AccountType.LIABILITY }.sumOf { it.currentBalance }
        val totalLiabilities = accounts.filter { it.type == AccountType.LIABILITY  }.sumOf { it.currentBalance }

        val breakdown = JSONObject().apply {
            accounts.forEach { put(it.name, it.currentBalance) }
        }.toString()

        snapshotDao.upsert(
            NetWorthSnapshot(
                yearMonth        = yearMonth,
                totalAssets      = totalAssets,
                totalLiabilities = totalLiabilities,
                netWorth         = totalAssets - totalLiabilities,
                breakdown        = breakdown
            )
        )

        return Result.success()
    }
}
