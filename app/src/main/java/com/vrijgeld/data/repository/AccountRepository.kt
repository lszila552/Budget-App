package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.AccountDao
import com.vrijgeld.data.model.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(private val dao: AccountDao) {
    fun getAll() = dao.getAll()
    fun getActive() = dao.getActive()
    suspend fun insert(a: Account) = dao.insert(a)
    suspend fun insertAll(accounts: List<Account>) = dao.insertAll(accounts)
    suspend fun update(a: Account) = dao.update(a)
    suspend fun updateBalance(id: Long, balance: Long) = dao.updateBalance(id, balance)
}
