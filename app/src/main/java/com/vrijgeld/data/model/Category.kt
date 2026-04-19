package com.vrijgeld.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val type: CategoryType,
    val monthlyBudget: Long? = null,
    val isSinkingFund: Boolean = false,
    val sinkingFundTarget: Long? = null,
    val rollover: Boolean = false,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val accountId: Long? = null
)

enum class CategoryType { EXPENSE, INCOME, SAVINGS }
