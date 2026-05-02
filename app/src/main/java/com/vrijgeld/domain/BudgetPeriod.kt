package com.vrijgeld.domain

import java.util.Calendar

const val BUDGET_START_DAY = 24

data class BudgetPeriod(
    val yearMonth: String,
    val startMs: Long,
    val endMs: Long,
    val dayOfPeriod: Int,
    val daysInPeriod: Int
)

fun currentBudgetPeriod(now: Long = System.currentTimeMillis()): BudgetPeriod {
    val cal = Calendar.getInstance().apply { timeInMillis = now }
    val day = cal.get(Calendar.DAY_OF_MONTH)

    val startCal = Calendar.getInstance().apply {
        timeInMillis = now
        if (day < BUDGET_START_DAY) add(Calendar.MONTH, -1)
        set(Calendar.DAY_OF_MONTH, BUDGET_START_DAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCal = Calendar.getInstance().apply {
        timeInMillis = startCal.timeInMillis
        add(Calendar.MONTH, 1)
        add(Calendar.DAY_OF_MONTH, -1)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    val yearMonth = "%04d-%02d".format(
        startCal.get(Calendar.YEAR),
        startCal.get(Calendar.MONTH) + 1
    )
    val daysInPeriod = ((endCal.timeInMillis - startCal.timeInMillis) / 86_400_000L + 1).toInt()
    val dayOfPeriod  = ((now - startCal.timeInMillis) / 86_400_000L + 1).toInt()
        .coerceIn(1, daysInPeriod)

    return BudgetPeriod(yearMonth, startCal.timeInMillis, endCal.timeInMillis, dayOfPeriod, daysInPeriod)
}

fun previousBudgetPeriod(current: BudgetPeriod): BudgetPeriod = currentBudgetPeriod(current.startMs - 1L)
