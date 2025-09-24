package com.example.dayplanner

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.dayplanner.databinding.ActivityAddNoteBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private val noteViewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get noteId from intent
        val noteIdFromIntent = intent.getIntExtra("noteId", -1)

        // If noteId is valid, fetch existing note data and populate the fields
        if (noteIdFromIntent != -1) {
            noteViewModel.getNoteById(noteIdFromIntent).observe(this) { existingNote ->
                existingNote?.let { safeNote ->
                    binding.titleEditText.setText(safeNote.title)
                    binding.descriptionEditText.setText(safeNote.description)
                    binding.dateEditText.setText(safeNote.date)
                }
            }
        }

        // Date picker
        binding.dateEditText.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.dateEditText.setText(selectedDate)
            }
        }

        // Cancel
        binding.cancelButton.setOnClickListener {
            finish()
        }

        // Save
        binding.saveButton.setOnClickListener {
            val titleInput = binding.titleEditText.text.toString().trim()
            val descriptionInput = binding.descriptionEditText.text.toString().trim()
            val dateInput = binding.dateEditText.text.toString().trim()

            if (titleInput.isNotBlank() && descriptionInput.isNotBlank()) {
                val noteToPersist = Note(
                    id = if (noteIdFromIntent == -1) 0 else noteIdFromIntent,
                    title = titleInput,
                    description = descriptionInput,
                    date = dateInput
                )

                if (noteIdFromIntent == -1) {
                    noteViewModel.insert(noteToPersist)
                } else {
                    noteViewModel.update(noteToPersist)
                }
                finish()
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(onSelected: (String) -> Unit) {
        val calendarNow = Calendar.getInstance()
        val yearNow = calendarNow.get(Calendar.YEAR)
        val monthNow = calendarNow.get(Calendar.MONTH)
        val dayNow = calendarNow.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val calendarSelected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onSelected(formatter.format(calendarSelected.time))
            },
            yearNow,
            monthNow,
            dayNow
        ).show()
    }
}
