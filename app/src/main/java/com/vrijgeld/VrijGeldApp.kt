package com.vrijgeld

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vrijgeld.data.seed.DatabaseSeeder
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
        MainScope().launch { seeder.seedIfNeeded() }
        scheduleWidgetUpdates()
    }

    private fun scheduleWidgetUpdates() {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget_update",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
