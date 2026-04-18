package com.vrijgeld.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detected_subscriptions")
data class DetectedSubscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val estimatedAmount: Long,
    val frequency: RecurrenceFrequency,
    val nextExpectedDate: Long,
    val lastSeenDate: Long,
    val occurrenceCount: Int,
    val isConfirmed: Boolean = false,
    val isDismissed: Boolean = false
)
