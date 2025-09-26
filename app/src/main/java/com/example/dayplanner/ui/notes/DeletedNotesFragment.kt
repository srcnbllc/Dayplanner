package com.example.dayplanner.ui.notes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dayplanner.utils.CustomToast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.NoteAdapter
import com.example.dayplanner.NoteViewModel
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentDeletedNotesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class DeletedNotesFragment : Fragment() {

    private var _binding: FragmentDeletedNotesBinding? = null
    private val binding get() = _binding!!

    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter
    private var isSelectionMode: Boolean = false
    private var selectedNotes: MutableSet<Int> = mutableSetOf()

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
        
        try {
            setupRecyclerView()
            setupButtons()
            setupSelectionMode()
            observeDeletedNotes()
        } catch (e: Exception) {
            Log.e("DeletedNotesFragment", "onViewCreated error", e)
            // Show empty state if setup fails
            showEmptyState("Silinen notlar yüklenirken hata oluştu")
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onClick = { note -> 
                // Silinen notu açma - güvenli şekilde
                try {
                    openDeletedNoteSafely(note)
                } catch (e: Exception) {
                    android.util.Log.e("DeletedNotesFragment", "Error opening deleted note: ${e.message}", e)
                    CustomToast.show(requireContext(), "Not açılamadı: ${e.message}")
                }
            },
            onLockToggle = { note, isLocked -> 
                // Silinen notta lock toggle yok
            },
            onSoftDelete = { note ->
                showPermanentDeleteDialog(note)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = noteAdapter
    }

    private fun setupButtons() {
        binding.restoreAllButton.setOnClickListener {
            restoreAllNotes()
        }

        binding.deleteAllButton.setOnClickListener {
            deleteAllNotesPermanently()
        }
    }

    private fun setupSelectionMode() {
        // Bottom action bar buttons
        binding.btnCancel.setOnClickListener {
            exitSelectionMode()
        }
        
        binding.checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectAllNotes()
            } else {
                deselectAllNotes()
            }
        }
        
        binding.btnRestoreSelected.setOnClickListener {
            showRestoreSelectedConfirmation()
        }
        
        binding.btnDeleteSelected.setOnClickListener {
            showDeleteSelectedConfirmation()
        }
    }

    private fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        selectedNotes.clear()
        
        if (isSelectionMode) {
            binding.selectionModeLayout.visibility = View.VISIBLE
            binding.selectModeButton.text = "İptal"
            binding.selectModeButton.setIconResource(android.R.drawable.ic_menu_close_clear_cancel)
            CustomToast.show(requireContext(), "Çoklu seçim modu açıldı")
        } else {
            binding.selectionModeLayout.visibility = View.GONE
            binding.selectModeButton.text = "Seç"
            binding.selectModeButton.setIconResource(R.drawable.ic_check_box)
            CustomToast.show(requireContext(), "Çoklu seçim modu kapandı")
        }
        
        noteAdapter.notifyDataSetChanged()
    }

    private fun selectAllNotes() {
        selectedNotes.clear()
        noteAdapter.currentList.forEach { note ->
            selectedNotes.add(note.id)
        }
        noteAdapter.notifyDataSetChanged()
    }

    private fun deselectAllNotes() {
        selectedNotes.clear()
        noteAdapter.notifyDataSetChanged()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedNotes.clear()
        binding.selectionBar.visibility = View.GONE
        binding.checkboxSelectAll.isChecked = false
        // noteAdapter.setSelectionMode(false) // NoteAdapter doesn't have setSelectionMode
    }

    private fun showRestoreSelectedConfirmation() {
        if (selectedNotes.isEmpty()) {
            CustomToast.show(requireContext(), "Geri yüklenecek not seçin")
            return
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Seçilen Notları Geri Yükle")
            .setMessage("${selectedNotes.size} not geri yüklenecek. Emin misiniz?")
            .setPositiveButton("Geri Yükle") { _, _ ->
                restoreSelectedNotes()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showDeleteSelectedConfirmation() {
        if (selectedNotes.isEmpty()) {
            CustomToast.show(requireContext(), "Silinecek not seçin")
            return
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Seçilen Notları Kalıcı Olarak Sil")
            .setMessage("${selectedNotes.size} not kalıcı olarak silinecek. Bu işlem geri alınamaz!")
            .setPositiveButton("Kalıcı Olarak Sil") { _, _ ->
                deleteSelectedNotesPermanently()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun restoreSelectedNotes() {
        lifecycleScope.launch {
            try {
                selectedNotes.forEach { noteId ->
                    noteViewModel.restoreNote(noteId)
                }
                exitSelectionMode()
                CustomToast.show(requireContext(), "${selectedNotes.size} not geri yüklendi")
            } catch (e: Exception) {
                CustomToast.show(requireContext(), "Notlar geri yüklenemedi: ${e.message}")
            }
        }
    }

    private fun deleteSelectedNotesPermanently() {
        lifecycleScope.launch {
            try {
                selectedNotes.forEach { noteId ->
                    noteViewModel.deleteNotePermanently(noteId)
                }
                exitSelectionMode()
                CustomToast.show(requireContext(), "${selectedNotes.size} not kalıcı olarak silindi")
            } catch (e: Exception) {
                CustomToast.show(requireContext(), "Notlar silinemedi: ${e.message}")
            }
        }
    }

    private fun observeDeletedNotes() {
        try {
            noteViewModel.deletedNotes.observe(viewLifecycleOwner) { notes ->
                try {
                    if (notes.isNullOrEmpty()) {
                        showEmptyState("Silinen not bulunamadı")
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        noteAdapter.submitList(notes)
                    }
                } catch (e: Exception) {
                    Log.e("DeletedNotesFragment", "Error updating notes list", e)
                    showEmptyState("Notlar güncellenirken hata oluştu")
                }
            }
        } catch (e: Exception) {
            Log.e("DeletedNotesFragment", "Error observing deleted notes", e)
            showEmptyState("Silinen notlar yüklenirken hata oluştu")
        }
    }

    private fun showEmptyState(message: String) {
        try {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            // Update empty state message if needed
            Log.d("DeletedNotesFragment", "Empty state: $message")
        } catch (e: Exception) {
            Log.e("DeletedNotesFragment", "Error showing empty state", e)
        }
    }

    private fun showUndoSnackbar(message: String, undoAction: () -> Unit) {
        try {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setAction("Geri Al") {
                    undoAction()
                }
                .show()
        } catch (e: Exception) {
            Log.e("DeletedNotesFragment", "Error showing undo snackbar", e)
        }
    }

    private fun openDeletedNoteSafely(note: com.example.dayplanner.Note) {
        try {
            // Check if note is valid
            if (note.title.isBlank()) {
                CustomToast.show(requireContext(), "Geçersiz not")
                return
            }
            
            // For encrypted notes, show password dialog
            if (note.isEncrypted || note.encryptedBlob != null) {
                showPasswordDialog("Şifreli Silinen Not", "Bu notu görüntülemek için şifrenizi girin:") { password ->
                    try {
                        val encryptedContent = String(note.encryptedBlob ?: return@showPasswordDialog)
                        val decryptedContent = com.example.dayplanner.security.PasswordManager.decryptNote(encryptedContent, password)
                        
                        // Show read-only view
                        showReadOnlyNoteDialog(note.title, decryptedContent)
                    } catch (e: Exception) {
                        CustomToast.show(requireContext(), "Şifre yanlış veya not açılamadı")
                    }
                }
            } else {
                // Show read-only view for non-encrypted notes
                showReadOnlyNoteDialog(note.title, note.description)
            }
        } catch (e: Exception) {
            android.util.Log.e("DeletedNotesFragment", "Error opening deleted note safely: ${e.message}", e)
            CustomToast.show(requireContext(), "Not açılamadı: ${e.message}")
        }
    }
    
    private fun showReadOnlyNoteDialog(title: String, content: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("Tamam", null)
            .show()
    }
    
    private fun showPasswordDialog(title: String, message: String, onPasswordEntered: (String) -> Unit) {
        val passwordInput = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Şifre girin"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setView(passwordInput)
            .setPositiveButton("Aç") { _, _ ->
                val password = passwordInput.text.toString().trim()
                if (password.isNotEmpty()) {
                    onPasswordEntered(password)
                } else {
                    CustomToast.show(requireContext(), "Şifre girin")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showDeletedNoteOptions(note: com.example.dayplanner.Note) {
        val options = arrayOf("Geri Yükle", "Kalıcı Olarak Sil")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Seçenekler")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> restoreNote(note)
                    1 -> deleteNotePermanently(note)
                }
            }
            .show()
    }

    private fun showPermanentDeleteDialog(note: com.example.dayplanner.Note) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Kalıcı Silme")
            .setMessage("'${note.title}' notunu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Sil") { _, _ ->
                deleteNotePermanently(note)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun restoreNote(note: com.example.dayplanner.Note) {
        lifecycleScope.launch {
            try {
                noteViewModel.restoreNote(note.id)
                CustomToast.show(requireContext(), "Not geri yüklendi: ${note.title}")
            } catch (e: Exception) {
                CustomToast.show(requireContext(), "Not geri yüklenemedi: ${e.message}")
            }
        }
    }

    private fun deleteNotePermanently(note: com.example.dayplanner.Note) {
        lifecycleScope.launch {
            try {
                noteViewModel.deleteNotePermanently(note.id)
                CustomToast.show(requireContext(), "Not kalıcı olarak silindi: ${note.title}")
            } catch (e: Exception) {
                CustomToast.show(requireContext(), "Not silinemedi: ${e.message}")
            }
        }
    }

    private fun restoreAllNotes() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Tümünü Geri Yükle")
            .setMessage("Tüm silinen notları geri yüklemek istediğinizden emin misiniz?")
            .setPositiveButton("Geri Yükle") { _, _ ->
                noteViewModel.restoreAllNotes()
                CustomToast.show(requireContext(), "Tüm notlar geri yüklendi")
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deleteAllNotesPermanently() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Tümünü Kalıcı Sil")
            .setMessage("Tüm silinen notları kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Sil") { _, _ ->
                noteViewModel.deleteAllNotesPermanently()
                CustomToast.show(requireContext(), "Tüm notlar kalıcı olarak silindi")
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}