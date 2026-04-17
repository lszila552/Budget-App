package com.vrijgeld.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "net_worth_snapshots")
data class NetWorthSnapshot(
    @PrimaryKey val yearMonth: String,
    val totalAssets: Long,
    val totalLiabilities: Long,
    val netWorth: Long,
    val breakdown: String   // JSON map
)
