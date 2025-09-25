package com.example.dayplanner.features

import android.content.Context
import com.example.dayplanner.utils.CustomToast
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern not uygulamalarından popüler özellikler
 */
object ModernFeatures {

    /**
     * Not öncelik seviyeleri
     */
    enum class Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    /**
     * Not kategorileri
     */
    enum class Category {
        PERSONAL, WORK, SHOPPING, IDEAS, MEETINGS, TRAVEL, HEALTH, FINANCE, OTHER
    }

    /**
     * Not durumları
     */
    enum class Status {
        DRAFT, ACTIVE, COMPLETED, ARCHIVED, DELETED
    }

    /**
     * Hatırlatma türleri
     */
    enum class ReminderType {
        NONE, EXACT_TIME, LOCATION_BASED, CONTEXT_AWARE
    }

    /**
     * Not şablonları
     */
    object Templates {
        const val MEETING = "Toplantı Notu"
        const val SHOPPING = "Alışveriş Listesi"
        const val TODO = "Yapılacaklar Listesi"
        const val IDEA = "Fikir Notu"
        const val JOURNAL = "Günlük Notu"
        const val RECIPE = "Tarif Notu"
        const val TRAVEL = "Seyahat Notu"
        const val PROJECT = "Proje Notu"
    }

    /**
     * Hızlı etiketler
     */
    object QuickTags {
        val COMMON_TAGS = listOf(
            "önemli", "acil", "iş", "kişisel", "alışveriş", 
            "toplantı", "fikir", "proje", "seyahat", "sağlık"
        )
    }

    /**
     * Not istatistikleri
     */
    data class NoteStats(
        val totalNotes: Int,
        val completedNotes: Int,
        val pinnedNotes: Int,
        val lockedNotes: Int,
        val notesThisWeek: Int,
        val notesThisMonth: Int,
        val mostUsedTags: List<String>,
        val averageNoteLength: Int
    )

    /**
     * Akıllı öneriler
     */
    object SmartSuggestions {
        fun getTimeBasedGreeting(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> "Günaydın! Bugün neler yapacaksınız?"
                in 12..17 -> "İyi günler! Öğleden sonra planlarınız neler?"
                in 18..22 -> "İyi akşamlar! Günün özetini çıkaralım mı?"
                else -> "Gece notları için hazır mısınız?"
            }
        }

        fun getMotivationalMessage(): String {
            val messages = listOf(
                "Her not, bir adım daha ileriye!",
                "Bugün de harika notlar yazacaksınız!",
                "Fikirlerinizi kaydetmek, onları gerçekleştirmenin ilk adımıdır.",
                "Not almak, hafızanızı güçlendirir.",
                "Her not, gelecekteki sizin için bir hediye!"
            )
            return messages.random()
        }
    }

    /**
     * Not formatları
     */
    object NoteFormats {
        fun createMeetingNote(title: String, date: String, participants: List<String>): String {
            return """
                # $title
                
                **Tarih:** $date
                **Katılımcılar:** ${participants.joinToString(", ")}
                
                ## Gündem
                - 
                - 
                - 
                
                ## Kararlar
                - 
                - 
                
                ## Eylemler
                - [ ] 
                - [ ] 
                - [ ] 
            """.trimIndent()
        }

        fun createShoppingList(items: List<String>): String {
            return """
                # Alışveriş Listesi
                
                ${items.map { "- [ ] $it" }.joinToString("\n")}
                
                **Toplam:** ${items.size} ürün
            """.trimIndent()
        }

        fun createTodoList(title: String, tasks: List<String>): String {
            return """
                # $title
                
                ${tasks.map { "- [ ] $it" }.joinToString("\n")}
                
                **İlerleme:** 0/${tasks.size} tamamlandı
            """.trimIndent()
        }
    }

    /**
     * Not arama önerileri
     */
    object SearchSuggestions {
        fun getSearchTips(): List<String> {
            return listOf(
                "Etiketlerle arama: #iş, #önemli",
                "Tarihle arama: bugün, dün, bu hafta",
                "Durumla arama: tamamlandı, bekliyor",
                "Öncelikle arama: yüksek, düşük",
                "Kategoriyle arama: iş, kişisel, alışveriş"
            )
        }
    }

    /**
     * Not yedekleme ve senkronizasyon
     */
    object BackupFeatures {
        fun getBackupInfo(): String {
            return """
                **Otomatik Yedekleme Özellikleri:**
                • Her 6 saatte bir otomatik yedekleme
                • Bulut senkronizasyonu
                • Şifreli yedekleme
                • Çoklu cihaz desteği
                • Sürüm geçmişi
            """.trimIndent()
        }
    }

    /**
     * Not paylaşım özellikleri
     */
    object SharingFeatures {
        fun getSharingOptions(): List<String> {
            return listOf(
                "Metin olarak paylaş",
                "PDF olarak dışa aktar",
                "E-posta ile gönder",
                "Sosyal medyada paylaş",
                "QR kod oluştur",
                "Yazdır"
            )
        }
    }

    /**
     * Not analitikleri
     */
    object Analytics {
        fun calculateProductivityScore(notes: List<Any>): Int {
            // Basit verimlilik skoru hesaplama
            return when {
                notes.size > 50 -> 100
                notes.size > 30 -> 80
                notes.size > 20 -> 60
                notes.size > 10 -> 40
                else -> 20
            }
        }

        fun getProductivityTips(score: Int): List<String> {
            return when {
                score >= 80 -> listOf(
                    "Harika! Çok verimli bir kullanıcısınız.",
                    "Notlarınızı kategorilere ayırmayı deneyin.",
                    "Hatırlatmaları aktif kullanın."
                )
                score >= 60 -> listOf(
                    "İyi gidiyorsunuz! Daha fazla not ekleyin.",
                    "Etiketleri kullanmayı deneyin.",
                    "Günlük not alışkanlığı edinin."
                )
                else -> listOf(
                    "Daha fazla not ekleyerek başlayın.",
                    "Basit notlar yazmaya odaklanın.",
                    "Hatırlatma özelliklerini kullanın."
                )
            }
        }
    }
}
