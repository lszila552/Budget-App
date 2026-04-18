package com.vrijgeld.di

import android.content.Context
import androidx.room.Room
import com.vrijgeld.data.db.*
import com.vrijgeld.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VrijGeldDatabase {
        val passphrase = DatabaseKeyManager(context).getOrCreateKey()
        return Room.databaseBuilder(context, VrijGeldDatabase::class.java, "vrijgeld.db")
            .openHelperFactory(SupportFactory(passphrase))
            .build()
    }

    @Provides fun provideTransactionDao(db: VrijGeldDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideAccountDao(db: VrijGeldDatabase): AccountDao = db.accountDao()
    @Provides fun provideCategoryDao(db: VrijGeldDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideRuleDao(db: VrijGeldDatabase): RuleDao = db.ruleDao()
    @Provides fun provideAllocationDao(db: VrijGeldDatabase): AllocationDao = db.allocationDao()
    @Provides fun provideNetWorthSnapshotDao(db: VrijGeldDatabase): NetWorthSnapshotDao = db.netWorthSnapshotDao()
    @Provides fun provideDetectedSubscriptionDao(db: VrijGeldDatabase): DetectedSubscriptionDao = db.detectedSubscriptionDao()
    @Provides fun provideSettingDao(db: VrijGeldDatabase): SettingDao = db.settingDao()
}
