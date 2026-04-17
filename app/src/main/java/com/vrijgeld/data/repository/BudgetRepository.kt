package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.AllocationDao
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.model.MonthlyAllocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val allocationDao: AllocationDao,
    private val categoryDao: CategoryDao
) {
    fun getExpenseCategories() = categoryDao.getExpenseCategories()
    fun getIncomeCategories() = categoryDao.getIncomeCategories()
    fun getSinkingFunds() = categoryDao.getSinkingFunds()
    fun getAllocationsForMonth(yearMonth: String) = allocationDao.getForMonth(yearMonth)
    suspend fun upsertAllocation(a: MonthlyAllocation) = allocationDao.upsert(a)
    suspend fun getAllCategoriesOnce() = categoryDao.getAllOnce()
}
