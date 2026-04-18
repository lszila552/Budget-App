package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.AppSetting

@Dao
interface SettingDao {

    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun get(key: String): AppSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: AppSetting)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(settings: List<AppSetting>)
}
