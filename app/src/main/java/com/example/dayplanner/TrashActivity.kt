package com.example.dayplanner

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dayplanner.databinding.FragmentTrashBinding

class TrashActivity : AppCompatActivity() {

    private lateinit var binding: FragmentTrashBinding
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Silinenler"

        adapter = NoteAdapter(
            onClick = { note ->
                // Çöpteki not tıklanınca geri yükleme dialog'u göster
            },
            onLockToggle = { note, shouldLock ->
                if (!shouldLock) {
                    // Şifre kaldır
                    val updated = note.copy(isEncrypted = false, isLocked = false)
                    viewModel.update(updated)
                }
            },
            onSoftDelete = { note ->
                // Kalıcı silme
                viewModel.deleteNotePermanently(note.id)
            },
            onPinToggle = { note, shouldPin ->
                viewModel.setPinned(note.id, shouldPin)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.deletedNotes.observe(this) { notes ->
            adapter.submitList(notes)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}


