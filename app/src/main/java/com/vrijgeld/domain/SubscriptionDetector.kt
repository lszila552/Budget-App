package com.vrijgeld.domain

import com.vrijgeld.data.model.DetectedSubscription
import com.vrijgeld.data.model.RecurrenceFrequency
import com.vrijgeld.data.model.Transaction
import kotlin.math.pow
import kotlin.math.sqrt

class SubscriptionDetector {

    companion object {
        private const val MS_PER_DAY = 86_400_000L
        private val TRAILING_NUMBERS = Regex("""\s*\d+\s*$""")
    }

    fun detect(expenses: List<Transaction>): List<DetectedSubscription> {
        val now = System.currentTimeMillis()
        return expenses
            .groupBy { normalizeName(it) }
            .filter { (_, txs) -> txs.size >= 3 }
            .mapNotNull { (name, txs) -> analyzeGroup(name, txs.sortedBy { it.date }, now) }
    }

    private fun analyzeGroup(
        name: String,
        sorted: List<Transaction>,
        now: Long
    ): DetectedSubscription? {
        val dates   = sorted.map { it.date }
        val amounts = sorted.map { -it.amount }   // positive cents

        val diffs = dates.zipWithNext { a, b -> (b - a) / MS_PER_DAY }
        if (diffs.isEmpty()) return null
        val medianDays = medianLong(diffs)

        val frequency = classifyFrequency(medianDays) ?: return null

        // Reject high coefficient of variation (habitual purchases like groceries)
        val mean = amounts.average()
        if (mean <= 0) return null
        val stdDev = sqrt(amounts.map { (it - mean).pow(2) }.average())
        if (stdDev / mean > 0.4) return null

        // Reject very high frequency merchants (> 8 occurrences per 30 days)
        val spanDays = (dates.last() - dates.first()).toDouble() / MS_PER_DAY
        if (spanDays > 0 && (sorted.size.toDouble() / spanDays) * 30 > 8) return null

        val lastDate     = dates.last()
        val nextExpected = lastDate + medianDays * MS_PER_DAY

        return DetectedSubscription(
            merchantName     = name,
            estimatedAmount  = medianLong(amounts),
            frequency        = frequency,
            nextExpectedDate = nextExpected,
            lastSeenDate     = lastDate,
            occurrenceCount  = sorted.size
        )
    }

    private fun normalizeName(tx: Transaction): String {
        val raw = tx.merchantName?.takeIf { it.isNotBlank() } ?: tx.description
        return raw.lowercase().trim().replace(TRAILING_NUMBERS, "").trim()
    }

    private fun classifyFrequency(medianDays: Long): RecurrenceFrequency? = when (medianDays) {
        in 6L..8L    -> RecurrenceFrequency.WEEKLY
        in 27L..33L  -> RecurrenceFrequency.MONTHLY
        in 88L..95L  -> RecurrenceFrequency.QUARTERLY
        in 360L..370L -> RecurrenceFrequency.YEARLY
        else         -> null
    }

    private fun medianLong(values: List<Long>): Long {
        val s = values.sorted()
        val mid = s.size / 2
        return if (s.size % 2 == 0) (s[mid - 1] + s[mid]) / 2 else s[mid]
    }
}

fun DetectedSubscription.monthlyCost(): Long = when (frequency) {
    RecurrenceFrequency.WEEKLY     -> (estimatedAmount * 4.33).toLong()
    RecurrenceFrequency.MONTHLY    -> estimatedAmount
    RecurrenceFrequency.QUARTERLY  -> estimatedAmount / 3
    RecurrenceFrequency.YEARLY     -> estimatedAmount / 12
}

fun DetectedSubscription.annualCost(): Long = when (frequency) {
    RecurrenceFrequency.WEEKLY     -> estimatedAmount * 52
    RecurrenceFrequency.MONTHLY    -> estimatedAmount * 12
    RecurrenceFrequency.QUARTERLY  -> estimatedAmount * 4
    RecurrenceFrequency.YEARLY     -> estimatedAmount
}

fun nextFutureOccurrence(lastDate: Long, frequency: RecurrenceFrequency): Long {
    val now = System.currentTimeMillis()
    var next = lastDate
    while (next <= now) {
        next = advanceByFrequency(next, frequency)
    }
    return next
}

private fun advanceByFrequency(from: Long, frequency: RecurrenceFrequency): Long {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = from }
    when (frequency) {
        RecurrenceFrequency.WEEKLY     -> cal.add(java.util.Calendar.DAY_OF_YEAR, 7)
        RecurrenceFrequency.MONTHLY    -> cal.add(java.util.Calendar.MONTH, 1)
        RecurrenceFrequency.QUARTERLY  -> cal.add(java.util.Calendar.MONTH, 3)
        RecurrenceFrequency.YEARLY     -> cal.add(java.util.Calendar.YEAR, 1)
    }
    return cal.timeInMillis
}
