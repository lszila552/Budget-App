package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.SettingDao
import com.vrijgeld.data.model.AppSetting
import javax.inject.Inject
import javax.inject.Singleton

const val KEY_NOTIF_WEEKLY_PACE        = "notif_weekly_pace"
const val KEY_NOTIF_BILL_LOW_BALANCE   = "notif_bill_low_balance"
const val KEY_NOTIF_UNUSUAL_TX         = "notif_unusual_tx"
const val KEY_NOTIF_SUBSCRIPTION_RENEWAL = "notif_subscription_renewal"

@Singleton
class SettingsRepository @Inject constructor(private val dao: SettingDao) {
    suspend fun get(key: String): String? = dao.get(key)?.value
    suspend fun set(key: String, value: String) = dao.set(AppSetting(key, value))

    suspend fun getMonthlyIncome(): Long  = get("monthly_income")?.toLongOrNull() ?: 350_000L
    suspend fun getTargetSavingsRate(): Float = get("target_savings_rate")?.toFloatOrNull() ?: 40f

    suspend fun getNotifWeeklyPace():        Boolean = get(KEY_NOTIF_WEEKLY_PACE)          != "false"
    suspend fun getNotifBillLowBalance():    Boolean = get(KEY_NOTIF_BILL_LOW_BALANCE)     != "false"
    suspend fun getNotifUnusualTx():         Boolean = get(KEY_NOTIF_UNUSUAL_TX)           != "false"
    suspend fun getNotifSubscriptionRenewal(): Boolean = get(KEY_NOTIF_SUBSCRIPTION_RENEWAL) != "false"
}
