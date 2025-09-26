package com.example.dayplanner.finance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_record")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String? = null,
    val amount: Double = 0.0,
    val date: Long = 0L,
    val note: String? = null,
    val isIncome: Boolean = false
)

