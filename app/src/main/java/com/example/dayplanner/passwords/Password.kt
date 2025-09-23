package com.example.dayplanner.passwords

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class Password(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val username: String,
    val password: String, // This will be encrypted before storage
    val category: String,
    val website: String? = null,
    val notes: String? = null
)

@Entity(tableName = "password_categories")
data class PasswordCategory(
    @PrimaryKey val name: String
)




