package com.example.dayplanner

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "activity_log_table")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String, // "CREATE_NOTE", "UPDATE_NOTE", "DELETE_NOTE", "RESTORE_NOTE", "ENCRYPT_NOTE", "DECRYPT_NOTE"
    val description: String, // "Yeni not oluşturuldu", "Not güncellendi", vb.
    val noteId: Int? = null, // İlgili not ID'si
    val noteTitle: String? = null, // Not başlığı
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null // Ek detaylar (JSON formatında)
) {
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
    
    fun getActionDisplayName(): String {
        return when (action) {
            "CREATE_NOTE" -> "Not Oluşturuldu"
            "UPDATE_NOTE" -> "Not Güncellendi"
            "DELETE_NOTE" -> "Not Silindi"
            "RESTORE_NOTE" -> "Not Geri Yüklendi"
            "ENCRYPT_NOTE" -> "Not Şifrelendi"
            "DECRYPT_NOTE" -> "Not Şifresi Kaldırıldı"
            "PIN_NOTE" -> "Not Sabitlendi"
            "UNPIN_NOTE" -> "Not Sabitlemesi Kaldırıldı"
            else -> action
        }
    }
}
