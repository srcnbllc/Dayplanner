package com.example.dayplanner

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.databinding.ActivitySimpleAddNoteBinding
import com.example.dayplanner.NoteViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class SimpleAddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimpleAddNoteBinding
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("SimpleAddNoteActivity", "onCreate started")
            
            binding = ActivitySimpleAddNoteBinding.inflate(layoutInflater)
            setContentView(binding.root)
            android.util.Log.d("SimpleAddNoteActivity", "Layout inflated")
            
            // Test mesajı
            Toast.makeText(this, "Not ekleme sayfası açıldı!", Toast.LENGTH_SHORT).show()

            // ViewModel initialization
            noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

            // Toolbar setup
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Yeni Not"
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }

            // Tarih seçici
            binding.dateEditText.setOnClickListener {
                showDatePicker()
            }

            // Butonlar
            binding.saveButton.setOnClickListener {
                saveNote()
            }
            
            binding.cancelButton.setOnClickListener {
                finish()
            }
            
            android.util.Log.d("SimpleAddNoteActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("SimpleAddNoteActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
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
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                    selectedYear, selectedMonth + 1, selectedDay)
                binding.dateEditText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveNote() {
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val date = binding.dateEditText.text.toString().trim()
        
        if (title.isEmpty()) {
            binding.titleEditText.error = "Başlık gerekli"
            return
        }

        if (description.isEmpty()) {
            binding.descriptionEditText.error = "Not içeriği gerekli"
            return
        }

        val note = Note(
            title = title,
            description = description,
            date = date.ifEmpty { LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
            isPinned = false,
            isLocked = false,
            status = "ACTIVE",
            createdAt = System.currentTimeMillis()
        )

        try {
            noteViewModel.insert(note)
            Toast.makeText(this, "Not başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            android.util.Log.e("SimpleAddNoteActivity", "Error saving note: ${e.message}", e)
            Toast.makeText(this, "Not kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
