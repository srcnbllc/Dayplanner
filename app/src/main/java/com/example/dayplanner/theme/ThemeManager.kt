package com.example.dayplanner.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_KEY = "selected_theme"
    }
    
    fun getCurrentTheme(): String {
        return prefs.getString(THEME_KEY, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    fun setTheme(theme: String) {
        prefs.edit().putString(THEME_KEY, theme).apply()
        applyTheme(theme)
    }
    
    fun applyTheme(theme: String) {
        val mode = when (theme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    fun applyCurrentTheme() {
        applyTheme(getCurrentTheme())
    }
    
    fun getAvailableThemes(): List<ThemeOption> {
        return listOf(
            ThemeOption(THEME_SYSTEM, "Sistem Teması", "Cihaz ayarlarını takip eder"),
            ThemeOption(THEME_LIGHT, "Açık Tema", "Her zaman açık tema kullan"),
            ThemeOption(THEME_DARK, "Koyu Tema", "Her zaman koyu tema kullan")
        )
    }
}

data class ThemeOption(
    val id: String,
    val name: String,
    val description: String
)
