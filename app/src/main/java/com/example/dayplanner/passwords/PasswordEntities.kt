package com.example.dayplanner.passwords

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_item")
data class PasswordItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val service: String,
    val username: String,
    val encryptedPassword: ByteArray,
    val backupEmail: String? = null,
    val securityKeyNote: String? = null,
    val lastChangedAt: String
)

@Entity(tableName = "password_history")
data class PasswordHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val passwordItemId: Int,
    val encryptedPassword: ByteArray,
    val changedAt: String
)


