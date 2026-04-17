package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.SettingDao
import com.vrijgeld.data.model.AppSetting
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(private val dao: SettingDao) {
    suspend fun get(key: String): String? = dao.get(key)?.value
    suspend fun set(key: String, value: String) = dao.set(AppSetting(key, value))
    suspend fun getMonthlyIncome(): Long = get("monthly_income")?.toLongOrNull() ?: 350_000L
    suspend fun getTargetSavingsRate(): Float = get("target_savings_rate")?.toFloatOrNull() ?: 40f
}
