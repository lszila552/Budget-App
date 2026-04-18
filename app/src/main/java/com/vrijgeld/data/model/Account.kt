package com.vrijgeld.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String = "EUR",
    val currentBalance: Long,
    val institution: String? = null,
    val iban: String? = null,
    val includeInNetWorth: Boolean = true,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AccountType { CHECKING, SAVINGS, INVESTMENT, PROPERTY, LIABILITY }
