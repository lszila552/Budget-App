package com.vrijgeld.domain

import com.vrijgeld.data.model.Transaction
import java.util.Calendar

data class SpendingInsight(
    val type: InsightType,
    val title: String,
    val body: String,
)

enum class InsightType { LIFESTYLE_INFLATION, SMALL_PURCHASES, MERCHANT_SPIKE }

data class MerchantSummary(val name: String, val totalCents: Long, val count: Int)

object SpendingInsightsEngine {

    fun lifestyleInflationAlert(transactions: List<Transaction>, currentYearMonth: String): SpendingInsight? {
        val cal = Calendar.getInstance()
        val months = (0..5).map { offset ->
            val month = cal.get(Calendar.MONTH) + 1 - offset
            val year  = cal.get(Calendar.YEAR) + (month - 1) / 12 - if (month <= 0) 1 else 0
            val adj   = ((month - 1 + 120) % 12) + 1
            "%04d-%02d".format(year, adj)
        }

        fun sumForMonth(ym: String) = transactions
            .filter { it.amount < 0 }
            .filter { txYearMonth(it.date) == ym }
            .sumOf { -it.amount }

        val recent3Avg = (months.take(3).sumOf { sumForMonth(it) }) / 3.0
        val prior3Avg  = (months.drop(3).sumOf { sumForMonth(it) }) / 3.0

        if (prior3Avg <= 0) return null
        val increase = (recent3Avg - prior3Avg) / prior3Avg

        return if (increase > 0.15) {
            SpendingInsight(
                type  = InsightType.LIFESTYLE_INFLATION,
                title = "Lifestyle inflation detected",
                body  = "Your 3-month spending avg is ${(increase * 100).toInt()}% higher than the prior 3 months."
            )
        } else null
    }

    fun smallPurchasesAlert(transactions: List<Transaction>, currentYearMonth: String): SpendingInsight? {
        val small = transactions.filter { it.amount < 0 && -it.amount < 500 && txYearMonth(it.date) == currentYearMonth }
        return if (small.size > 10) {
            SpendingInsight(
                type  = InsightType.SMALL_PURCHASES,
                title = "${small.size} small purchases this month",
                body  = "You made ${small.size} transactions under €5 — totalling €${"%.2f".format(small.sumOf { -it.amount } / 100.0)}."
            )
        } else null
    }

    fun topMerchantsForCategory(transactions: List<Transaction>, catId: Long, yearMonth: String): List<MerchantSummary> =
        transactions
            .filter { it.categoryId == catId && it.amount < 0 && txYearMonth(it.date) == yearMonth }
            .groupBy { it.merchantName?.trim()?.ifEmpty { null } ?: it.description }
            .map { (name, txs) -> MerchantSummary(name, txs.sumOf { -it.amount }, txs.size) }
            .sortedByDescending { it.totalCents }
            .take(5)

    private fun txYearMonth(epochMillis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return "%04d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }
}
