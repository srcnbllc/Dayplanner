package com.example.dayplanner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.databinding.ActivityAddNoteBinding
import com.example.dayplanner.utils.SmartTemplates
import com.example.dayplanner.utils.NoteTemplate
import com.example.dayplanner.utils.CustomToast
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var noteViewModel: NoteViewModel
    private val attachments = mutableListOf<String>()
    private lateinit var attachmentAdapter: AttachmentAdapter

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            attachments.add(it.toString())
            attachmentAdapter.notifyDataSetChanged()
            binding.attachmentsRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityAddNoteBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // ViewModel initialization
            noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
            
            // Set up toolbar (no need for setSupportActionBar since toolbar is already in layout)
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }

            // Handle encrypted note editing
            val noteId = intent.getIntExtra("noteId", -1)
            val isEncrypted = intent.getBooleanExtra("isEncrypted", false)
            val decryptedContent = intent.getStringExtra("decryptedContent")
            
            if (noteId != -1) {
                // Editing existing note
                if (isEncrypted && !decryptedContent.isNullOrEmpty()) {
                    // Load decrypted content for editing
                    binding.titleEditText.setText(intent.getStringExtra("title") ?: "")
                    binding.descriptionEditText.setText(decryptedContent)
                } else {
                    // Load normal note
                    loadNoteForEditing(noteId)
                }
            }

            setupReminderSection()
            setupFileAttachment()
            setupBottomButtons()
            setupCursorFocusSequence()

        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Error in onCreate: ${e.message}", e)
            CustomToast.show(this, "Sayfa açılırken hata oluştu: ${e.message}")
            finish()
        }
    }
    

    private fun setupReminderSection() {
        // Setup reminder switch
        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.reminderDetailsLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Setup date picker button
        binding.datePickerButton.setOnClickListener {
            showDatePicker()
        }
        
        // Setup time picker button
        binding.timePickerButton.setOnClickListener {
            showTimePicker()
        }
        
        // Setup reminder period chips
        binding.chipOnce.setOnClickListener {
            updateReminderPeriod("Tek Sefer")
        }
        binding.chipDaily.setOnClickListener {
            updateReminderPeriod("Günlük")
        }
        binding.chipWeekly.setOnClickListener {
            updateReminderPeriod("Haftalık")
        }
        binding.chipMonthly.setOnClickListener {
            updateReminderPeriod("Aylık")
        }
    }
    
    private fun updateReminderPeriod(period: String) {
        binding.selectedPeriodText.text = period
    }

    private fun setupFileAttachment() {
        attachmentAdapter = AttachmentAdapter(attachments) { position ->
            attachments.removeAt(position)
            attachmentAdapter.notifyDataSetChanged()
            if (attachments.isEmpty()) {
                binding.attachmentsRecyclerView.visibility = View.GONE
            }
        }
        
        binding.attachmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.attachmentsRecyclerView.adapter = attachmentAdapter
        
        binding.attachFileButton.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }
    }

    private fun setupBottomButtons() {
        binding.saveButton.setOnClickListener {
            saveNote(false)
        }
        
        binding.savePasswordButton.setOnClickListener {
            showPasswordDialog()
        }
    }

    private fun setupCursorFocusSequence() {
        // Set initial focus on title field
        binding.titleEditText.requestFocus()
        
        // Setup focus sequence
        binding.titleEditText.setOnEditorActionListener { _, _, _ ->
            binding.descriptionEditText.requestFocus()
            true
        }
        
        binding.descriptionEditText.setOnEditorActionListener { _, _, _ ->
            binding.reminderSwitch.requestFocus()
            true
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                selectedYear, selectedMonth + 1, selectedDay)
            binding.selectedDateText.text = selectedDate
            binding.datePickerButton.text = "Tarih: $selectedDate"
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeString = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            binding.selectedTimeText.text = timeString
            binding.timePickerButton.text = "Saat: $timeString"
        }, hour, minute, true).show()
    }

    private fun showTemplateSelector() {
        val templates = SmartTemplates.getTemplates(this)
        val templateNames = templates.map { it.name }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Şablon Seç")
            .setItems(templateNames) { _, which ->
                val selectedTemplate = templates[which]
                applyTemplate(selectedTemplate)
            }
            .show()
    }
    
    private fun applyTemplate(template: NoteTemplate) {
        val note = SmartTemplates.applyTemplate(template)
        binding.titleEditText.setText(note.title)
        binding.descriptionEditText.setText(note.description)
    }

    private fun showPasswordDialog() {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(6))
        input.setPadding(50, 20, 50, 20)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Şifreli Kaydet")
            .setMessage("6 haneli PIN girin:")
            .setView(input)
            .setPositiveButton("Kaydet") { _, _ ->
                val password = input.text.toString()
                if (password.length == 6) {
                    saveNote(true, password)
                } else {
                    CustomToast.show(this, "PIN 6 haneli olmalıdır")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun loadNoteForEditing(noteId: Int) {
        noteViewModel.getNoteById(noteId).observe(this) { note ->
            note?.let {
                binding.titleEditText.setText(it.title)
                binding.descriptionEditText.setText(it.description)
            }
        }
    }
    private fun saveNote(isEncrypted: Boolean = false, password: String = "") {
        try {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            if (title.isNotBlank() && description.isNotBlank()) {
                val noteId = intent.getIntExtra("noteId", -1)
                val note = Note(
                    id = if (noteId != -1) noteId else 0,
                    title = title,
                    description = description,
                    date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    isEncrypted = isEncrypted,
                    status = "NEW",
                    createdAt = System.currentTimeMillis(),
                    reminderMinutesBefore = if (binding.reminderSwitch.isChecked) {
                        when (binding.selectedPeriodText.text.toString()) {
                            "Tek Sefer" -> 0
                            "Günlük" -> 1
                            "Haftalık" -> 7
                            "Aylık" -> 30
                            else -> null
                        }
                    } else null
                )

                if (noteId != -1) {
                    noteViewModel.update(note)
                } else {
                    noteViewModel.insert(note)
                }
                
                val message = if (isEncrypted) "Not şifreli olarak kaydedildi" else "Not kaydedildi"
                CustomToast.show(this, message)
                finish()
            } else {
                CustomToast.show(this, "Lütfen başlık ve içerik alanlarını doldurun")
            }
        } catch (e: Exception) {
            android.util.Log.e("AddNoteActivity", "Error saving note: ${e.message}", e)
            CustomToast.show(this, "Not kaydedilemedi: ${e.message}")
        }
    }
}

// Attachment Adapter for RecyclerView
class AttachmentAdapter(
    private val attachments: List<String>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): AttachmentViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount() = attachments.size

    inner class AttachmentViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(attachment: String) {
            itemView.findViewById<android.widget.TextView>(android.R.id.text1).text = 
                "📎 ${attachment.substringAfterLast("/")}"
            
            itemView.setOnClickListener {
                onRemove(bindingAdapterPosition)
            }
        }
    }
}