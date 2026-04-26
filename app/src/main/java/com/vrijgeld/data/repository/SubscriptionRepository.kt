package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.DetectedSubscriptionDao
import com.vrijgeld.data.model.DetectedSubscription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(private val dao: DetectedSubscriptionDao) {
    fun getAll()       = dao.getAll()
    fun getConfirmed() = dao.getConfirmed()
    suspend fun getByMerchant(name: String)           = dao.getByMerchant(name)
    suspend fun upsert(s: DetectedSubscription)       = dao.upsert(s)
    suspend fun dismiss(id: Long)                     = dao.dismiss(id)
    suspend fun confirm(id: Long)                     = dao.confirm(id)
    suspend fun getUpcomingOnce(from: Long, to: Long) = dao.getUpcomingOnce(from, to)
    suspend fun getById(id: Long)                     = dao.getById(id)
    suspend fun delete(id: Long)                      = dao.delete(id)
}
