package com.example.dayplanner

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Otomatik artan birincil anahtar
    val title: String,                                 // Not başlığı
    val description: String,                           // Not açıklaması
    val date: String                                    // Not tarihi (yyyy-MM-dd formatında)
) {
    // Tarihi daha kullanıcı dostu bir formata dönüştürme
    fun getFormattedDate(): String {
        return try {
            // String türündeki tarihi Date'e dönüştür
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate: Date = inputFormat.parse(date) ?: Date() // Geçerli değilse bugünün tarihini al
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Yeni format (dd/MM/yyyy)
            outputFormat.format(parsedDate) // Formatlı tarihi döndür
        } catch (e: Exception) {
            "No Date" // Hata durumunda "No Date" döndür
        }
    }
}
