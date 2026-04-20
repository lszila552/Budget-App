package com.vrijgeld.data.repository

import com.vrijgeld.data.db.dao.TransactionDao
import com.vrijgeld.data.model.Transaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(private val dao: TransactionDao) {
    fun getByDateRange(start: Long, end: Long) = dao.getByDateRange(start, end)
    fun getForMonth(yearMonth: String) = dao.getForMonth(yearMonth)
    fun getUncategorized() = dao.getUncategorized()
    fun getByCategory(catId: Long, yearMonth: String) = dao.getByCategory(catId, yearMonth)
    fun countUncategorizedImported() = dao.countUncategorizedImported()
    fun getRecentCategoryIds() = dao.getRecentCategoryIds()
    fun getUncategorizedImported() = dao.getUncategorizedImported()
    suspend fun insert(t: Transaction) = dao.insert(t)
    suspend fun insertAll(ts: List<Transaction>) = dao.insertAll(ts)
    suspend fun update(t: Transaction) = dao.update(t)
    suspend fun delete(t: Transaction) = dao.delete(t)
    suspend fun getByImportHash(hash: String) = dao.getByImportHash(hash)
    suspend fun getForMonthOnce(yearMonth: String) = dao.getForMonthOnce(yearMonth)
    suspend fun getByCategoryYearMonths(catId: Long, yearMonths: List<String>) = dao.getByCategoryYearMonths(catId, yearMonths)
    suspend fun getAllOnce() = dao.getAllOnce()
    suspend fun getAllExpensesOnce() = dao.getAllExpensesOnce()
    suspend fun getRecurringOnce() = dao.getRecurringOnce()
    suspend fun getRecentExpensesSince(since: Long) = dao.getRecentExpensesSince(since)
    suspend fun getCategoryExpensesSince(catId: Long, since: Long) = dao.getCategoryExpensesSince(catId, since)
    suspend fun getAccountBalance(accountId: Long) = dao.getAccountBalance(accountId)
}
