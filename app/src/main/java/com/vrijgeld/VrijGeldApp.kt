package com.vrijgeld

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vrijgeld.data.seed.DatabaseSeeder
import com.vrijgeld.domain.CHANNEL_BILL
import com.vrijgeld.domain.CHANNEL_PACE
import com.vrijgeld.domain.CHANNEL_SUBSCRIPTION
import com.vrijgeld.domain.CHANNEL_UNUSUAL
import com.vrijgeld.domain.CHANNEL_WEEKLY_REVIEW
import com.vrijgeld.worker.MonthRolloverWorker
import com.vrijgeld.worker.NetWorthSnapshotWorker
import com.vrijgeld.worker.NotificationWorker
import com.vrijgeld.worker.SubscriptionDetectionWorker
import com.vrijgeld.worker.WeeklyReviewWorker
import com.vrijgeld.worker.WidgetUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class VrijGeldApp : Application() {

    @Inject lateinit var seeder: DatabaseSeeder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        MainScope().launch { seeder.seedIfNeeded() }
        scheduleBackgroundWork()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(CHANNEL_PACE,         "Weekly Pace Check",     NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_BILL,         "Bill Due",              NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_UNUSUAL,      "Unusual Transaction",   NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_SUBSCRIPTION,  "Subscription Renewal",  NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_WEEKLY_REVIEW, "Weekly Review",         NotificationManager.IMPORTANCE_DEFAULT),
        ).forEach { nm.createNotificationChannel(it) }
    }

    private fun scheduleBackgroundWork() {
        val wm = WorkManager.getInstance(this)

        wm.enqueueUniquePeriodicWork(
            "widget_update",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.DAYS).build()
        )
        wm.enqueueUniquePeriodicWork(
            "subscription_detection",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SubscriptionDetectionWorker>(1, TimeUnit.DAYS).build()
        )
        wm.enqueueUniquePeriodicWork(
            "notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        )
        wm.enqueueUniquePeriodicWork(
            "month_rollover",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<MonthRolloverWorker>(1, TimeUnit.DAYS).build()
        )
        wm.enqueueUniquePeriodicWork(
            "net_worth_snapshot",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NetWorthSnapshotWorker>(1, TimeUnit.DAYS).build()
        )
        wm.enqueueUniquePeriodicWork(
            "weekly_review",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WeeklyReviewWorker>(1, TimeUnit.DAYS).build()
        )
    }
}
