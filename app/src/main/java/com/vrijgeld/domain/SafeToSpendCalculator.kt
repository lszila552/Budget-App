package com.vrijgeld.domain

import javax.inject.Inject
import javax.inject.Singleton

val FIXED_CATEGORY_NAMES = setOf("Housing", "Insurance", "Utilities", "Subscriptions", "Taxes", "Education Debt")

@Singleton
class SafeToSpendCalculator @Inject constructor() {

    data class Result(val monthly: Long, val daily: Long)

    data class CalcInput(
        val incomeThisMonth: Long,
        val fixedBudgets: Map<Long, Long>,      // catId → monthlyBudget (cents)
        val fixedSpent: Map<Long, Long>,         // catId → spent this month (cents, positive)
        val sinkingContributions: Long,          // sum(sinkingFundTarget / 12)
        val variableSpent: Long,                 // all non-fixed, non-sinking expenses
        val dayOfMonth: Int,
        val daysInMonth: Int
    )

    fun calculate(input: CalcInput): Result {
        val fixedRemaining = input.fixedBudgets.entries.sumOf { (id, budget) ->
            val spent = input.fixedSpent[id] ?: 0L
            maxOf(0L, budget - spent)
        }
        val monthly = input.incomeThisMonth - fixedRemaining - input.sinkingContributions - input.variableSpent
        val daysLeft = maxOf(1, input.daysInMonth - input.dayOfMonth + 1)
        return Result(monthly = monthly, daily = monthly / daysLeft)
    }

    fun calculate(monthlyIncome: Long, spentThisMonth: Long, dayOfMonth: Int, daysInMonth: Int): Result {
        val remaining = monthlyIncome - spentThisMonth
        val daysLeft  = maxOf(1, daysInMonth - dayOfMonth + 1)
        return Result(monthly = remaining, daily = remaining / daysLeft)
    }
}
