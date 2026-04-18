package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.AccountDao
import com.vrijgeld.data.db.dao.NetWorthSnapshotDao
import com.vrijgeld.data.model.NetWorthSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetWorthRepository @Inject constructor(
    private val dao: NetWorthSnapshotDao,
    private val accountDao: AccountDao
) {
    fun getAll() = dao.getAll()
    fun getActiveAccountsFlow() = accountDao.getActive()
    suspend fun getLatest() = dao.getLatest()
    suspend fun upsert(s: NetWorthSnapshot) = dao.upsert(s)
    suspend fun getActiveAccounts() = accountDao.getActiveOnce()
}
