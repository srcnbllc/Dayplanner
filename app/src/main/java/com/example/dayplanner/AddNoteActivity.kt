package com.example.dayplanner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.databinding.ActivityAddNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel initialization
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Get noteId from intent
        val noteId = intent.getIntExtra("noteId", -1)

        // If noteId is valid, fetch existing note data and populate the fields
        if (noteId != -1) {
            noteViewModel.getNoteById(noteId).observe(this) { note ->
                note?.let {
                    // Directly set the text as they are guaranteed to be non-null
                    binding.titleEditText.setText(it.title)
                    binding.descriptionEditText.setText(it.description)
                    binding.dateEditText.setText(it.date)
                }
            }
        }

        // Save button click listener
        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val date = binding.dateEditText.text.toString().trim()

            // Validate input fields
            if (title.isBlank()) {
                Toast.makeText(this, "Lütfen başlık girin", Toast.LENGTH_SHORT).show()
                binding.titleEditText.requestFocus()
                return@setOnClickListener
            }
            
            if (description.isBlank()) {
                Toast.makeText(this, "Lütfen not açıklaması girin", Toast.LENGTH_SHORT).show()
                binding.descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            // Validate date format if provided
            val finalDate = if (date.isBlank()) {
                // If no date provided, use current date
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else {
                // Validate date format
                if (isValidDate(date)) {
                    convertToStandardFormat(date)
                } else {
                    Toast.makeText(this, "Geçersiz tarih formatı. GG/AA/YYYY formatında girin", Toast.LENGTH_SHORT).show()
                    binding.dateEditText.requestFocus()
                    return@setOnClickListener
                }
            }

            val note = Note(
                id = if (noteId == -1) 0 else noteId,  // If the note is new, set id to 0 (auto-generated)
                title = title,
                description = description,
                date = finalDate
            )

            // Insert new note or update existing note
            if (noteId == -1) {
                noteViewModel.insert(note)
                Toast.makeText(this, "Not başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
            } else {
                noteViewModel.update(note)
                Toast.makeText(this, "Not başarıyla güncellendi", Toast.LENGTH_SHORT).show()
            }

            // Finish activity after saving
            finish()
        }

        // Cancel button click listener
        binding.cancelButton.setOnClickListener {
            finish() // Close activity without saving
        }
    }

    // Date validation function
    private fun isValidDate(dateString: String): Boolean {
        return try {
            val formats = arrayOf(
                "dd/MM/yyyy",
                "dd.MM.yyyy",
                "dd-MM-yyyy",
                "yyyy-MM-dd"
            )
            
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    sdf.isLenient = false
                    sdf.parse(dateString)
                    return true
                } catch (e: Exception) {
                    continue
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    // Convert date to standard format (yyyy-MM-dd)
    private fun convertToStandardFormat(dateString: String): String {
        return try {
            val formats = arrayOf(
                "dd/MM/yyyy",
                "dd.MM.yyyy", 
                "dd-MM-yyyy",
                "yyyy-MM-dd"
            )
            
            for (format in formats) {
                try {
                    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                    inputFormat.isLenient = false
                    val date = inputFormat.parse(dateString)
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    return outputFormat.format(date!!)
                } catch (e: Exception) {
                    continue
                }
            }
            // If conversion fails, return current date
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
