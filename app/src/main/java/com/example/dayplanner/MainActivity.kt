package com.example.dayplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dayplanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val noteViewModel: NoteViewModel by viewModels() // ViewModel'i kullanıyoruz
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NoteAdapter'ı RecyclerView'a bağlamak için oluşturduk
        noteAdapter = NoteAdapter(
            onClick = { note ->
            // Herhangi bir not'a tıklandığında notu düzenlemek için AddNoteActivity'ye geçiş yapıyoruz
            val intent = Intent(this, AddNoteActivity::class.java)
            intent.putExtra("noteId", note.id)
            startActivity(intent)
            },
            onLockToggle = { note, shouldLock ->
                val updated = note.copy(isEncrypted = shouldLock)
                noteViewModel.update(updated)
            },
            onSoftDelete = { note ->
                noteViewModel.softDeleteById(note.id)
            }
        )

        // RecyclerView'ın layout manager'ını ve adapter'ını ayarlıyoruz
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = noteAdapter

        // ViewModel'den notları izliyoruz ve RecyclerView'a bağlıyoruz
        noteViewModel.allNotes.observe(this, Observer { notes ->
            notes?.let { noteAdapter.submitList(it) }
        })

        // Add Note butonuna tıklanıldığında, yeni not eklemek için AddNoteActivity'ye yönlendiriyoruz
        binding.addNoteButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_trash -> {
                startActivity(Intent(this, TrashActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
