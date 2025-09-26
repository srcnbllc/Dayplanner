package com.example.dayplanner.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.dayplanner.Note
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object SmartTemplates {
    
    private const val PREFS_NAME = "smart_templates"
    private const val KEY_TEMPLATES = "templates"
    
    // Önceden tanımlanmış şablonlar
    private val defaultTemplates = listOf(
        NoteTemplate(
            name = "Günlük Plan",
            title = "Günlük Plan - {date}",
            description = "🌅 Sabah:\n- \n\n🌞 Öğle:\n- \n\n🌙 Akşam:\n- ",
            tags = "plan,günlük"
        ),
        NoteTemplate(
            name = "Toplantı Notları",
            title = "Toplantı - {date}",
            description = "📅 Tarih: {date}\n👥 Katılımcılar:\n- \n\n📝 Gündem:\n1. \n2. \n3. \n\n✅ Aksiyonlar:\n- ",
            tags = "toplantı,iş"
        ),
        NoteTemplate(
            name = "Alışveriş Listesi",
            title = "Alışveriş - {date}",
            description = "🛒 Alışveriş Listesi:\n\n🥬 Sebze:\n- \n\n🥩 Et:\n- \n\n🥛 Süt Ürünleri:\n- \n\n🍞 Fırın:\n- ",
            tags = "alışveriş,liste"
        ),
        NoteTemplate(
            name = "Seyahat Planı",
            title = "Seyahat - {destination}",
            description = "✈️ Seyahat Planı\n\n📍 Nereye: {destination}\n📅 Ne Zaman: {date}\n\n🎒 Çanta:\n- \n\n🏨 Konaklama:\n- \n\n🚗 Ulaşım:\n- ",
            tags = "seyahat,plan"
        ),
        NoteTemplate(
            name = "Proje Notları",
            title = "Proje - {project_name}",
            description = "📋 Proje Detayları\n\n🎯 Hedef:\n\n📅 Deadline: \n\n👥 Ekip:\n- \n\n📝 Görevler:\n- [ ] \n- [ ] \n- [ ] ",
            tags = "proje,iş"
        ),
        NoteTemplate(
            name = "Fikir Notları",
            title = "Fikir - {date}",
            description = "💡 Fikir Notları\n\n🎯 Ana Fikir:\n\n📝 Detaylar:\n\n🔗 Kaynaklar:\n- \n\n📅 Takip Edilecek Aksiyonlar:\n- ",
            tags = "fikir,not"
        )
    )
    
    fun getTemplates(context: Context): List<NoteTemplate> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val templatesJson = prefs.getString(KEY_TEMPLATES, null)
        
        return if (templatesJson != null) {
            try {
                // JSON'dan parse et (basit implementasyon)
                defaultTemplates
            } catch (e: Exception) {
                defaultTemplates
            }
        } else {
            defaultTemplates
        }
    }
    
    fun saveTemplate(context: Context, template: NoteTemplate) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentTemplates = getTemplates(context).toMutableList()
        currentTemplates.add(template)
        
        // Basit JSON serialization (gerçek uygulamada Gson kullanılabilir)
        val templatesJson = currentTemplates.joinToString("|") { "${it.name}|||${it.title}|||${it.description}|||${it.tags}" }
        prefs.edit().putString(KEY_TEMPLATES, templatesJson).apply()
    }
    
    fun getTemplateByName(context: Context, name: String): NoteTemplate? {
        return getTemplates(context).find { it.name == name }
    }
    
    fun applyTemplate(template: NoteTemplate, customValues: Map<String, String> = emptyMap()): Note {
        var title = template.title
        var description = template.description
        
        // Placeholder'ları değiştir
        customValues.forEach { (key, value) ->
            title = title.replace("{$key}", value)
            description = description.replace("{$key}", value)
        }
        
        // Varsayılan değerler
        title = title.replace("{date}", java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()))
        description = description.replace("{date}", java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()))
        
        val currentDateTime = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        
        return Note(
            title = title,
            description = description,
            date = currentDateTime.format(dateFormatter),
            tags = template.tags,
            createdAt = System.currentTimeMillis(),
            isPinned = false,
            isEncrypted = false,
            isLocked = false,
            status = "NEW"
        )
    }
}

data class NoteTemplate(
    val name: String,
    val title: String,
    val description: String,
    val tags: String
)
