package com.example.dayplanner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
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
            
            // Set up action bar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Yeni Not"

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
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_note_template, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_template -> {
                showTemplateSelector()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupReminderSection() {
        // Setup reminder period dropdown
        val reminderPeriods = listOf(
            "5 Minutes Before",
            "10 Minutes Before", 
            "30 Minutes Before",
            "1 Hour Before"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, reminderPeriods)
        binding.reminderPeriodDropdown.setAdapter(adapter)
        
        // Setup reminder checkbox
        binding.reminderCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.reminderDetailsLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Setup date picker
        binding.reminderDateEditText.setOnClickListener {
            showDatePicker()
        }
        
        // Setup time picker
        binding.reminderTimeEditText.setOnClickListener {
            showTimePicker()
        }
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
            binding.reminderCheckbox.requestFocus()
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
            binding.reminderDateEditText.setText(selectedDate)
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeString = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            binding.reminderTimeEditText.setText(timeString)
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

    private fun saveNote(isEncrypted: Boolean = false, password: String = "") {
        try {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            if (title.isNotBlank() && description.isNotBlank()) {
                val note = Note(
                    id = 0,
                    title = title,
                    description = description,
                    date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    isEncrypted = isEncrypted,
                    status = "NEW",
                    createdAt = System.currentTimeMillis(),
                    reminderMinutesBefore = if (binding.reminderCheckbox.isChecked) {
                        when (binding.reminderPeriodDropdown.text.toString()) {
                            "5 Minutes Before" -> 5
                            "10 Minutes Before" -> 10
                            "30 Minutes Before" -> 30
                            "1 Hour Before" -> 60
                            else -> null
                        }
                    } else null
                )

                noteViewModel.insert(note)
                
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