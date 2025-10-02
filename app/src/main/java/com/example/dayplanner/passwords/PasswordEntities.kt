package com.example.dayplanner.passwords

// Room temporarily disabled due to annotation processor issues
// This file will be re-enabled once Room issues are resolved

/*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_categories")
data class PasswordCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "password_item")
data class PasswordItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val service: String,
    val username: String,
    val password: String,
    val website: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "password_history")
data class PasswordHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val passwordItemId: Int,
    val oldPassword: String,
    val changedAt: Long = System.currentTimeMillis()
)
*/