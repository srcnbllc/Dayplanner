package com.example.dayplanner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_log_table")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val action: String,
    val description: String,
    val noteId: Int? = null,
    val noteTitle: String? = null,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)