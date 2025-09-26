package com.example.dayplanner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel initialization (ensure AndroidViewModel receives Application)
        noteViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

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
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val date = binding.dateEditText.text.toString()

            // Validate input fields
            if (title.isNotBlank() && description.isNotBlank()) {
                val note = Note(
                    id = if (noteId == -1) 0 else noteId,  // If the note is new, set id to 0 (auto-generated)
                    title = title,
                    description = description,
                    date = date
                )

                // Insert new note or update existing note
                if (noteId == -1) {
                    noteViewModel.insert(note)
                } else {
                    noteViewModel.update(note)
                }

                // Finish activity after saving
                finish()
            } else {
                // Show error if input is invalid
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
