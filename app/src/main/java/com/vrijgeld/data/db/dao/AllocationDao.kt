package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.MonthlyAllocation
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationDao {

    @Query("SELECT * FROM allocations WHERE yearMonth = :yearMonth")
    fun getForMonth(yearMonth: String): Flow<List<MonthlyAllocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(allocation: MonthlyAllocation)
}
