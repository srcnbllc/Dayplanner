package com.example.dayplanner.tags

// Room temporarily disabled due to annotation processor issues
// This file will be re-enabled once Room issues are resolved

/*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "note_tag")
data class NoteTag(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int,
    val tagId: Int,
    val createdAt: Long = System.currentTimeMillis()
)
*/