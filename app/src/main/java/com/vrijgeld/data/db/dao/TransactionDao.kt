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

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE categoryId IS NULL AND importSource IS NOT NULL AND importSource != 'MANUAL'
    """)
    fun countUncategorizedImported(): Flow<Int>

    @Query("""
        SELECT DISTINCT categoryId FROM transactions
        WHERE categoryId IS NOT NULL
        ORDER BY date DESC
        LIMIT 8
    """)
    fun getRecentCategoryIds(): Flow<List<Long>>

    @Query("""
        SELECT * FROM transactions
        WHERE categoryId IS NULL AND importSource IS NOT NULL AND importSource != 'MANUAL'
        ORDER BY date DESC
    """)
    fun getUncategorizedImported(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE amount < 0 ORDER BY date ASC")
    suspend fun getAllExpensesOnce(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND amount < 0 ORDER BY date DESC")
    suspend fun getRecurringOnce(): List<Transaction>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE accountId = :accountId")
    suspend fun getAccountBalance(accountId: Long): Long

    @Query("SELECT * FROM transactions WHERE categoryId = :catId AND amount < 0 AND date >= :since ORDER BY date ASC")
    suspend fun getCategoryExpensesSince(catId: Long, since: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE amount < 0 AND date >= :since ORDER BY date DESC")
    suspend fun getRecentExpensesSince(since: Long): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :yearMonth
        ORDER BY date DESC
    """)
    suspend fun getForMonthOnce(yearMonth: String): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE categoryId = :catId
          AND strftime('%Y-%m', date / 1000, 'unixepoch') IN (:yearMonths)
        ORDER BY date DESC
    """)
    suspend fun getByCategoryYearMonths(catId: Long, yearMonths: List<String>): List<Transaction>

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

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllOnce(): List<Transaction>
}
