package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE categoryId = :catId
          AND strftime('%Y-%m', date / 1000, 'unixepoch') = :yearMonth
        ORDER BY date DESC
    """)
    fun getByCategory(catId: Long, yearMonth: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE categoryId IS NULL ORDER BY date DESC")
    fun getUncategorized(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :yearMonth
        ORDER BY date DESC
    """)
    fun getForMonth(yearMonth: String): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>): List<Long>

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE importHash = :hash LIMIT 1")
    suspend fun getByImportHash(hash: String): Transaction?
}
