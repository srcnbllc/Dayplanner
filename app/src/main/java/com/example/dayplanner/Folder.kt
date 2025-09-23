package com.example.dayplanner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_table")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)


