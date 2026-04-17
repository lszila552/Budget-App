package com.vrijgeld.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "rules", indices = [Index("priority")])
data class CategorizationRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val field: RuleField,
    val matchType: MatchType,
    val pattern: String,
    val priority: Int = 0
)

enum class RuleField { DESCRIPTION, MERCHANT, COUNTERPARTY_IBAN }
enum class MatchType  { CONTAINS, STARTS_WITH, EQUALS, REGEX }
