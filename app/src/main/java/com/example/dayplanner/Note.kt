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

    // Not oluşturulma tarihi (gösterim için string, sıralama için timestamp var)
    val date: String,

    val folderId: Int? = null,          // Notun klasör bilgisi
    val isPinned: Boolean = false,      // Üstte sabitlenmiş mi
    val isLocked: Boolean = false,      // Kilitlenmiş mi
    val isEncrypted: Boolean = false,   // Şifrelenmiş mi (UI için)
    val encryptedBlob: ByteArray? = null, // Şifrelenmiş içerik

    val imageUri: String? = null,       // Not ile eklenen görsel
    val recurrenceRule: String? = null, // Tekrarlama kuralı (örn: günlük, haftalık)
    val reminderMinutesBefore: Int? = null, // Hatırlatma için dakika bilgisi
    val tags: String? = null,           // Etiketler (virgülle ayrılmış)

    val status: String = "NEW",         // NEW, NOTES, DELETED
    val createdAt: Long = System.currentTimeMillis(), // Unix timestamp
    val deletedAt: Long? = null         // Silinme tarihi
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
