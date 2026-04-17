package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.DetectedSubscriptionDao
import com.vrijgeld.data.model.DetectedSubscription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(private val dao: DetectedSubscriptionDao) {
    fun getAll() = dao.getAll()
    suspend fun upsert(s: DetectedSubscription) = dao.upsert(s)
    suspend fun dismiss(id: Long) = dao.dismiss(id)
    suspend fun confirm(id: Long) = dao.confirm(id)
}
