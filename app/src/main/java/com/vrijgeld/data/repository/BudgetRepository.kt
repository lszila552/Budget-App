package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.AllocationDao
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.model.Category
import com.vrijgeld.data.model.MonthlyAllocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val allocationDao: AllocationDao,
    private val categoryDao: CategoryDao
) {
    fun getExpenseCategories()                         = categoryDao.getExpenseCategories()
    fun getIncomeCategories()                          = categoryDao.getIncomeCategories()
    fun getSinkingFunds()                              = categoryDao.getSinkingFunds()
    fun getAllocationsForMonth(yearMonth: String)      = allocationDao.getForMonth(yearMonth)
    suspend fun getAllCategoriesOnce()                 = categoryDao.getAllOnce()
    suspend fun getExpenseCategoriesOnce()             = categoryDao.getAllOnce().filter { it.type == com.vrijgeld.data.model.CategoryType.EXPENSE && it.isActive }
    suspend fun getAllocationsForMonthOnce(ym: String) = allocationDao.getForMonthOnce(ym)
    suspend fun getAllocationsForYearMonths(yms: List<String>) = allocationDao.getForYearMonthsOnce(yms)
    suspend fun getAllocationsForCategory(catId: Long) = allocationDao.getForCategoryOnce(catId)
    suspend fun upsertAllocation(a: MonthlyAllocation) = allocationDao.upsert(a)
    suspend fun upsertAllAllocations(list: List<MonthlyAllocation>) = allocationDao.upsertAll(list)
    suspend fun updateCategory(category: Category)     = categoryDao.update(category)
}
