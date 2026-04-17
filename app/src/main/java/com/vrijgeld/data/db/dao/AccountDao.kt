package com.vrijgeld.data.db.dao

import androidx.room.*
import com.vrijgeld.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY sortOrder")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY sortOrder")
    fun getActive(): Flow<List<Account>>

    @Query("UPDATE accounts SET currentBalance = :balance WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>): List<Long>

    @Update
    suspend fun update(account: Account)
}
