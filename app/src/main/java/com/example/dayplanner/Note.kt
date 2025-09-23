package com.example.dayplanner

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String,
    val folderId: Int? = null,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val encryptedBlob: ByteArray? = null,
    val imageUri: String? = null,
    val recurrenceRule: String? = null,
    val reminderMinutesBefore: Int? = null,
    val tags: String? = null,
    val status: String = "NEW", // NEW, NOTES, DELETED
    val createdAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null // Silme tarihi
) {
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate: Date = inputFormat.parse(date) ?: Date()
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            outputFormat.format(parsedDate)
        } catch (e: Exception) {
            "No Date"
        }
    }
}
