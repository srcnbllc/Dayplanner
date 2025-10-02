package com.example.dayplanner.finance

// Room temporarily disabled due to annotation processor issues
// This file will be re-enabled once Room issues are resolved

/*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_record")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val currency: String = "TRY",
    val date: Long,
    val description: String? = null,
    val category: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installment")
data class Installment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val financeRecordId: Int,
    val amount: Double,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val paidDate: Long? = null
)

@Entity(tableName = "payment_reminder")
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val financeRecordId: Int,
    val reminderTime: Long,
    val isTriggered: Boolean = false
)
*/