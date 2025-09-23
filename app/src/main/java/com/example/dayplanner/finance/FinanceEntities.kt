package com.example.dayplanner.finance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_record")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // debt, loan, recurring
    val title: String,
    val amount: Double,
    val startDate: String,
    val endDate: String? = null
)

@Entity(tableName = "installment")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val financeRecordId: Int,
    val dueDate: String,
    val amount: Double,
    val isPaid: Boolean = false
)

@Entity(tableName = "payment_reminder")
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val financeRecordId: Int,
    val daysBefore: Int
)


