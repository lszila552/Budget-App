package com.vrijgeld.domain

import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.MonthlyAllocation

data class EnvelopeState(
    val category: Category,
    val allocated: Long,
    val carriedOver: Long,
    val spent: Long
) {
    val available: Long    get() = allocated + carriedOver
    val remaining: Long    get() = available - spent
    val isOverspent: Boolean get() = remaining < 0
    val overspendAmount: Long get() = if (isOverspent) -remaining else 0L
    val percentUsed: Float get() = if (available > 0) (spent.toFloat() / available).coerceAtLeast(0f) else 0f
}

object EnvelopeBudgetEngine {

    fun build(
        categories: List<Category>,
        allocations: List<MonthlyAllocation>,
        spentByCategory: Map<Long, Long>
    ): List<EnvelopeState> {
        val allocByCategory = allocations.associateBy { it.categoryId }
        return categories.map { cat ->
            val alloc = allocByCategory[cat.id]
            EnvelopeState(
                category    = cat,
                allocated   = alloc?.allocated   ?: cat.monthlyBudget ?: 0L,
                carriedOver = alloc?.carriedOver ?: 0L,
                spent       = spentByCategory[cat.id] ?: 0L
            )
        }
    }

    fun unallocated(totalIncome: Long, envelopes: List<EnvelopeState>): Long =
        totalIncome - envelopes.sumOf { it.allocated + it.carriedOver }

    fun transferAllocation(
        allocations: MutableList<MonthlyAllocation>,
        sourceId: Long,
        targetId: Long,
        amount: Long,
        yearMonth: String
    ): List<MonthlyAllocation> {
        fun findOrCreate(catId: Long): MonthlyAllocation =
            allocations.find { it.categoryId == catId }
                ?: MonthlyAllocation(categoryId = catId, yearMonth = yearMonth, allocated = 0L)

        val src = findOrCreate(sourceId)
        val tgt = findOrCreate(targetId)
        val transfer = amount.coerceAtMost(src.allocated)

        val updatedSrc = src.copy(allocated = (src.allocated - transfer).coerceAtLeast(0L))
        val updatedTgt = tgt.copy(allocated = tgt.allocated + transfer)

        return listOf(updatedSrc, updatedTgt)
    }
}

fun previousYearMonths(currentYearMonth: String, count: Int = 3): List<String> {
    val (y, m) = currentYearMonth.split("-").map { it.toInt() }
    return (1..count).map { offset ->
        val month = m - offset
        val year  = y + (month - 1) / 12 - if (month <= 0) 1 else 0
        val adj   = ((month - 1 + 12 * 10) % 12) + 1
        "%04d-%02d".format(year, adj)
    }.reversed()
}
