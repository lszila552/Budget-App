package com.vrijgeld

import android.app.Application
import com.vrijgeld.data.seed.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VrijGeldApp : Application() {

    @Inject lateinit var seeder: DatabaseSeeder

    override fun onCreate() {
        super.onCreate()
        MainScope().launch { seeder.seedIfNeeded() }
    }
}
