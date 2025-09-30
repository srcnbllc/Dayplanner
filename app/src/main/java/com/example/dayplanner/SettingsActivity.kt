package com.example.dayplanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.example.dayplanner.utils.CustomToast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dayplanner.backup.DataExportImportManager
import com.example.dayplanner.databinding.ActivitySettingsBinding
import com.example.dayplanner.theme.ThemeManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var exportImportManager: DataExportImportManager
    private lateinit var themeManager: ThemeManager

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportData(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importData(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exportImportManager = DataExportImportManager(this)
        themeManager = ThemeManager(this)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Ayarlar"
    }

    private fun setupClickListeners() {
        binding.exportDataButton.setOnClickListener {
            val fileName = "dayplanner_backup_${System.currentTimeMillis()}.json"
            exportLauncher.launch(fileName)
        }

        binding.importDataButton.setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        binding.aboutButton.setOnClickListener {
            // Show about dialog
            showAboutDialog()
        }

        binding.themeButton.setOnClickListener {
            // Show theme selection dialog
            showThemeDialog()
        }

        binding.backupButton.setOnClickListener {
            // Show backup options
            showBackupDialog()
        }
    }

    private fun exportData(uri: Uri) {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.exportDataButton.isEnabled = false

            val success = exportImportManager.exportAllData(uri)

            binding.progressBar.visibility = android.view.View.GONE
            binding.exportDataButton.isEnabled = true

            if (success) {
                CustomToast.show(this@SettingsActivity, "Veriler dışa aktarıldı")
            } else {
                CustomToast.show(this@SettingsActivity, "Dışa aktarma başarısız")
            }
        }
    }

    private fun importData(uri: Uri) {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.importDataButton.isEnabled = false

            val success = exportImportManager.importAllData(uri)

            binding.progressBar.visibility = android.view.View.GONE
            binding.importDataButton.isEnabled = true

            if (success) {
                CustomToast.show(this@SettingsActivity, "Veriler içe aktarıldı")
            } else {
                CustomToast.show(this@SettingsActivity, "İçe aktarma başarısız")
            }
        }
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Dayplanner Hakkında")
            .setMessage("Dayplanner v1.0\n\nGünlük planlama, finans yönetimi ve şifre saklama uygulaması.\n\nGeliştirici: Dayplanner Team")
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun showThemeDialog() {
        val themes = themeManager.getAvailableThemes()
        val themeNames = themes.map { it.name }.toTypedArray()
        val currentTheme = themeManager.getCurrentTheme()
        val selectedIndex = themes.indexOfFirst { it.id == currentTheme }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tema Seçin")
            .setSingleChoiceItems(themeNames, selectedIndex) { _, which ->
                val selectedTheme = themes[which]
                themeManager.setTheme(selectedTheme.id)
                CustomToast.show(this, "Tema değiştirildi: ${selectedTheme.name}")
            }
            .setNegativeButton("Kapat", null)
            .show()
    }

    private fun showBackupDialog() {
        val options = arrayOf("Google Drive", "Yerel Yedekleme", "Otomatik Yedekleme")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Yedekleme Seçenekleri")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> CustomToast.show(this, "Google Drive yedekleme yakında eklenecek")
                    1 -> CustomToast.show(this, "Yerel yedekleme seçildi")
                    2 -> CustomToast.show(this, "Otomatik yedekleme ayarları yakında eklenecek")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
