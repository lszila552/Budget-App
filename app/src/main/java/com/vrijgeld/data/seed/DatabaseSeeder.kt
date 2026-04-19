package com.vrijgeld.data.seed

import com.vrijgeld.data.db.dao.AccountDao
import com.vrijgeld.data.db.dao.CategoryDao
import com.vrijgeld.data.db.dao.RuleDao
import com.vrijgeld.data.db.dao.SettingDao
import com.vrijgeld.data.model.AppSetting
import com.vrijgeld.data.model.CategorizationRule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val categoryDao: CategoryDao,
    private val ruleDao: RuleDao,
    private val settingDao: SettingDao,
    private val accountDao: AccountDao,
) {
    suspend fun seedIfNeeded() {
        if (accountDao.getActiveOnce().isEmpty()) {
            accountDao.insert(DEFAULT_ACCOUNT)
        }

        if (settingDao.get("seeded")?.value == "true") return

        categoryDao.insertAll(DEFAULT_CATEGORIES)
        val nameToId = categoryDao.getAllOnce().associate { it.name to it.id }

        val rules = DUTCH_RULE_SPECS.mapNotNull { spec ->
            nameToId[spec.categoryName]?.let { catId ->
                CategorizationRule(categoryId = catId, field = spec.field, matchType = spec.matchType, pattern = spec.pattern)
            }
        }
        ruleDao.insertAll(rules)

        settingDao.insertAll(DEFAULT_SETTINGS.map { (k, v) -> AppSetting(k, v) })
        settingDao.set(AppSetting("seeded", "true"))
    }
}
