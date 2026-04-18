package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.CategorizationRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules ORDER BY priority DESC")
    fun getAllByPriority(): Flow<List<CategorizationRule>>

    @Query("SELECT * FROM rules ORDER BY priority DESC")
    suspend fun getAllByPriorityOnce(): List<CategorizationRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: CategorizationRule): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rules: List<CategorizationRule>): List<Long>

    @Delete
    suspend fun delete(rule: CategorizationRule)
}
