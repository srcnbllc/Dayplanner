package com.example.dayplanner.ui.trash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dayplanner.utils.CustomToast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.AddNoteActivity
import com.example.dayplanner.NoteAdapter
import com.example.dayplanner.NoteViewModel
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentTrashBinding
import com.example.dayplanner.security.PasswordManager
import kotlinx.coroutines.launch

class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var trashAdapter: NoteAdapter
    private var selectedNotes: MutableSet<Int> = mutableSetOf()
    private var isSelectionMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupBottomBar()
        observeDeletedNotes()
    }

    private fun setupRecyclerView() {
        trashAdapter = NoteAdapter(
            onClick = { note ->
                // Open note for editing (with decryption if needed)
                openNoteForEditing(note)
            },
            onLockToggle = { note, isLocked ->
                // Lock toggle not applicable in trash
                CustomToast.show(requireContext(), "Çöp kutusundaki notlar kilitlenemez")
            },
            onSoftDelete = { note ->
                // Permanent delete
                showPermanentDeleteDialog(note)
            },
            onPinToggle = { note, shouldPin ->
                // Pinleme işlemi çöp kutusunda yok
                CustomToast.show(requireContext(), "Çöp kutusundaki notlar pinlenemez")
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = trashAdapter
    }

    private fun setupBottomBar() {
        binding.selectAllButton.setOnClickListener {
            selectAllNotes()
        }
        
        binding.cancelButton.setOnClickListener {
            exitSelectionMode()
        }
        
        binding.restoreButton.setOnClickListener {
            restoreSelectedNotes()
        }
        
        binding.deletePermanentlyButton.setOnClickListener {
            deleteSelectedNotesPermanently()
        }
        
        updateBottomBarVisibility()
    }

    private fun observeDeletedNotes() {
        noteViewModel.deletedNotes.observe(viewLifecycleOwner) { deletedNotes ->
            trashAdapter.submitList(deletedNotes)
            updateEmptyState(deletedNotes.isEmpty())
            updateBottomBarVisibility()
        }
    }

    private fun openNoteForEditing(note: com.example.dayplanner.Note) {
        if (note.isEncrypted || note.isLocked || note.encryptedBlob != null) {
            showPasswordDialog("Şifreli Not", "Bu notu açmak için şifrenizi girin:") { password ->
                try {
                    val decryptedContent = PasswordManager.decryptNote(
                        String(note.encryptedBlob ?: ByteArray(0)), 
                        password
                    )
                    val intent = Intent(requireContext(), AddNoteActivity::class.java)
                    intent.putExtra("noteId", note.id)
                    intent.putExtra("decryptedContent", decryptedContent)
                    intent.putExtra("isEncrypted", true)
                    startActivity(intent)
                } catch (e: Exception) {
                    CustomToast.show(requireContext(), "Şifre yanlış veya not bozuk")
                }
            }
        } else {
            val intent = Intent(requireContext(), AddNoteActivity::class.java)
            intent.putExtra("noteId", note.id)
            startActivity(intent)
        }
    }

    private fun showTrashNoteOptions(note: com.example.dayplanner.Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Not Seçenekleri")
            .setItems(arrayOf("Geri Yükle", "Kalıcı Olarak Sil", "Düzenle")) { _, which ->
                when (which) {
                    0 -> restoreNote(note)
                    1 -> showPermanentDeleteDialog(note)
                    2 -> openNoteForEditing(note)
                }
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

    private fun showPermanentDeleteDialog(note: com.example.dayplanner.Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Kalıcı Silme")
            .setMessage("'${note.title}' notunu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Sil") { _, _ ->
                deleteNotePermanently(note)
            }
            .setNegativeButton("İptal", null)
            .show()
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

    private fun selectAllNotes() {
        selectedNotes.clear()
        selectedNotes.addAll(trashAdapter.currentList.map { it.id })
        // trashAdapter.updateSelectionMode(isSelectionMode, selectedNotes) // NoteAdapter doesn't have updateSelectionMode
        updateBottomBarVisibility()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedNotes.clear()
        // trashAdapter.updateSelectionMode(isSelectionMode, selectedNotes) // NoteAdapter doesn't have updateSelectionMode
        updateBottomBarVisibility()
    }

    private fun restoreSelectedNotes() {
        if (selectedNotes.isEmpty()) {
            CustomToast.show(requireContext(), "Lütfen geri yüklenecek notları seçin")
            return
        }

        lifecycleScope.launch {
            try {
                selectedNotes.forEach { noteId ->
                    noteViewModel.restoreNote(noteId)
                }
                CustomToast.show(requireContext(), "${selectedNotes.size} not geri yüklendi")
                exitSelectionMode()
            } catch (e: Exception) {
                CustomToast.show(requireContext(), "Notlar geri yüklenemedi: ${e.message}")
            }
        }
    }

    private fun deleteSelectedNotesPermanently() {
        if (selectedNotes.isEmpty()) {
            CustomToast.show(requireContext(), "Lütfen silinecek notları seçin")
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Kalıcı Silme")
            .setMessage("${selectedNotes.size} notu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Sil") { _, _ ->
                lifecycleScope.launch {
                    try {
                        selectedNotes.forEach { noteId ->
                            noteViewModel.deleteNotePermanently(noteId)
                        }
                        CustomToast.show(requireContext(), "${selectedNotes.size} not kalıcı olarak silindi")
                        exitSelectionMode()
                    } catch (e: Exception) {
                        CustomToast.show(requireContext(), "Notlar silinemedi: ${e.message}")
                    }
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun updateBottomBarVisibility() {
        val hasNotes = trashAdapter.currentList.isNotEmpty()
        val hasSelection = selectedNotes.isNotEmpty()
        
        binding.bottomBar.visibility = if (hasNotes) View.VISIBLE else View.GONE
        binding.selectionModeBar.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        
        binding.restoreButton.isEnabled = hasSelection
        binding.deletePermanentlyButton.isEnabled = hasSelection
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showPasswordDialog(title: String, message: String, onPasswordEntered: (String) -> Unit) {
        val passwordInput = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "6 haneli şifre girin"
            filters = arrayOf(android.text.InputFilter.LengthFilter(6))
            textSize = 24f
            gravity = android.view.Gravity.CENTER
            setPadding(50, 20, 50, 20)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setView(passwordInput)
            .setPositiveButton("Aç") { _, _ ->
                val password = passwordInput.text.toString().trim()
                if (com.example.dayplanner.security.PasswordManager.isValidPassword(password)) {
                    onPasswordEntered(password)
                } else {
                    val errorMessage = com.example.dayplanner.security.PasswordManager.getPasswordValidationError(password)
                    CustomToast.show(requireContext(), errorMessage ?: "Geçersiz şifre")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
