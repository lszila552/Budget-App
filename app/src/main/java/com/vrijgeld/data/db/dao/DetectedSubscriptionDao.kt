package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.DetectedSubscription
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectedSubscriptionDao {

    @Query("SELECT * FROM detected_subscriptions WHERE isDismissed = 0 ORDER BY nextExpectedDate")
    fun getAll(): Flow<List<DetectedSubscription>>

    @Query("SELECT * FROM detected_subscriptions WHERE isDismissed = 0 AND isConfirmed = 1 ORDER BY nextExpectedDate")
    fun getConfirmed(): Flow<List<DetectedSubscription>>

    @Query("SELECT * FROM detected_subscriptions WHERE isDismissed = 0 AND nextExpectedDate BETWEEN :from AND :to ORDER BY nextExpectedDate")
    suspend fun getUpcomingOnce(from: Long, to: Long): List<DetectedSubscription>

    @Query("SELECT * FROM detected_subscriptions WHERE merchantName = :name LIMIT 1")
    suspend fun getByMerchant(name: String): DetectedSubscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subscription: DetectedSubscription)

    @Query("UPDATE detected_subscriptions SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long)

    @Query("UPDATE detected_subscriptions SET isConfirmed = 1 WHERE id = :id")
    suspend fun confirm(id: Long)

    @Query("SELECT * FROM detected_subscriptions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DetectedSubscription?

    @Query("DELETE FROM detected_subscriptions WHERE id = :id")
    suspend fun delete(id: Long)
}
