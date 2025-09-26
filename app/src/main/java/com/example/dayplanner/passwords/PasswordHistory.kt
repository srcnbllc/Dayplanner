package com.example.dayplanner.passwords

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_history")
data class PasswordHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val previousPassword: String? = null,
    val changedAt: Long = 0L
)

