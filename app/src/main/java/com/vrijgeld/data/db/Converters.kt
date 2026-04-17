package com.vrijgeld.data.db

import androidx.room.TypeConverter
import com.vrijgeld.data.model.*

class Converters {
    @TypeConverter fun fromAccountType(v: AccountType): String = v.name
    @TypeConverter fun toAccountType(v: String): AccountType = AccountType.valueOf(v)

    @TypeConverter fun fromCategoryType(v: CategoryType): String = v.name
    @TypeConverter fun toCategoryType(v: String): CategoryType = CategoryType.valueOf(v)

    @TypeConverter fun fromRuleField(v: RuleField): String = v.name
    @TypeConverter fun toRuleField(v: String): RuleField = RuleField.valueOf(v)

    @TypeConverter fun fromMatchType(v: MatchType): String = v.name
    @TypeConverter fun toMatchType(v: String): MatchType = MatchType.valueOf(v)

    @TypeConverter fun fromRecurrenceFrequency(v: RecurrenceFrequency?): String? = v?.name
    @TypeConverter fun toRecurrenceFrequency(v: String?): RecurrenceFrequency? = v?.let { RecurrenceFrequency.valueOf(it) }

    @TypeConverter fun fromImportSource(v: ImportSource?): String? = v?.name
    @TypeConverter fun toImportSource(v: String?): ImportSource? = v?.let { ImportSource.valueOf(it) }
}
