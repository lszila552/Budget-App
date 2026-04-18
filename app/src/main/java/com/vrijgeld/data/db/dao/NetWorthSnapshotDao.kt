package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.NetWorthSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface NetWorthSnapshotDao {

    @Query("SELECT * FROM net_worth_snapshots ORDER BY yearMonth DESC")
    fun getAll(): Flow<List<NetWorthSnapshot>>

    @Query("SELECT * FROM net_worth_snapshots ORDER BY yearMonth DESC LIMIT 1")
    suspend fun getLatest(): NetWorthSnapshot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: NetWorthSnapshot)
}
