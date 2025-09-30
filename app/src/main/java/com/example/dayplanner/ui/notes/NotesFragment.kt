package com.example.dayplanner.ui.notes

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.dayplanner.utils.CustomToast
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.addTextChangedListener
import com.example.dayplanner.AddNoteActivity
import com.example.dayplanner.NoteAdapter
import com.example.dayplanner.NoteViewModel
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentNotesBinding
import com.example.dayplanner.security.BiometricHelper
import com.example.dayplanner.security.SecurityKeyManager
import com.example.dayplanner.security.CryptoUtils
import com.example.dayplanner.security.PasswordManager
import com.example.dayplanner.features.ModernFeatures
import com.example.dayplanner.QuickPreviewDialog
import kotlinx.coroutines.launch
import java.security.SecureRandom

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter
    private var allNotesCache: List<com.example.dayplanner.Note> = emptyList()
    private var currentQuery: String = ""
    private var currentStatusFilter: String? = null
    private var isSelectionMode: Boolean = false
    private var selectedNotes: MutableSet<Int> = mutableSetOf()
    private var currentFilter: String = "ALL" // ALL, RECENTLY_ADDED, PINNED, ENCRYPTED, PASSWORD_PROTECTED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            setupRecyclerView()
            setupFab()
            setupSearch()
            setupTopBarFilters()
            setupSelectionMode()
            setupFilterSpinner()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onClick = { note ->
                if (note.isEncrypted || note.encryptedBlob != null) {
                    // Şifreli not - şifre girme ekranı göster
                    showPasswordDialog("Şifreli Not", "Bu notu açmak için şifrenizi girin:") { password ->
                        openEncryptedNote(note, password)
                    }
                } else {
                    // Normal not - düzenleme ekranına git
                    val intent = Intent(requireContext(), AddNoteActivity::class.java)
                    intent.putExtra("noteId", note.id)
                    startActivity(intent)
                }
            },
            onLockToggle = { note, shouldLock ->
                if (shouldLock) {
                    // Şifrele
                    showPasswordDialog("Şifre Belirle", "Notu şifrelemek için 6 haneli şifre belirleyin:") { password ->
                        encryptNote(note, password)
                    }
                } else {
                    // Şifre kaldır
                    showPasswordDialog("Şifreyi Kaldır", "Şifreyi kaldırmak için mevcut şifreyi girin:") { password ->
                        removeEncryption(note, password)
                    }
                }
            },
            onSoftDelete = { note ->
                showDeleteConfirmation(note)
            },
            onPinToggle = { note, shouldPin ->
                if (shouldPin) {
                    // Pinleme - şifre olmadan sadece favorileme/işaretleme
                    pinNote(note)
                } else {
                    // Pin kaldırma - şifre olmadan
                    unpinNote(note)
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = noteAdapter

        // Set up selection listener
        noteAdapter.setOnSelectionChangedListener { selectedIds ->
            selectedNotes.clear()
            selectedNotes.addAll(selectedIds)
        }

        // Observe filtered notes based on current filter
        observeFilteredNotes()
    }

    private fun handleLockUnlock(note: com.example.dayplanner.Note) {
        if (!note.isLocked) {
            // Lock - show password dialog
            showPasswordDialog("Notu Kilitle", "Bu notu kilitlemek için 6 haneli şifre belirleyin:") { password ->
                encryptNote(note, password)
            }
        } else {
            // Unlock - show password dialog
            showPasswordDialog("Kilidi Aç", "Bu notu açmak için şifrenizi girin:") { password ->
                removeEncryption(note, password)
            }
        }
    }


        private fun setupFab() {
            try {
                
                // Yeni Not butonu - Rapid click protection
                binding.addNoteButton.setOnClickListener {
                    try {
                        // Prevent rapid clicks
                        binding.addNoteButton.isEnabled = false
                        
                        val intent = Intent(requireContext(), AddNoteActivity::class.java)
                        startActivity(intent)
                        
                        // Re-enable button after a short delay (using viewLifecycleOwner for safety)
                        binding.addNoteButton.postDelayed({
                            if (isAdded && view != null) {
                                binding.addNoteButton.isEnabled = true
                            }
                        }, 1000)
                    } catch (e: Exception) {
                        android.util.Log.e("NotesFragment", "Error starting AddNoteActivity: ${e.message}", e)
                        binding.addNoteButton.isEnabled = true // Re-enable on error
                    }
                }
                
                // Sil butonu
                binding.deleteButton.setOnClickListener {
                    try {
                        if (isSelectionMode && selectedNotes.isNotEmpty()) {
                            // Already in selection mode with selections - show confirmation
                            showDeleteSelectedConfirmation()
                        } else {
                            // Enter selection mode
                            isSelectionMode = true
                            noteAdapter.setSelectionMode(true)
                            binding.selectionModeLayout.visibility = View.VISIBLE
                            android.util.Log.d("NotesFragment", "Delete button clicked - selection mode enabled")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NotesFragment", "Error in delete button click: ${e.message}", e)
                    }
                }
                
                android.util.Log.d("NotesFragment", "Action buttons setup completed")
            } catch (e: Exception) {
                android.util.Log.e("NotesFragment", "Error setting up action buttons: ${e.message}", e)
            }
        }

    // Folder UI removed in current layout

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { text ->
            currentQuery = text?.toString()?.trim().orEmpty()
            android.util.Log.d("NotesFragment", "Search query: $currentQuery")
            applyFiltersAndSearch()
            showSearchSuggestions()
        }
        
        // Arama alanına odaklanma
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchTips()
            }
        }
    }

    private fun setupTopBarFilters() {
        parentFragmentManager.setFragmentResultListener("filter_status", viewLifecycleOwner) { _, bundle ->
            currentStatusFilter = bundle.getString("status")
            applyFiltersAndSearch()
        }
    }

    private fun observeFilteredNotes() {
        // Use single observer for better performance
        noteViewModel.allNotesSorted.observe(viewLifecycleOwner) { notes ->
            allNotesCache = notes
            updateStatistics(notes)
            applyFiltersAndSearch()
        }
    }

    private fun applyFiltersAndSearch() {
        // Empty list check to prevent crashes
        if (allNotesCache.isEmpty()) {
            android.util.Log.w("NotesFragment", "allNotesCache is empty, skipping filter")
            return
        }
        
        // Use coroutine for better performance
        lifecycleScope.launch {
            try {
                var list = allNotesCache
                
                // Apply filter based on currentFilter for better performance
                list = when (currentFilter) {
                    "RECENTLY_ADDED" -> list.take(50) // Limit to 50 most recent
                    "PINNED" -> list.filter { it.isPinned }
                    "ENCRYPTED" -> list.filter { it.isEncrypted }
                    "PASSWORD_PROTECTED" -> list.filter { it.isEncrypted || it.encryptedBlob != null }
                    else -> list // "ALL" - no additional filtering needed
                }
                
                // Status filtresi
                currentStatusFilter?.let { status ->
                    list = when (status) {
                        "NEW", "NOTES", "DELETED" -> list.filter { it.status == status }
                        else -> list
                    }
                }
                
                // Optimized search - pre-compute lowercase query
                if (currentQuery.isNotEmpty()) {
                    val q = currentQuery.lowercase()
                    list = list.filter { note ->
                        // Pre-compute lowercase values for better performance
                        val titleLower = note.title.lowercase()
                        val descLower = note.description.lowercase()
                        val tagsLower = note.tags?.lowercase()
                        
                        titleLower.contains(q) || 
                        descLower.contains(q) ||
                        (tagsLower?.contains(q) == true) ||
                        // Optimized date search
                        note.createdAt.toString().contains(q) ||
                        // Boolean checks (faster than string operations)
                        (note.isEncrypted && q.contains("şifreli")) ||
                        (note.isLocked && q.contains("kilitli")) ||
                        (note.isPinned && q.contains("sabitlenmiş"))
                    }
                }
                
                // Optimized sorting - only sort if needed
                val sorted = if (list.size > 1) {
                    list.sortedWith(compareByDescending<com.example.dayplanner.Note> { it.isPinned }
                        .thenByDescending { it.createdAt }
                        .thenByDescending { it.id })
                } else {
                    list
                }
                
                // Update UI on main thread
                noteAdapter.submitList(sorted)
                
                // Empty state göster/gizle
                if (sorted.isEmpty()) {
                    binding.emptyStateLayout.visibility = android.view.View.VISIBLE
                    binding.recyclerView.visibility = android.view.View.GONE
                } else {
                    binding.emptyStateLayout.visibility = android.view.View.GONE
                    binding.recyclerView.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                android.util.Log.e("NotesFragment", "Error in applyFiltersAndSearch: ${e.message}", e)
            }
        }
    }

    private fun showSearchSuggestions() {
        if (currentQuery.isNotEmpty()) {
            // Arama önerileri göster
            val suggestions = getSearchSuggestions(currentQuery)
            if (suggestions.isNotEmpty()) {
                // Önerileri göster (basit implementasyon)
                binding.searchEditText.hint = "Öneriler: ${suggestions.take(3).joinToString(", ")}"
            }
        }
    }

    private fun getSearchSuggestions(query: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Etiket önerileri
        if (query.startsWith("#")) {
            suggestions.addAll(ModernFeatures.QuickTags.COMMON_TAGS.filter { 
                it.contains(query.substring(1), true) 
            })
        }
        
        // Tarih önerileri
        val dateKeywords = listOf("bugün", "dün", "yarın", "bu hafta", "geçen hafta")
        dateKeywords.forEach { keyword ->
            if (keyword.contains(query, true)) {
                suggestions.add(keyword)
            }
        }
        
        // Durum önerileri
        val statusKeywords = listOf("tamamlandı", "bekliyor", "önemli", "acil")
        statusKeywords.forEach { keyword ->
            if (keyword.contains(query, true)) {
                suggestions.add(keyword)
            }
        }
        
        return suggestions
    }

        private fun showSearchTips() {
            val tips = ModernFeatures.SearchSuggestions.getSearchTips()
            // Arama ipuçlarını göster (basit implementasyon)
            binding.searchEditText.hint = tips.firstOrNull() ?: "Notlarda ara..."
        }

        private fun showNoteOptionsMenu(note: com.example.dayplanner.Note) {
            val options = mutableListOf<String>()
            
            // Düzenle her zaman mevcut
            options.add("Düzenle")
            
            // Şifrele/Şifre Kaldır
            if (note.isLocked) {
                options.add("Şifreyi Kaldır")
            } else {
                options.add("Şifrele")
            }
            
            // Pin/Unpin
            if (note.isPinned) {
                options.add("Unpin")
            } else {
                options.add("Pin")
            }
            
            // Sil/Geri Yükle
            if (note.status == "DELETED") {
                options.add("Geri Yükle")
                options.add("Kalıcı Olarak Sil")
            } else {
                options.add("Sil")
            }

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Not Seçenekleri")
                .setItems(options.toTypedArray()) { _, which ->
                    // Haptic feedback
                    view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    when (which) {
                        0 -> {
                            // Düzenle
                            val intent = Intent(requireContext(), AddNoteActivity::class.java)
                            intent.putExtra("noteId", note.id)
                            startActivity(intent)
                        }
                        1 -> {
                            // Şifrele/Şifre Kaldır
                            if (note.isEncrypted || note.encryptedBlob != null) {
                                showPasswordDialog("Şifreyi Kaldır", "Şifreyi kaldırmak için mevcut şifreyi girin:") { password ->
                                    removeEncryption(note, password)
                                }
                            } else {
                                showPasswordDialog("Şifre Belirle", "Notu şifrelemek için 6 haneli şifre belirleyin:") { password ->
                                    encryptNote(note, password)
                                }
                            }
                        }
                        2 -> {
                            // Pin/Unpin - gereksiz toast kaldırıldı
                            noteViewModel.setPinned(note.id, !note.isPinned)
                        }
                        3 -> {
                            // Sil/Geri Yükle/Kalıcı Sil
                            if (note.status == "DELETED") {
                                if (options[which] == "Geri Yükle") {
                                    restoreNote(note)
                                } else {
                                    showPermanentDeleteConfirmation(note)
                                }
                            } else {
                                showDeleteConfirmation(note)
                            }
                        }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun showPasswordDialog(title: String, message: String, onConfirm: (String) -> Unit) {
            val input = android.widget.EditText(requireContext())
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            input.hint = "6 haneli şifre girin"
            input.filters = arrayOf(android.text.InputFilter.LengthFilter(6))
            input.textSize = 24f
            input.gravity = android.view.Gravity.CENTER
            input.setPadding(50, 20, 50, 20)

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setView(input)
                .setPositiveButton("Aç") { _, _ ->
                    val password = input.text.toString()
                    if (com.example.dayplanner.security.PasswordManager.isValidPassword(password)) {
                        onConfirm(password)
                    } else {
                        val errorMessage = com.example.dayplanner.security.PasswordManager.getPasswordValidationError(password)
                        CustomToast.show(requireContext(), errorMessage ?: "Geçersiz şifre")
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun encryptNote(note: com.example.dayplanner.Note, password: String) {
            try {
                val content = note.description
                if (content.isEmpty()) {
                    CustomToast.show(requireContext(), "Şifrelenecek içerik bulunamadı")
                    return
                }

                val encryptedContent = PasswordManager.encryptNote(content, password)
                
                val updatedNote = note.copy(
                    isLocked = true,  // Kilitli olarak işaretle
                    isEncrypted = true,
                    description = "", // Şifrelenmiş içerik boş
                    encryptedBlob = encryptedContent.toByteArray()
                )
                noteViewModel.update(updatedNote)
                // Şifreleme başarılı - gereksiz toast kaldırıldı
            } catch (e: Exception) {
                android.util.Log.e("NotesFragment", "Encryption error: ${e.message}", e)
                CustomToast.show(requireContext(), "Şifreleme hatası: ${e.message}")
            }
        }

        private fun removeEncryption(note: com.example.dayplanner.Note, password: String) {
            lifecycleScope.launch {
                try {
                    android.util.Log.d("NotesFragment", "Removing encryption from note: ${note.title}, isEncrypted: ${note.isEncrypted}, isLocked: ${note.isLocked}, hasBlob: ${note.encryptedBlob != null}")
                    
                    // Check if note is actually encrypted - more comprehensive check
                    val isActuallyEncrypted = note.isEncrypted || note.isLocked || note.encryptedBlob != null
                    
                    if (!isActuallyEncrypted) {
                        CustomToast.show(requireContext(), "Bu not zaten şifrelenmemiş")
                        return@launch
                    }

                    // Handle different encryption states
                    val encryptedBlob = note.encryptedBlob
                    if (encryptedBlob == null) {
                        // Note is marked as locked/encrypted but has no encrypted content
                        // This might be a legacy note or corrupted state
                        android.util.Log.w("NotesFragment", "Note marked as encrypted but no encrypted blob found: ${note.title}")
                        
                        // Simply remove encryption flags without password verification
                        val updatedNote = note.copy(
                            isLocked = false,
                            isEncrypted = false,
                            encryptedBlob = null
                        )
                        
                        noteViewModel.update(updatedNote)
                        CustomToast.show(requireContext(), "Şifre kaldırıldı (içerik zaten açıktı)")
                        return@launch
                    }

                    val encryptedContent = String(encryptedBlob)
                    
                    // Verify password before attempting decryption
                    if (!PasswordManager.verifyPassword(encryptedContent, password)) {
                        CustomToast.show(requireContext(), "Yanlış şifre")
                        return@launch
                    }

                    // Decrypt the content
                    val decryptedContent = PasswordManager.decryptNote(encryptedContent, password)
                    
                    // Update note with decrypted content and remove encryption flags
                    val updatedNote = note.copy(
                        isLocked = false,  // Kilitli değil
                        isEncrypted = false,
                        description = decryptedContent,
                        encryptedBlob = null
                    )
                    
                    android.util.Log.d("NotesFragment", "Updated note: isEncrypted: ${updatedNote.isEncrypted}, isLocked: ${updatedNote.isLocked}, hasBlob: ${updatedNote.encryptedBlob != null}")
                    
                    // Save the updated note
                    noteViewModel.update(updatedNote)
                    // Şifre kaldırma başarılı - gereksiz toast kaldırıldı
                    
                    // Log success
                    android.util.Log.d("NotesFragment", "Password removed from note: ${note.title}")
                    
                } catch (e: Exception) {
                    android.util.Log.e("NotesFragment", "Error removing password: ${e.message}", e)
                    CustomToast.show(requireContext(), "Şifre kaldırma hatası: ${e.message}")
                }
            }
        }

        private fun pinNote(note: com.example.dayplanner.Note) {
            lifecycleScope.launch {
                try {
                    val updatedNote = note.copy(isPinned = true)
                    noteViewModel.update(updatedNote)
                    // Pinleme başarılı - gereksiz toast kaldırıldı
                } catch (e: Exception) {
                    android.util.Log.e("NotesFragment", "Pin error: ${e.message}", e)
                    CustomToast.show(requireContext(), "Pinleme hatası: ${e.message}")
                }
            }
        }

        private fun unpinNote(note: com.example.dayplanner.Note) {
            lifecycleScope.launch {
                try {
                    val updatedNote = note.copy(isPinned = false)
                    noteViewModel.update(updatedNote)
                    // Pin kaldırma başarılı - gereksiz toast kaldırıldı
                } catch (e: Exception) {
                    android.util.Log.e("NotesFragment", "Unpin error: ${e.message}", e)
                    CustomToast.show(requireContext(), "Pin kaldırma hatası: ${e.message}")
                }
            }
        }

        private fun openEncryptedNote(note: com.example.dayplanner.Note, password: String) {
            try {
                android.util.Log.d("NotesFragment", "Opening encrypted note: ${note.title}")
                
                // Check if encrypted blob exists
                val encryptedBlob = note.encryptedBlob
                if (encryptedBlob == null) {
                    CustomToast.show(requireContext(), "Şifrelenmiş içerik bulunamadı")
                    return
                }
                
                val encryptedContent = String(encryptedBlob)
                
                // Verify password before attempting decryption
                if (!PasswordManager.verifyPassword(encryptedContent, password)) {
                    CustomToast.show(requireContext(), "Yanlış şifre")
                    return
                }

                // Decrypt the content
                val decryptedContent = PasswordManager.decryptNote(encryptedContent, password)
                
                android.util.Log.d("NotesFragment", "Successfully decrypted note: ${note.title}")
                
                // Open AddNoteActivity with decrypted content
                val intent = Intent(requireContext(), AddNoteActivity::class.java)
                intent.putExtra("noteId", note.id)
                intent.putExtra("isEncrypted", true)
                intent.putExtra("decryptedContent", decryptedContent)
                intent.putExtra("title", note.title) // Pass the title as well
                startActivity(intent)
                
            } catch (e: Exception) {
                android.util.Log.e("NotesFragment", "Error opening encrypted note: ${e.message}", e)
                CustomToast.show(requireContext(), "Not açma hatası: ${e.message}")
            }
        }

        private fun showDeleteConfirmation(note: com.example.dayplanner.Note) {
            // Şifrelenmiş notları silmeyi engelle
            if (note.isEncrypted || note.isLocked || note.encryptedBlob != null) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Şifreli Not Silinemez")
                    .setMessage("Şifreli not silinemez. Öncelikle şifreyi kaldırınız.")
                    .setPositiveButton("Tamam", null)
                    .show()
                return
            }
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Notu Sil")
                .setMessage("Bu not silinecek ve 30 gün sonra kalıcı olarak silinecek. Geri yüklemek için 'Silinenler' bölümünü kontrol edin.")
                .setPositiveButton("Sil") { _, _ ->
                    softDeleteNote(note)
                    CustomToast.show(requireContext(), "Not silindi (30 gün sonra kalıcı olarak silinecek)")
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun showPermanentDeleteConfirmation(note: com.example.dayplanner.Note) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Kalıcı Olarak Sil")
                .setMessage("Bu notu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz!")
                .setPositiveButton("Kalıcı Olarak Sil") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            noteViewModel.delete(note)
                        } catch (e: Exception) {
                            android.util.Log.e("NotesFragment", "Error permanently deleting note: ${e.message}", e)
                        }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun softDeleteNote(note: com.example.dayplanner.Note) {
            lifecycleScope.launch {
                try {
                    noteViewModel.softDeleteById(note.id)
                    CustomToast.show(requireContext(), "Not çöp kutusuna taşındı")
                } catch (e: Exception) {
                    android.util.Log.e("NotesFragment", "Error soft deleting note: ${e.message}", e)
                    CustomToast.show(requireContext(), "Silme hatası: ${e.message}")
                }
            }
        }

        private fun restoreNote(note: com.example.dayplanner.Note) {
            lifecycleScope.launch {
                try {
                    noteViewModel.restoreNote(note.id)
                } catch (e: Exception) {
                    android.util.Log.e("NotesFragment", "Error restoring note: ${e.message}", e)
                }
            }
        }

        private fun setupSelectionMode() {
            // Selection mode buttons
            binding.cancelSelectionButton.setOnClickListener {
                exitSelectionMode()
            }
            
            binding.selectAllButton.setOnClickListener {
                toggleSelectAll()
            }
            
        }

        private fun selectAllNotes() {
            selectedNotes.clear()
            noteAdapter.currentList.forEach { note ->
                selectedNotes.add(note.id)
            }
            noteAdapter.setSelectedNotes(selectedNotes)
        }

        private fun deselectAllNotes() {
            selectedNotes.clear()
            noteAdapter.setSelectedNotes(selectedNotes)
        }

        private fun exitSelectionMode() {
            isSelectionMode = false
            selectedNotes.clear()
            noteAdapter.setSelectionMode(false)
            binding.selectionModeLayout.visibility = View.GONE
        }


        private fun showDeleteSelectedConfirmation() {
            if (selectedNotes.isEmpty()) {
                return
            }
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Seçilen Notları Sil")
                .setMessage("${selectedNotes.size} not silinecek ve silinenler bölümüne taşınacak. Emin misiniz?")
                .setPositiveButton("Sil") { _, _ ->
                    deleteSelectedNotes()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun toggleSelectAll() {
            val allNoteIds = allNotesCache.map { it.id }.toSet()
            val isAllSelected = allNoteIds.all { selectedNotes.contains(it) }
            
            if (isAllSelected) {
                // Deselect all
                selectedNotes.clear()
                noteAdapter.setSelectedNotes(emptySet())
                binding.selectAllButton.text = "Tümü"
            } else {
                // Select all notes
                selectedNotes.addAll(allNoteIds)
                noteAdapter.setSelectedNotes(allNoteIds)
                binding.selectAllButton.text = "Seçimi Kaldır"
            }
        }

        private fun deleteSelectedNotes() {
            lifecycleScope.launch {
                try {
                    var successCount = 0
                    var encryptedCount = 0
                    
                    selectedNotes.forEach { noteId ->
                        // Şifrelenmiş notları kontrol et
                        val note = allNotesCache.find { it.id == noteId }
                        if (note != null && (note.isEncrypted || note.isLocked || note.encryptedBlob != null)) {
                            encryptedCount++
                        } else {
                            val result = noteViewModel.moveToTrashIfDecrypted(noteId)
                            if (result.isSuccess) {
                                successCount++
                            }
                        }
                    }
                    
                    exitSelectionMode()
                    
                    // Sonuç mesajı
                    if (encryptedCount > 0) {
                        CustomToast.show(requireContext(), "$successCount not silindi, $encryptedCount şifreli not silinemedi")
                    } else {
                        CustomToast.show(requireContext(), "$successCount not silindi")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NotesFragment", "Error deleting selected notes: ${e.message}", e)
                }
            }
        }



        private fun setupFilterSpinner() {
            val filterOptions = listOf(
                "Tümü",
                "Son Eklenen", 
                "Şifreli",
                "Pinlenmiş"
            )
            
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filterOptions)
            binding.filterSpinner.setAdapter(adapter)
            
            // Use setOnItemClickListener for AutoCompleteTextView dropdown selection
            binding.filterSpinner.setOnItemClickListener { parent, view, position, id ->
                when (position) {
                    0 -> currentFilter = "ALL"
                    1 -> currentFilter = "RECENTLY_ADDED"
                    2 -> currentFilter = "ENCRYPTED"
                    3 -> currentFilter = "PINNED"
                }
                observeFilteredNotes()
            }
            
            // Default selection
            binding.filterSpinner.setText(filterOptions[0], false)
        }

        private fun showQuickPreview(note: com.example.dayplanner.Note) {
            val dialog = QuickPreviewDialog.newInstance(
                note = note,
                onPinToggle = { note ->
                    noteViewModel.setPinned(note.id, !note.isPinned)
                    CustomToast.show(requireContext(), "Pin durumu değiştirildi: ${note.title}")
                },
                onEdit = { note ->
                    if (note.isEncrypted || note.encryptedBlob != null) {
                        showPasswordDialog("Şifreli Not", "Bu notu açmak için şifrenizi girin:") { password ->
                            openEncryptedNote(note, password)
                        }
                    } else {
                        val intent = Intent(requireContext(), AddNoteActivity::class.java)
                        intent.putExtra("noteId", note.id)
                        startActivity(intent)
                    }
                },
                onDelete = { note ->
                    showDeleteConfirmation(note)
                }
            )
            dialog.show(parentFragmentManager, "quick_preview")
        }


        private fun updateStatistics(notes: List<com.example.dayplanner.Note>) {
            try {
                val totalNotes = notes.size
                val pinnedNotes = notes.count { it.isPinned }
                val recentNotes = notes.count { 
                    val currentTime = System.currentTimeMillis()
                    val weekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
                    it.createdAt > weekAgo
                }
                
                // Update UI with proper text colors for visibility
                binding.totalNotesCount.text = totalNotes.toString()
                binding.pinnedNotesCount.text = pinnedNotes.toString()
                binding.recentNotesCount.text = recentNotes.toString()
                
                android.util.Log.d("NotesFragment", "Statistics updated: Total=$totalNotes, Pinned=$pinnedNotes, Recent=$recentNotes")
            } catch (e: Exception) {
                android.util.Log.e("NotesFragment", "Error updating statistics: ${e.message}", e)
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
}
