package com.example.dayplanner

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.example.dayplanner.utils.CustomToast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dayplanner.databinding.ActivityAddNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: NoteDatabase
    private var reminderEnabled: Boolean = false
    private var reminderMinutes: Int? = null
    private var editingNoteId: Int = -1
    
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabase.getDatabase(this)

        setupToolbar()
        setupButtons()
        setupDatePicker()
        setupReminderSystem()
        createNotificationChannel()
        
        // Eğer düzenleme modundaysa notu yükle
        editingNoteId = intent.getIntExtra("noteId", -1)
        if (editingNoteId != -1) {
            loadNoteForEditing(editingNoteId)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish() // Toolbar geri tuşu ile Notlar sayfasına dön
        }
    }

    private fun setupButtons() {
        // Vazgeç
        binding.cancelButton.setOnClickListener {
            finish() // Not eklemeden Notlar sayfasına dön
        }

        // Kaydet
        binding.saveButton.setOnClickListener {
            val note = collectNoteData()
            saveNote(note, encrypt = false, pin = null)
        }

        // Şifreli Kaydet
        binding.encryptButton.setOnClickListener {
            showPinDialog()
        }
    }

    private fun collectNoteData(): Note {
        val title = binding.titleEditText.text?.toString()?.trim() ?: ""
        val description = binding.descriptionEditText.text?.toString()?.trim() ?: ""
        val date = binding.dateEditText.text?.toString()?.trim() ?: ""
        val tags = binding.tagsEditText.text?.toString()?.trim() ?: ""

        reminderEnabled = binding.reminderCheckbox.isChecked
        reminderMinutes = if (reminderEnabled) {
            val reminderTimeText = binding.reminderTimeSpinner.text?.toString() ?: ""
            parseReminderMinutes(reminderTimeText)
        } else null

        return Note(
            title = title,
            description = description,
            date = date,
            tags = if (tags.isEmpty()) null else tags,
            reminderMinutesBefore = reminderMinutes,
            status = "NEW" // New notes start as "NEW" status
        )
    }

    private fun saveNote(note: Note, encrypt: Boolean, pin: String?) {
        // Boş alanları kontrol et
        if (note.title.isBlank()) {
            CustomToast.show(this, "Lütfen not başlığı girin")
            return
        }
        
        if (note.description.isBlank()) {
            CustomToast.show(this, "Lütfen not içeriği girin")
            return
        }
        
        lifecycleScope.launch {
            try {
                val noteToSave = if (encrypt && !pin.isNullOrEmpty()) {
                    try {
                        // Actually encrypt the content using PasswordManager
                        val encryptedContent = com.example.dayplanner.security.PasswordManager.encryptNote(note.description, pin)
                        note.copy(
                            isLocked = true,
                            isEncrypted = true,
                            description = "", // Clear the original content
                            encryptedBlob = encryptedContent.toByteArray()
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AddNoteActivity", "Encryption failed: ${e.message}", e)
                        CustomToast.show(this@AddNoteActivity, "Şifreleme hatası: ${e.message}")
                        return@launch
                    }
                } else note

                val savedNoteId = withContext(Dispatchers.IO) {
                    if (editingNoteId != -1) {
                        // Düzenleme modu
                        val updatedNote = noteToSave.copy(id = editingNoteId)
                        db.noteDao().update(updatedNote)
                        editingNoteId.toLong()
                    } else {
                        // Yeni not ekleme
                        db.noteDao().insert(noteToSave)
                    }
                }
                
                // Hatırlatma ayarla (UI thread'de)
                if (reminderEnabled && reminderMinutes != null) {
                    val reminderDate = binding.reminderDateText.text.toString()
                    val reminderTime = binding.reminderTimeSpinner.text.toString()
                    scheduleReminder(noteToSave.title, reminderDate, reminderTime, savedNoteId.toInt())
                }
                
                val message = if (editingNoteId != -1) {
                    if (encrypt) "Şifreli not güncellendi" else "Not güncellendi"
                } else {
                    if (encrypt) "Şifreli not kaydedildi" else "Not kaydedildi"
                }
                CustomToast.show(this@AddNoteActivity, message)
                finish() // Kaydettikten sonra Notlar sayfasına dön
            } catch (e: Exception) {
                android.util.Log.e("AddNoteActivity", "Not kaydetme hatası: ${e.message}", e)
                CustomToast.show(this@AddNoteActivity, "Not kaydedilemedi: ${e.message}")
            }
        }
    }

    private fun showPinDialog() {
        val passwordInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "6 haneli şifre girin"
            filters = arrayOf(InputFilter.LengthFilter(6))
            textSize = 24f
            gravity = android.view.Gravity.CENTER
            setPadding(50, 20, 50, 20)
        }

        AlertDialog.Builder(this)
            .setTitle("Şifreli Not")
            .setMessage("Bu notu şifrelemek için 6 haneli bir şifre belirleyin:")
            .setView(passwordInput)
            .setPositiveButton("Şifrele ve Kaydet") { dialog, _ ->
                val password = passwordInput.text.toString().trim()
                if (com.example.dayplanner.security.PasswordManager.isValidPassword(password)) {
                    val note = collectNoteData()
                    saveNote(note, encrypt = true, pin = password)
                } else {
                    val errorMessage = com.example.dayplanner.security.PasswordManager.getPasswordValidationError(password)
                    CustomToast.show(this, errorMessage ?: "Geçersiz şifre")
                }
                dialog.dismiss()
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupDatePicker() {
        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }
        }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.dateEditText.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupReminderSystem() {
        // Hatırlatma checkbox listener
        binding.reminderCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.reminderOptionsLayout.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Hatırlatma tarihi seçici
        binding.reminderDateText.setOnClickListener {
            showReminderDatePicker()
        }

        // Hatırlatma saati seçici
        binding.reminderTimeText.setOnClickListener {
            showTimePicker()
        }

        // Hatırlatma zamanı spinner
        val reminderTimeOptions = arrayOf(
            "5 dakika önce",
            "10 dakika önce", 
            "30 dakika önce",
            "1 saat önce",
            "2 saat önce",
            "1 gün önce"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, reminderTimeOptions)
        binding.reminderTimeSpinner.setAdapter(adapter)
        binding.reminderTimeSpinner.setText(reminderTimeOptions[0], false)
    }

    private fun showReminderDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.reminderDateText.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.reminderTimeText.setText(formattedTime)
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Hatırlatma Bildirimleri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Not hatırlatma bildirimleri"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleReminder(title: String, reminderDate: String, reminderTime: String, noteId: Int) {
        try {
            if (title.isBlank() || reminderDate.isBlank() || reminderTime.isBlank()) {
                CustomToast.show(this, "Hatırlatma bilgileri eksik")
                return
            }
            
            val reminderMinutes = parseReminderMinutes(reminderTime)
            if (reminderMinutes != null) {
                // Bildirim izni kontrolü
                if (checkNotificationPermission()) {
                    // Basit bildirim sistemi (gerçek uygulamada AlarmManager kullanılmalı)
                    CustomToast.showLong(this, "Hatırlatma ayarlandı: $title - $reminderDate $reminderTime")
                    
                    // Test için hemen bildirim göster
                    showNotification(title, "Hatırlatma: $reminderTime")
                } else {
                    // İzin iste
                    requestNotificationPermission()
                }
            } else {
                CustomToast.show(this, "Geçersiz hatırlatma zamanı")
            }
        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Hatırlatma ayarlama hatası: ${e.message}", e)
            CustomToast.show(this, "Hatırlatma ayarlanamadı: ${e.message}")
        }
    }

    private fun parseReminderMinutes(reminderText: String): Int? {
        return when (reminderText) {
            "5 dakika önce" -> 5
            "10 dakika önce" -> 10
            "30 dakika önce" -> 30
            "1 saat önce" -> 60
            "2 saat önce" -> 120
            "1 gün önce" -> 1440
            else -> null
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 öncesi için izin gerekmez
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                // Kullanıcıya neden izin gerektiğini açıkla
                AlertDialog.Builder(this)
                    .setTitle("Bildirim İzni Gerekli")
                    .setMessage("Hatırlatma bildirimleri gönderebilmek için bildirim iznine ihtiyacımız var. Bu izni verir misiniz?")
                    .setPositiveButton("İzin Ver") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton("İptal") { _, _ ->
                        CustomToast.show(this, "Bildirim izni verilmedi. Hatırlatma ayarlanamadı.")
                    }
                    .show()
            } else {
                // Direkt izin iste
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CustomToast.show(this, "Bildirim izni verildi! Hatırlatma sistemi aktif.")
                } else {
                    CustomToast.show(this, "Bildirim izni reddedildi. Hatırlatma bildirimleri gönderilemeyecek.")
                }
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        if (!checkNotificationPermission()) {
            CustomToast.show(this, "Bildirim izni gerekli")
            return
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun loadNoteForEditing(noteId: Int) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val note = db.noteDao().getNoteByIdSync(noteId)
                    withContext(Dispatchers.Main) {
                        if (note != null) {
                            // Şifreli not kontrolü
                            val decryptedContent = intent.getStringExtra("decryptedContent")
                            val isEncrypted = intent.getBooleanExtra("isEncrypted", false)
                            
                            binding.titleEditText.setText(note.title)
                            binding.descriptionEditText.setText(
                                if (isEncrypted && decryptedContent != null) decryptedContent else note.description
                            )
                            binding.dateEditText.setText(note.date)
                            binding.tagsEditText.setText(note.tags ?: "")
                            
                            // Hatırlatma bilgilerini yükle
                            if (note.reminderMinutesBefore != null) {
                                binding.reminderCheckbox.isChecked = true
                                binding.reminderOptionsLayout.visibility = android.view.View.VISIBLE
                                // Hatırlatma zamanını ayarla
                                val reminderTime = when (note.reminderMinutesBefore) {
                                    5 -> "5 dakika önce"
                                    10 -> "10 dakika önce"
                                    30 -> "30 dakika önce"
                                    60 -> "1 saat önce"
                                    120 -> "2 saat önce"
                                    1440 -> "1 gün önce"
                                    else -> "5 dakika önce"
                                }
                                binding.reminderTimeSpinner.setText(reminderTime, false)
                            }
                            
                            // Toolbar başlığını güncelle
                            binding.toolbar.title = "Notu Düzenle"
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddNoteActivity", "Not yükleme hatası: ${e.message}", e)
                CustomToast.show(this@AddNoteActivity, "Not yüklenemedi: ${e.message}")
            }
        }
    }
}
