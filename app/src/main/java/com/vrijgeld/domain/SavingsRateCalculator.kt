package com.vrijgeld.domain

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsRateCalculator @Inject constructor() {

    /** Returns a value in [0, 1+] where 0.40 means 40% savings rate. */
    fun calculate(income: Long, totalExpenses: Long): Float {
        if (income <= 0L) return 0f
        return ((income - totalExpenses).toFloat() / income.toFloat()).coerceAtLeast(0f)
    }
}
