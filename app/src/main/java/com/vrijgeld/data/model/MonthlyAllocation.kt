package com.vrijgeld.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "allocations",
    indices = [Index(value = ["categoryId", "yearMonth"], unique = true)]
)
data class MonthlyAllocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val yearMonth: String,      // "2026-04"
    val allocated: Long,        // cents
    val carriedOver: Long = 0   // cents
)
