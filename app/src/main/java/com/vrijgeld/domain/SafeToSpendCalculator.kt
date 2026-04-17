package com.vrijgeld.domain

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeToSpendCalculator @Inject constructor() {

    data class Result(val monthly: Long, val daily: Long)

    fun calculate(
        monthlyIncome: Long,
        spentThisMonth: Long,
        dayOfMonth: Int,
        daysInMonth: Int
    ): Result {
        val remaining = monthlyIncome - spentThisMonth
        val daysLeft  = maxOf(1, daysInMonth - dayOfMonth + 1)
        return Result(monthly = remaining, daily = remaining / daysLeft)
    }
}
