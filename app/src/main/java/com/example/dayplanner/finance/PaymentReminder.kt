package com.example.dayplanner.finance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_reminder")
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: Int,
    val remindAt: Long = 0L,
    val message: String? = null
)

