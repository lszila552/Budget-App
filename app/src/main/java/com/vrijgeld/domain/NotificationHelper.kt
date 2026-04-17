package com.vrijgeld.domain

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vrijgeld.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

const val CHANNEL_PACE         = "pace_check"
const val CHANNEL_BILL         = "bill_due"
const val CHANNEL_UNUSUAL      = "unusual_tx"
const val CHANNEL_SUBSCRIPTION = "subscription_renewal"

@Singleton
class NotificationHelper @Inject constructor(@dagger.hilt.android.qualifiers.ApplicationContext private val context: Context) {

    private val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun launchIntent() = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    fun notifyWeeklyPace(categoryName: String, spentCents: Long, budgetCents: Long, pct: Int) {
        val text = "You've spent €${spentCents / 100} of €${budgetCents / 100} in $categoryName ($pct%)"
        post(1001, CHANNEL_PACE, "Weekly Pace Check", text)
    }

    fun notifyBillDueLowBalance(merchantName: String, amountCents: Long, daysUntil: Int) {
        val text = "€${amountCents / 100} due in $daysUntil day(s) — account balance may be too low"
        post(1002, CHANNEL_BILL, "Bill Due: $merchantName", text)
    }

    fun notifyUnusualTransaction(description: String, amountCents: Long, categoryName: String) {
        val text = "€${amountCents / 100} in $categoryName is unusually high"
        post(1003, CHANNEL_UNUSUAL, "Unusual charge: $description", text)
    }

    fun notifySubscriptionRenewal(merchantName: String, amountCents: Long, daysUntil: Int) {
        val text = "€${amountCents / 100} subscription renews in $daysUntil day(s)"
        post(1004, CHANNEL_SUBSCRIPTION, "Renewal: $merchantName", text)
    }

    private fun post(id: Int, channel: String, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(launchIntent())
            .setAutoCancel(true)
            .build()
        nm.notify(id, notification)
    }
}
