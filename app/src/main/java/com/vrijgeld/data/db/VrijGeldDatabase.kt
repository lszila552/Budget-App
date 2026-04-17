package com.vrijgeld.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vrijgeld.data.db.dao.*
import com.vrijgeld.data.model.*

@Database(
    entities = [
        Account::class,
        Category::class,
        Transaction::class,
        CategorizationRule::class,
        MonthlyAllocation::class,
        NetWorthSnapshot::class,
        AppSetting::class,
        DetectedSubscription::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VrijGeldDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun ruleDao(): RuleDao
    abstract fun allocationDao(): AllocationDao
    abstract fun netWorthSnapshotDao(): NetWorthSnapshotDao
    abstract fun detectedSubscriptionDao(): DetectedSubscriptionDao
    abstract fun settingDao(): SettingDao
}
