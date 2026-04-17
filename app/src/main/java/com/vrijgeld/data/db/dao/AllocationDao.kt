package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.MonthlyAllocation
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationDao {

    @Query("SELECT * FROM allocations WHERE yearMonth = :yearMonth")
    fun getForMonth(yearMonth: String): Flow<List<MonthlyAllocation>>

    @Query("SELECT * FROM allocations WHERE yearMonth = :yearMonth")
    suspend fun getForMonthOnce(yearMonth: String): List<MonthlyAllocation>

    @Query("SELECT * FROM allocations WHERE yearMonth IN (:yearMonths)")
    suspend fun getForYearMonthsOnce(yearMonths: List<String>): List<MonthlyAllocation>

    @Query("SELECT * FROM allocations WHERE categoryId = :categoryId ORDER BY yearMonth ASC")
    suspend fun getForCategoryOnce(categoryId: Long): List<MonthlyAllocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(allocation: MonthlyAllocation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(allocations: List<MonthlyAllocation>)
}
