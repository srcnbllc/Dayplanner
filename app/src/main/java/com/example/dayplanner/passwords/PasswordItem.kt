package com.example.dayplanner.passwords

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_item")
data class PasswordItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String? = null,
    val username: String? = null,
    val password: String? = null,
    val url: String? = null,
    val notes: String? = null
)

