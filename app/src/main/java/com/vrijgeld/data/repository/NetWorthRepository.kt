package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.NetWorthSnapshotDao
import com.vrijgeld.data.model.NetWorthSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetWorthRepository @Inject constructor(private val dao: NetWorthSnapshotDao) {
    fun getAll() = dao.getAll()
    suspend fun getLatest() = dao.getLatest()
    suspend fun upsert(s: NetWorthSnapshot) = dao.upsert(s)
}
