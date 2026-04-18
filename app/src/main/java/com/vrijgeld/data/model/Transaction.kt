package com.vrijgeld.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Account::class,  parentColumns = ["id"], childColumns = ["accountId"]),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"])
    ],
    indices = [
        Index("accountId"), Index("categoryId"), Index("date"),
        Index("merchantName"), Index("isRecurring")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val categoryId: Long? = null,
    val amount: Long,                       // cents; positive = income, negative = expense
    val date: Long,                         // epoch millis
    val description: String,
    val merchantName: String? = null,
    val counterpartyIban: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: RecurrenceFrequency? = null,
    val importSource: ImportSource? = null,
    val importHash: String? = null,
    val isReviewed: Boolean = false,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RecurrenceFrequency { WEEKLY, MONTHLY, QUARTERLY, YEARLY }
enum class ImportSource { MANUAL, CAMT053, MT940, CSV, BUNQ_API }
