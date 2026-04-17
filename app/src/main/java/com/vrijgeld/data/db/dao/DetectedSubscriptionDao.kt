package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.DetectedSubscription
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectedSubscriptionDao {

    @Query("SELECT * FROM detected_subscriptions WHERE isDismissed = 0 ORDER BY nextExpectedDate")
    fun getAll(): Flow<List<DetectedSubscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subscription: DetectedSubscription)

    @Query("UPDATE detected_subscriptions SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long)

    @Query("UPDATE detected_subscriptions SET isConfirmed = 1 WHERE id = :id")
    suspend fun confirm(id: Long)
}
