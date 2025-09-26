package com.example.dayplanner.finance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installment")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: Int,
    val dueDate: Long = 0L,
    val amount: Double = 0.0,
    val isPaid: Boolean = false
)

