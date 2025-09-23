package com.example.dayplanner.finance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val description: String = "",
    val type: TransactionType,
    val date: Long // Unix timestamp
)

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val name: String
)
