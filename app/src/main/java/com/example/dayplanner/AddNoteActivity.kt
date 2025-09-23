package com.example.dayplanner

import android.app.DatePickerDialog
import android.os.Bundle
import android.net.Uri
import android.app.Activity
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.databinding.ActivityAddNoteBinding
import androidx.activity.result.contract.ActivityResultContracts
import android.speech.RecognizerIntent
import com.example.dayplanner.reminders.ReminderManager
import com.example.dayplanner.features.ModernFeatures
import com.example.dayplanner.NoteViewModel
import com.example.dayplanner.security.PasswordManager
import com.example.dayplanner.security.PasswordStrength
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var reminderManager: ReminderManager
    private var currentImageUri: String? = null
    private var currentNoteId: Int = -1

        // Hatırlatma seçenekleri
        private val reminderTimeOptions = listOf(
            "5 dakika önce",
            "10 dakika önce",
            "20 dakika önce",
            "30 dakika önce",
            "1 saat önce",
            "2 saat önce",
            "1 gün önce",
            "1 hafta önce"
        )

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentImageUri = it.toString()
            binding.imagePreview.setImageURI(it)
            binding.imagePreview.visibility = android.view.View.VISIBLE
        }
    }

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = results?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                binding.descriptionEditText.setText(spoken)
                parseNaturalDate(spoken)?.let { parsed ->
                    binding.dateEditText.setText(parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("AddNoteActivity", "onCreate started")
            
            // Önce basit layout ile test
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
            android.util.Log.d("AddNoteActivity", "Layout inflated")

        // ViewModel initialization
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
            reminderManager = ReminderManager(this)
            android.util.Log.d("AddNoteActivity", "ViewModels initialized")

        // Get noteId from intent
            currentNoteId = intent.getIntExtra("noteId", -1)
            android.util.Log.d("AddNoteActivity", "NoteId: $currentNoteId")

            // Sadece temel setup'ları yap
            setupBasicUI()
            
            // Eğer not düzenleniyorsa, notu yükle
            if (currentNoteId != -1) {
                loadNoteIfEditing()
            }
            
            android.util.Log.d("AddNoteActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Not ekleme ekranı yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupBasicUI() {
        try {
            android.util.Log.d("AddNoteActivity", "Setting up basic UI")
            
            // Toolbar setup
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = if (currentNoteId != -1) "Notu Düzenle" else "Yeni Not"
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
            android.util.Log.d("AddNoteActivity", "Toolbar setup complete")
            
            // Temel alanları doldur - sadece başlık
            if (currentNoteId == -1) {
                binding.titleEditText.setText("")
                binding.descriptionEditText.setText("")
            }

            // Tarih seçici
            binding.dateEditText.setOnClickListener {
                showDatePicker()
            }

            // Kaydet butonu
            binding.saveButton.setOnClickListener {
                android.util.Log.d("AddNoteActivity", "Save button clicked!")
                Toast.makeText(this, "Kaydet butonu tıklandı!", Toast.LENGTH_SHORT).show()
                saveNote()
            }
            
            // Vazgeç butonu
            binding.cancelButton.setOnClickListener {
                android.util.Log.d("AddNoteActivity", "Cancel button clicked!")
                Toast.makeText(this, "Vazgeç butonu tıklandı!", Toast.LENGTH_SHORT).show()
                finish()
            }
            
            // Şifreli kaydet butonu
            binding.encryptButton.setOnClickListener {
                android.util.Log.d("AddNoteActivity", "Encrypt button clicked!")
                Toast.makeText(this, "Şifreli Kaydet butonu tıklandı!", Toast.LENGTH_SHORT).show()
                showPasswordCreationDialog()
            }
            
            android.util.Log.d("AddNoteActivity", "Basic UI setup complete")
        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Error in setupBasicUI: ${e.message}", e)
            Toast.makeText(this, "UI ayarlanamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupUI() {
        // Tarih seçici
        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }

        // Medya butonları
        binding.pickImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.micButton.setOnClickListener {
            startSpeechRecognition()
        }

            binding.ocrButton.setOnClickListener {
                // OCR functionality - disabled for 16KB compatibility
                Toast.makeText(this, "OCR özelliği 16KB uyumluluğu için geçici olarak devre dışı", Toast.LENGTH_SHORT).show()
            }

        // Button handlers are already set up in setupBasicUI() - no duplicates needed
        
        // Akıllı öneriler
        setupSmartSuggestions()
    }

        private fun setupReminderSpinner() {
            // Hatırlatma checkbox listener
            binding.reminderCheckbox.setOnCheckedChangeListener { _, isChecked ->
                binding.reminderOptionsLayout.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
            }
            
            // Hatırlatma tarihi seçici
            binding.reminderDateText.setOnClickListener {
                showReminderDatePicker()
            }
            
            // Hatırlatma zamanı spinner
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, reminderTimeOptions)
            binding.reminderTimeSpinner.setAdapter(adapter)
            binding.reminderTimeSpinner.setText(reminderTimeOptions[0], false)
        }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                    selectedYear, selectedMonth + 1, selectedDay)
                binding.dateEditText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showReminderDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                    selectedYear, selectedMonth + 1, selectedDay)
                binding.reminderDateText.setText(selectedDate)
                updateReminderDescription()
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Notunuzu söyleyin...")
        }
        speechLauncher.launch(intent)
    }

    private fun parseNaturalDate(text: String): LocalDateTime? {
        // Basit doğal dil tarih ayrıştırma
        val now = LocalDateTime.now()
        return when {
            text.contains("bugün", ignoreCase = true) -> now
            text.contains("yarın", ignoreCase = true) -> now.plusDays(1)
            text.contains("gelecek hafta", ignoreCase = true) -> now.plusWeeks(1)
            text.contains("gelecek ay", ignoreCase = true) -> now.plusMonths(1)
            else -> null
        }
    }

    private fun loadNoteIfEditing() {
        val isEncrypted = intent.getBooleanExtra("isEncrypted", false)
        val decryptedContent = intent.getStringExtra("decryptedContent")
        
        if (currentNoteId != -1) {
            noteViewModel.getNoteById(currentNoteId).observe(this) { note ->
                note?.let {
                    binding.titleEditText.setText(it.title)
                    
                        // Şifreli not kontrolü
                        if (isEncrypted && decryptedContent != null) {
                            binding.descriptionEditText.setText(decryptedContent)
                        } else if (it.encryptedBlob != null) {
                            binding.descriptionEditText.setText("🔒 Şifrelenmiş not - Şifre gerekli")
                        } else {
                            binding.descriptionEditText.setText(it.description)
                        }
                    
                    binding.dateEditText.setText(it.date)
                    binding.tagsEditText.setText(it.tags)
                    
                    // Chip durumları kaldırıldı
                    
                    // Hatırlatma ayarı
                    it.reminderMinutesBefore?.let { minutes ->
                        val reminderText = when (minutes) {
                            5 -> "5 dakika önce"
                            10 -> "10 dakika önce"
                            20 -> "20 dakika önce"
                            30 -> "30 dakika önce"
                            60 -> "1 saat önce"
                            120 -> "2 saat önce"
                            1440 -> "1 gün önce"
                            10080 -> "1 hafta önce"
                            else -> "Hatırlatma yok"
                        }
                        binding.reminderTimeSpinner.setText(reminderText, false)
                    }
                    
                    // Görsel
                    currentImageUri = it.imageUri
                    it.imageUri?.let { uri -> 
                        binding.imagePreview.setImageURI(Uri.parse(uri))
                        binding.imagePreview.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
    }

    private fun saveNote() {
        try {
            android.util.Log.d("AddNoteActivity", "saveNote() called")
            
            val title = binding.titleEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val date = binding.dateEditText.text.toString().trim()
            val tags = binding.tagsEditText.text.toString().trim()

            android.util.Log.d("AddNoteActivity", "Title: $title, Description: $description")

            if (title.isEmpty()) {
                binding.titleEditText.error = "Başlık gerekli"
                Toast.makeText(this, "Başlık gerekli!", Toast.LENGTH_SHORT).show()
                return
            }

            // Description is now optional - no validation required

            // Hatırlatma kontrolü - basit versiyon
            var reminderMinutes: Int? = null

            val note = Note(
                id = if (currentNoteId != -1) currentNoteId else 0,
                title = title,
                description = description,
                date = date.ifEmpty { LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
                tags = tags,
                isPinned = false, // Chip'ler kaldırıldı, varsayılan değer
                isLocked = false, // Chip'ler kaldırıldı, varsayılan değer
                imageUri = currentImageUri,
                reminderMinutesBefore = reminderMinutes,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis()
            )

            android.util.Log.d("AddNoteActivity", "Note created: ${note.title}")

            if (currentNoteId != -1) {
                noteViewModel.update(note)
                Toast.makeText(this, "Not güncellendi", Toast.LENGTH_SHORT).show()
                android.util.Log.d("AddNoteActivity", "Note updated")
            } else {
                noteViewModel.insert(note)
                Toast.makeText(this, "Not kaydedildi", Toast.LENGTH_SHORT).show()
                android.util.Log.d("AddNoteActivity", "Note inserted")
            }

            finish()
        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Error in saveNote: ${e.message}", e)
            Toast.makeText(this, "Not kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseReminderMinutes(reminderText: String): Int? {
        return when (reminderText) {
            "5 dakika önce" -> 5
            "10 dakika önce" -> 10
            "20 dakika önce" -> 20
            "30 dakika önce" -> 30
            "1 saat önce" -> 60
            "2 saat önce" -> 120
            "1 gün önce" -> 1440
            "1 hafta önce" -> 10080
            else -> null
        }
    }

    private fun scheduleReminderIfNeeded(title: String, dateStr: String, minutesBefore: Int, noteId: Int) {
        if (dateStr.isBlank()) return

        runCatching {
            val date = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val reminderTime = date.minusMinutes(minutesBefore.toLong())

            if (reminderTime.isAfter(LocalDateTime.now())) {
                reminderManager.scheduleNoteReminder(
                    noteId = noteId,
                    title = "Hatırlatma: $title",
                    message = title,
                    reminderTime = reminderTime,
                    reminderType = "note"
                )
            }
        }
    }

    private fun deleteNote() {
        if (currentNoteId != -1) {
            // Önce notu bulup sonra sil
            noteViewModel.getNoteById(currentNoteId).observe(this) { note ->
                note?.let {
                    noteViewModel.delete(it)
                    Toast.makeText(this, "Not silindi", Toast.LENGTH_SHORT).show()
                }
            }
        }
        finish()
    }

    private fun setupTemplateButton() {
        binding.templateButton.setOnClickListener {
            showTemplateDialog()
        }
    }

    private fun setupSmartSuggestions() {
        // Başlık alanına akıllı öneriler
        binding.titleEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.titleEditText.text.isNullOrBlank()) {
                val suggestion = ModernFeatures.SmartSuggestions.getTimeBasedGreeting()
                binding.titleEditText.hint = suggestion
            }
        }
    }

    private fun showTemplateDialog() {
        val templates = listOf(
            ModernFeatures.Templates.MEETING,
            ModernFeatures.Templates.SHOPPING,
            ModernFeatures.Templates.TODO,
            ModernFeatures.Templates.IDEA,
            ModernFeatures.Templates.JOURNAL,
            ModernFeatures.Templates.RECIPE,
            ModernFeatures.Templates.TRAVEL,
            ModernFeatures.Templates.PROJECT
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Şablon Seçin")
            .setItems(templates.toTypedArray()) { _, which ->
                val selectedTemplate = templates[which]
                applyTemplate(selectedTemplate)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun applyTemplate(template: String) {
        when (template) {
            ModernFeatures.Templates.MEETING -> {
                binding.titleEditText.setText("Toplantı Notu")
                binding.descriptionEditText.setText(ModernFeatures.NoteFormats.createMeetingNote(
                    "Toplantı Başlığı",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    listOf("Katılımcı 1", "Katılımcı 2")
                ))
            }
            ModernFeatures.Templates.SHOPPING -> {
                binding.titleEditText.setText("Alışveriş Listesi")
                binding.descriptionEditText.setText(ModernFeatures.NoteFormats.createShoppingList(
                    listOf("Süt", "Ekmek", "Yumurta", "Meyve")
                ))
            }
            ModernFeatures.Templates.TODO -> {
                binding.titleEditText.setText("Yapılacaklar Listesi")
                binding.descriptionEditText.setText(ModernFeatures.NoteFormats.createTodoList(
                    "Bugünkü Görevler",
                    listOf("İş toplantısı", "Alışveriş yap", "Spor yap")
                ))
            }
            ModernFeatures.Templates.IDEA -> {
                binding.titleEditText.setText("Fikir Notu")
                binding.descriptionEditText.setText("💡 Fikir: \n\n📝 Detaylar: \n\n🎯 Hedef: \n\n⏰ Zaman: ")
            }
            ModernFeatures.Templates.JOURNAL -> {
                binding.titleEditText.setText("Günlük Not - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                binding.descriptionEditText.setText("📅 Bugün neler oldu?\n\n😊 En güzel an:\n\n😔 Zorluklar:\n\n🎯 Yarın için planlar:\n\n💭 Düşünceler:")
            }
        }
    }

    private fun showPasswordCreationDialog() {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "6 haneli PIN girin"
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(6))
        input.setPadding(50, 20, 50, 20)
        input.textSize = 28f
        input.gravity = android.view.Gravity.CENTER
        input.setBackgroundResource(android.R.drawable.edit_text)

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(android.widget.TextView(this@AddNoteActivity).apply {
                text = "🔒 Şifreli Not İçin PIN Oluştur"
                textSize = 18f
                setPadding(50, 20, 50, 10)
                gravity = android.view.Gravity.CENTER
            })
            addView(android.widget.TextView(this@AddNoteActivity).apply {
                text = "6 haneli rakam PIN'i girin"
                textSize = 14f
                setPadding(50, 10, 50, 20)
                gravity = android.view.Gravity.CENTER
            })
            addView(input)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("PIN Oluştur")
            .setView(container)
            .setPositiveButton("Kaydet", null) // Null yapıp manuel kontrol edeceğiz
            .setNegativeButton("İptal", null)
            .create()

        dialog.show()

        // Positive button'u manuel kontrol et
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val password = input.text.toString()
            
            if (password.length != 6) {
                Toast.makeText(this, "PIN 6 haneli olmalıdır", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!password.matches(Regex("\\d{6}"))) {
                Toast.makeText(this, "Sadece rakam girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Şifreli notu kaydet
            saveEncryptedNote(password)
            dialog.dismiss()
        }
    }

    private fun saveEncryptedNote(password: String) {
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val date = binding.dateEditText.text.toString().trim()
        val tags = binding.tagsEditText.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleEditText.error = "Başlık gerekli"
            return
        }

        // Description is now optional - no validation required

        try {
            // İçeriği şifrele
            val encryptedContent = PasswordManager.encryptNote(description, password)
            
            val note = Note(
                id = if (currentNoteId != -1) currentNoteId else 0,
                title = title,
                description = "", // Şifrelenmiş içerik boş
                date = date.ifEmpty { LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
                tags = tags,
                isPinned = false,
                isLocked = true, // Şifreli not
                encryptedBlob = encryptedContent.toByteArray(),
                imageUri = currentImageUri,
                reminderMinutesBefore = null,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis()
            )

            if (currentNoteId != -1) {
                noteViewModel.update(note)
                Toast.makeText(this, "Şifreli not güncellendi", Toast.LENGTH_SHORT).show()
            } else {
                noteViewModel.insert(note)
                Toast.makeText(this, "Şifreli not kaydedildi", Toast.LENGTH_SHORT).show()
            }

            finish()
        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Error saving encrypted note: ${e.message}", e)
            Toast.makeText(this, "Şifreli not kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEncryptionDialog() {
        val options = arrayOf("Şifrele", "Şifreyi Kaldır")
        val currentNote = getCurrentNote()
        val isEncrypted = currentNote?.isLocked == true
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(if (isEncrypted) "Şifreleme Seçenekleri" else "Notu Şifrele")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> if (isEncrypted) {
                        showPasswordDialog("Şifreyi Kaldır", "Şifreyi kaldırmak için mevcut şifreyi girin:") { _ ->
                            removeEncryption()
                        }
                    } else {
                        showPasswordDialog("Şifre Belirle", "Notu şifrelemek için bir şifre belirleyin:") { password ->
                            encryptNote()
                        }
                    }
                    1 -> if (isEncrypted) {
                        showPasswordDialog("Şifreyi Kaldır", "Şifreyi kaldırmak için mevcut şifreyi girin:") { _ ->
                            removeEncryption()
                        }
                    }
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showPasswordDialog(title: String, message: String, onConfirm: (String) -> Unit) {
        // Layout kullanılmıyor, kaldırıldı
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Şifre girin"
        input.setPadding(50, 20, 50, 20)

        val strengthText = android.widget.TextView(this)
        strengthText.text = "Şifre gücü: Zayıf"
        strengthText.setTextColor(android.graphics.Color.RED)
        strengthText.setPadding(50, 10, 50, 10)

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(input)
            addView(strengthText)
        }

        // Şifre gücü kontrolü
        input.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val password = s.toString()
                val strength = PasswordManager.getPasswordStrength(password)
                when (strength) {
                    PasswordStrength.WEAK -> {
                        strengthText.text = "Şifre gücü: Zayıf"
                        strengthText.setTextColor(android.graphics.Color.RED)
                    }
                    PasswordStrength.MEDIUM -> {
                        strengthText.text = "Şifre gücü: Orta"
                        strengthText.setTextColor(android.graphics.Color.YELLOW)
                    }
                    PasswordStrength.STRONG -> {
                        strengthText.text = "Şifre gücü: Güçlü"
                        strengthText.setTextColor(android.graphics.Color.GREEN)
                    }
                }
            }
        })

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(container)
            .setPositiveButton("Tamam") { _, _ ->
                val password = input.text.toString()
                if (password.isNotEmpty()) {
                    onConfirm(password)
                } else {
                    Toast.makeText(this, "Şifre boş olamaz", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun getCurrentNote(): Note? {
        // This function is not used in the current implementation
        // The note loading is handled by loadNoteIfEditing() which uses LiveData properly
        return null
    }

    private fun encryptNote() {
        try {
            val content = binding.descriptionEditText.text.toString()
            if (content.isEmpty()) {
                Toast.makeText(this, "Şifrelenecek içerik bulunamadı", Toast.LENGTH_SHORT).show()
                return
            }

            // Yeni not için şifreleme - kaydet butonuna basılacak
            Toast.makeText(this, "Not şifrelenecek (kaydet butonuna basın)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Şifreleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeEncryption() {
        try {
            // This function is not used in the current implementation
            // Encryption/decryption is handled by the saveEncryptedNote() function
            Toast.makeText(this, "Şifre kaldırma özelliği şu anda kullanılamıyor", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Şifre kaldırma hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateReminderDescription() {
        try {
            if (binding.reminderCheckbox.isChecked) {
                val reminderDate = binding.reminderDateText.text.toString()
                val reminderTime = binding.reminderTimeSpinner.text.toString()
                
                if (reminderDate.isNotEmpty() && reminderTime.isNotEmpty()) {
                    val description = "Hatırlatma: $reminderDate tarihinde $reminderTime bildirim alacaksınız"
                    binding.reminderDescriptionText.text = description
                } else {
                    binding.reminderDescriptionText.text = "Hatırlatma: Tarih ve zaman seçin"
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Error in updateReminderDescription: ${e.message}", e)
        }
    }
}