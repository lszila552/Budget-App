package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder")
    suspend fun getAllOnce(): List<Category>

    @Query("SELECT * FROM categories WHERE type = 'EXPENSE' AND isActive = 1 ORDER BY sortOrder")
    fun getExpenseCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = 'INCOME' AND isActive = 1 ORDER BY sortOrder")
    fun getIncomeCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isSinkingFund = 1 AND isActive = 1 ORDER BY sortOrder")
    fun getSinkingFunds(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>): List<Long>

    @Update
    suspend fun update(category: Category)
}
