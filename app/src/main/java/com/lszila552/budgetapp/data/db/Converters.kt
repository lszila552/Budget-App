package com.lszila552.budgetapp.data.db

import androidx.room.TypeConverter
import com.lszila552.budgetapp.data.entity.AccountType

class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)
}
