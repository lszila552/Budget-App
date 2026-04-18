package com.vrijgeld.domain

import kotlin.math.ln
import kotlin.math.pow

object FIRECalculator {

    // FI number = annual_expenses × (100 / swr%)
    fun fiNumber(annualExpensesCents: Long, swrPercent: Float): Long =
        (annualExpensesCents * (100.0 / swrPercent)).toLong()

    // Years to FI: n = ln((FI×r + s) / (P×r + s)) / ln(1+r)
    // P = current portfolio, s = annual savings, r = real return
    fun yearsToFi(
        fiNumberCents: Long,
        portfolioCents: Long,
        annualSavingsCents: Long,
        realReturn: Double
    ): Double {
        val fi = fiNumberCents.toDouble()
        val p  = portfolioCents.toDouble()
        val s  = annualSavingsCents.toDouble()
        val r  = realReturn / 100.0   // convert percent to decimal

        if (p >= fi) return 0.0
        if (r == 0.0) return if (s > 0) (fi - p) / s else Double.MAX_VALUE

        val num = fi * r + s
        val den = p * r + s
        if (den <= 0 || num <= 0 || num <= den) return Double.MAX_VALUE

        return ln(num / den) / ln(1.0 + r)
    }

    // Coast FI = FI_number / (1+r)^(retirementAge - currentAge)
    fun coastFi(
        fiNumberCents: Long,
        currentAge: Int,
        retirementAge: Int = 67,
        realReturn: Double
    ): Long {
        val years = (retirementAge - currentAge).coerceAtLeast(0)
        val r = realReturn / 100.0
        return (fiNumberCents / (1.0 + r).pow(years.toDouble())).toLong()
    }

    // Financial Freedom Runway = liquid assets / monthly expenses (months)
    fun ffRunway(liquidAssetsCents: Long, monthlyExpensesCents: Long): Double =
        if (monthlyExpensesCents > 0) liquidAssetsCents.toDouble() / monthlyExpensesCents else 0.0

    // Savings rate = (income - expenses) / income  [0..1]
    fun savingsRate(incomeCents: Long, expensesCents: Long): Float =
        if (incomeCents > 0) ((incomeCents - expensesCents).toFloat() / incomeCents).coerceIn(0f, 1f) else 0f

    // Scenario: years to FI with extra monthly savings
    fun yearsToFiWithExtra(
        fiNumberCents: Long,
        portfolioCents: Long,
        annualSavingsCents: Long,
        extraMonthlyCents: Long,
        realReturn: Double
    ): Double = yearsToFi(
        fiNumberCents,
        portfolioCents,
        annualSavingsCents + extraMonthlyCents * 12L,
        realReturn
    )
}
