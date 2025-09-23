package com.example.dayplanner.ui.notes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.AddNoteActivity
import com.example.dayplanner.NoteAdapter
import com.example.dayplanner.NoteViewModel
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentDeletedNotesBinding

class DeletedNotesFragment : Fragment() {

    private var _binding: FragmentDeletedNotesBinding? = null
    private val binding get() = _binding!!

    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter
    private var allDeletedNotes: List<com.example.dayplanner.Note> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeletedNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupUI()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onClick = { note ->
                // Silinen notu görüntüle
                val intent = Intent(requireContext(), AddNoteActivity::class.java)
                intent.putExtra("noteId", note.id)
                intent.putExtra("isDeleted", true)
                startActivity(intent)
            },
            onPinToggle = { _ ->
                // Silinen notlarda pinleme yok
            },
            onLockToggle = { _ ->
                // Silinen notlarda kilitleme yok
            },
            onMoreClick = { note ->
                showDeletedNoteOptionsMenu(note)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = noteAdapter
        attachSwipeActions()

        // Sadece silinen notları getir
        noteViewModel.notesByStatus("DELETED").observe(viewLifecycleOwner) { notes ->
            allDeletedNotes = notes
            noteAdapter.submitList(notes)
            
            // Empty state göster/gizle
            if (notes.isEmpty()) {
                binding.emptyStateLayout.visibility = android.view.View.VISIBLE
                binding.recyclerView.visibility = android.view.View.GONE
            } else {
                binding.emptyStateLayout.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.emptyAllButton.setOnClickListener {
            showEmptyAllConfirmation()
        }
    }

    private fun attachSwipeActions() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val position = vh.bindingAdapterPosition
                val note = noteAdapter.currentList.getOrNull(position) ?: return
                if (dir == ItemTouchHelper.RIGHT) {
                    restoreNote(note)
                } else if (dir == ItemTouchHelper.LEFT) {
                    showPermanentDeleteConfirmation(note)
                }
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
    }

    private fun showDeletedNoteOptionsMenu(note: com.example.dayplanner.Note) {
        val options = arrayOf("Geri Yükle", "Kalıcı Olarak Sil", "Detayları Görüntüle")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Silinen Not Seçenekleri")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> restoreNote(note)
                    1 -> showPermanentDeleteConfirmation(note)
                    2 -> {
                        val intent = Intent(requireContext(), AddNoteActivity::class.java)
                        intent.putExtra("noteId", note.id)
                        intent.putExtra("isDeleted", true)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun restoreNote(note: com.example.dayplanner.Note) {
        val updatedNote = note.copy(
            status = "ACTIVE",
            deletedAt = null
        )
        noteViewModel.update(updatedNote)
        Toast.makeText(requireContext(), "Not geri yüklendi", Toast.LENGTH_SHORT).show()
    }

    private fun showPermanentDeleteConfirmation(note: com.example.dayplanner.Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Kalıcı Olarak Sil")
            .setMessage("Bu notu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz!")
            .setPositiveButton("Kalıcı Olarak Sil") { _, _ ->
                noteViewModel.delete(note)
                Toast.makeText(requireContext(), "Not kalıcı olarak silindi", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("İptal") { _, _ ->
                noteAdapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun showEmptyAllConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tüm Silinen Notları Temizle")
            .setMessage("Tüm silinen notları kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz!")
            .setPositiveButton("Tümünü Sil") { _, _ ->
                allDeletedNotes.forEach { note ->
                    noteViewModel.delete(note)
                }
                Toast.makeText(requireContext(), "Tüm silinen notlar temizlendi", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
