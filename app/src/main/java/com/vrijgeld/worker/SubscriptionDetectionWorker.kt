package com.vrijgeld.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vrijgeld.data.repository.SubscriptionRepository
import com.vrijgeld.data.repository.TransactionRepository
import com.vrijgeld.domain.SubscriptionDetector
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SubscriptionWorkerEntryPoint {
    fun transactionRepository(): TransactionRepository
    fun subscriptionRepository(): SubscriptionRepository
}

class SubscriptionDetectionWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ep       = EntryPointAccessors.fromApplication(context, SubscriptionWorkerEntryPoint::class.java)
        val txRepo   = ep.transactionRepository()
        val subRepo  = ep.subscriptionRepository()
        val detector = SubscriptionDetector()

        val expenses  = txRepo.getAllExpensesOnce()
        val detected  = detector.detect(expenses)

        detected.forEach { candidate ->
            val existing = subRepo.getByMerchant(candidate.merchantName)
            val toSave = if (existing != null) {
                candidate.copy(
                    id          = existing.id,
                    isConfirmed = existing.isConfirmed,
                    isDismissed = existing.isDismissed
                )
            } else candidate
            subRepo.upsert(toSave)
        }

        return Result.success()
    }
}
