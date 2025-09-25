package com.example.dayplanner

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dayplanner.databinding.ActivityMainBinding

class TrashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Silinenler"

        adapter = NoteAdapter(
            onClick = { /* Çöpteki not tıklanınca düzenleme yok */ },
            onLockToggle = { note, shouldLock ->
                val updated = note.copy(isEncrypted = shouldLock)
                viewModel.update(updated)
            },
            onSoftDelete = { /* Çöpte tekrar silme yok; istenirse kalıcı silme eklenebilir */ }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.deletedNotes.observe(this) { notes ->
            adapter.submitList(notes)
        }

        // Alt butonu bu ekranda gizleyelim
        binding.addNoteButton.visibility = android.view.View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

