package com.example.dayplanner.tags

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "note_tag")
data class NoteTag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int,
    val tagId: Int
)


