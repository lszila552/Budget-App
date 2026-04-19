package com.vrijgeld.data.importer

import com.vrijgeld.data.db.dao.RuleDao
import com.vrijgeld.data.model.CategorizationRule
import com.vrijgeld.data.model.MatchType
import com.vrijgeld.data.model.RuleField
import com.vrijgeld.data.model.Transaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorizationEngine @Inject constructor(private val ruleDao: RuleDao) {

    suspend fun categorize(transaction: Transaction): Long? =
        applyRules(transaction, ruleDao.getAllByPriorityOnce())

    fun applyRules(transaction: Transaction, rules: List<CategorizationRule>): Long? {
        val desc     = transaction.description.lowercase()
        val merchant = transaction.merchantName?.lowercase() ?: ""
        val iban     = transaction.counterpartyIban ?: ""
        for (rule in rules) {
            val text = when (rule.field) {
                RuleField.DESCRIPTION      -> desc
                RuleField.MERCHANT         -> merchant
                RuleField.COUNTERPARTY_IBAN -> iban
            }
            if (matches(text, rule.matchType, rule.pattern.lowercase())) return rule.categoryId
        }
        return null
    }

    private fun matches(text: String, type: MatchType, pattern: String) = when (type) {
        MatchType.CONTAINS    -> text.contains(pattern)
        MatchType.STARTS_WITH -> text.startsWith(pattern)
        MatchType.EQUALS      -> text == pattern
        MatchType.REGEX       -> runCatching { Regex(pattern).containsMatchIn(text) }.getOrDefault(false)
    }
}
