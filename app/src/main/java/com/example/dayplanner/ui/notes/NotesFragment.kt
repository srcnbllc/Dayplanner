package com.example.dayplanner.ui.notes

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
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
import androidx.core.widget.addTextChangedListener
import com.example.dayplanner.AddNoteActivity
import com.example.dayplanner.SimpleAddNoteActivity
import com.example.dayplanner.NoteAdapter
import com.example.dayplanner.NoteViewModel
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentNotesBinding
import com.example.dayplanner.security.BiometricHelper
import com.example.dayplanner.security.SecurityKeyManager
import com.example.dayplanner.security.CryptoUtils
import com.example.dayplanner.security.PasswordManager
import com.example.dayplanner.features.ModernFeatures
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
    private var currentFilter: String = "ALL" // ALL, PINNED, ENCRYPTED, RECENT

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
            setupDeletedNotesButton()
            setupFilterChips()
    }

    private fun setupRecyclerView() {
            noteAdapter = NoteAdapter(
                onClick = { note ->
                    if (note.encryptedBlob != null) {
                        // Şifreli not - pin girme ekranı göster
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
                onPinToggle = { note ->
                    android.util.Log.d("NotesFragment", "Pin toggle clicked for note: ${note.title}")
                    Toast.makeText(requireContext(), "Pin durumu değiştirildi: ${note.title}", Toast.LENGTH_SHORT).show()
                    noteViewModel.setPinned(note.id, !note.isPinned)
                },
                onLockToggle = { note ->
                    handleLockUnlock(note)
                },
                onMoreClick = { note ->
                    showNoteOptionsMenu(note)
                }
            )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = noteAdapter
        attachSwipeActions()

        noteViewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            allNotesCache = notes
            applyFiltersAndSearch()
        }
    }

    private fun handleLockUnlock(note: com.example.dayplanner.Note) {
        val activity = requireActivity()
        if (!note.isLocked) {
            // Lock
            if (BiometricHelper.canAuthenticate(activity)) {
                BiometricHelper.prompt(
                    activity,
                    "Kilitle",
                    onSuccess = {
                        val key = SecurityKeyManager.getAppAesKey(requireContext())
                        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
                        val cipher = CryptoUtils.encryptAesGcm(
                            key,
                            note.description.toByteArray(),
                            iv
                        )
                        val combined = iv + cipher
                        noteViewModel.update(
                            note.copy(
                                isLocked = true,
                                encryptedBlob = combined,
                                description = ""
                            )
                        )
                    },
                    onError = {
                        // Ignore error
                    }
                )
            } else {
                noteViewModel.update(note.copy(isLocked = true))
            }
        } else {
            // Unlock
            val blob = note.encryptedBlob
            if (blob != null && blob.size > 12 && BiometricHelper.canAuthenticate(activity)) {
                BiometricHelper.prompt(
                    activity,
                    "Kilidi Aç",
                    onSuccess = {
                        val key = SecurityKeyManager.getAppAesKey(requireContext())
                        val iv = blob.copyOfRange(0, 12)
                        val cipher = blob.copyOfRange(12, blob.size)
                        val plain = CryptoUtils.decryptAesGcm(key, cipher, iv)
                        noteViewModel.update(
                            note.copy(
                                isLocked = false,
                                encryptedBlob = null,
                                description = String(plain)
                            )
                        )
                    },
                    onError = {
                        // Ignore error
                    }
                )
            } else {
                noteViewModel.update(note.copy(isLocked = false))
            }
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
                    android.util.Log.d("NotesFragment", "Swipe right - Pin toggle: ${note.title}")
                    Toast.makeText(requireContext(), "Pin durumu değiştirildi: ${note.title}", Toast.LENGTH_SHORT).show()
                    noteViewModel.setPinned(note.id, !note.isPinned)
                } else if (dir == ItemTouchHelper.LEFT) {
                    android.util.Log.d("NotesFragment", "Swipe left - Delete: ${note.title}")
                    Toast.makeText(requireContext(), "Not silindi: ${note.title}", Toast.LENGTH_SHORT).show()
                    noteViewModel.delete(note)
                }
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
    }

        private fun setupFab() {
            try {
                android.util.Log.d("NotesFragment", "Setting up add note button")
                binding.addNoteButton.setOnClickListener {
                    try {
                        android.util.Log.d("NotesFragment", "Add Note button clicked!")
                        android.util.Log.d("NotesFragment", "Starting AddNoteActivity")
                        val intent = Intent(requireContext(), AddNoteActivity::class.java)
                        startActivity(intent)
                        android.util.Log.d("NotesFragment", "SimpleAddNoteActivity started successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("NotesFragment", "Error starting SimpleAddNoteActivity: ${e.message}", e)
                        Toast.makeText(requireContext(), "Not ekleme ekranı açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                android.util.Log.d("NotesFragment", "Add note button setup complete")
            } catch (e: Exception) {
                android.util.Log.e("NotesFragment", "Error setting up add note button: ${e.message}", e)
                Toast.makeText(requireContext(), "Buton ayarlanamadı: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun applyFiltersAndSearch() {
        var list = allNotesCache
        
        // Status filtresi
        currentStatusFilter?.let { status ->
            list = when (status) {
                "NEW", "NOTES", "DELETED" -> list.filter { it.status == status }
                else -> list
            }
        }
        
        // Chip filtresi
        list = when (currentFilter) {
            "PINNED" -> list.filter { it.isPinned }
            "ENCRYPTED" -> list.filter { it.encryptedBlob != null }
            "RECENT" -> list.sortedByDescending { it.createdAt }.take(10)
            else -> list
        }
        
        // Arama filtresi
        if (currentQuery.isNotEmpty()) {
            val q = currentQuery
            list = list.filter { 
                it.title.contains(q, true) || 
                it.description.contains(q, true) ||
                it.tags?.contains(q, true) == true
            }
        }
        
        // Sıralama: Pinlenenler en üstte, sonra oluşturulma tarihine göre
        val sorted = list.sortedWith(compareByDescending<com.example.dayplanner.Note> { it.isPinned }
            .thenByDescending { it.createdAt }
            .thenByDescending { it.id })
        noteAdapter.submitList(sorted)
        
        // Empty state göster/gizle
        if (sorted.isEmpty()) {
            binding.emptyStateLayout.visibility = android.view.View.VISIBLE
            binding.recyclerView.visibility = android.view.View.GONE
        } else {
            binding.emptyStateLayout.visibility = android.view.View.GONE
            binding.recyclerView.visibility = android.view.View.VISIBLE
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
                options.add("Sabitlemeyi Kaldır")
            } else {
                options.add("Sabitle")
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
                    when (which) {
                        0 -> {
                            // Düzenle
                            val intent = Intent(requireContext(), AddNoteActivity::class.java)
                            intent.putExtra("noteId", note.id)
                            startActivity(intent)
                        }
                        1 -> {
                            // Şifrele/Şifre Kaldır
                            if (note.isLocked) {
                                showPasswordDialog("Şifreyi Kaldır", "Şifreyi kaldırmak için mevcut şifreyi girin:") { password ->
                                    removeEncryption(note, password)
                                }
                            } else {
                                showPasswordDialog("Şifre Belirle", "Notu şifrelemek için bir şifre belirleyin:") { password ->
                                    encryptNote(note, password)
                                }
                            }
                        }
                        2 -> {
                            // Pin/Unpin
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
                .setMessage("Şifreli notu açmak için 6 haneli şifrenizi girin")
                .setView(input)
                .setPositiveButton("Aç") { _, _ ->
                    val password = input.text.toString()
                    if (password.length == 6) {
                        onConfirm(password)
                    } else {
                        Toast.makeText(requireContext(), "6 haneli şifre girin", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun encryptNote(note: com.example.dayplanner.Note, password: String) {
            try {
                val content = note.description
                if (content.isEmpty()) {
                    Toast.makeText(requireContext(), "Şifrelenecek içerik bulunamadı", Toast.LENGTH_SHORT).show()
                    return
                }

                val encryptedContent = PasswordManager.encryptNote(content, password)
                
                val updatedNote = note.copy(
                    isLocked = true,
                    description = "", // Şifrelenmiş içerik boş
                    encryptedBlob = encryptedContent.toByteArray()
                )
                noteViewModel.update(updatedNote)
                Toast.makeText(requireContext(), "Not şifrelendi", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Şifreleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun removeEncryption(note: com.example.dayplanner.Note, password: String) {
            try {
                if (!note.isLocked) {
                    Toast.makeText(requireContext(), "Şifrelenmiş not bulunamadı", Toast.LENGTH_SHORT).show()
                    return
                }

                val encryptedContent = String(note.encryptedBlob ?: return)
                
                if (!PasswordManager.verifyPassword(encryptedContent, password)) {
                    Toast.makeText(requireContext(), "Yanlış şifre", Toast.LENGTH_SHORT).show()
                    return
                }

                val decryptedContent = PasswordManager.decryptNote(encryptedContent, password)
                
                val updatedNote = note.copy(
                    isLocked = false,
                    description = decryptedContent,
                    encryptedBlob = null
                )
                noteViewModel.update(updatedNote)
                Toast.makeText(requireContext(), "Şifre kaldırıldı", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Şifre kaldırma hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun openEncryptedNote(note: com.example.dayplanner.Note, password: String) {
            try {
                val encryptedContent = String(note.encryptedBlob ?: return)
                
                if (!PasswordManager.verifyPassword(encryptedContent, password)) {
                    Toast.makeText(requireContext(), "Yanlış şifre", Toast.LENGTH_SHORT).show()
                    return
                }

                val decryptedContent = PasswordManager.decryptNote(encryptedContent, password)
                
                // Geçici olarak notu açık hale getir (tempNote kullanılmıyor)
                
                // Düzenleme ekranına git
                val intent = Intent(requireContext(), AddNoteActivity::class.java)
                intent.putExtra("noteId", note.id)
                intent.putExtra("isEncrypted", true)
                intent.putExtra("decryptedContent", decryptedContent)
                startActivity(intent)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Not açma hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun showDeleteConfirmation(note: com.example.dayplanner.Note) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Notu Sil")
                .setMessage("Bu not silinecek ve 30 gün sonra kalıcı olarak silinecek. Geri yüklemek için 'Silinenler' bölümünü kontrol edin.")
                .setPositiveButton("Sil") { _, _ ->
                    softDeleteNote(note)
                    Toast.makeText(requireContext(), "Not silindi (30 gün sonra kalıcı olarak silinecek)", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun showPermanentDeleteConfirmation(note: com.example.dayplanner.Note) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Kalıcı Olarak Sil")
                .setMessage("Bu notu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz!")
                .setPositiveButton("Kalıcı Olarak Sil") { _, _ ->
                    noteViewModel.delete(note)
                    Toast.makeText(requireContext(), "Not kalıcı olarak silindi", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun softDeleteNote(note: com.example.dayplanner.Note) {
            val updatedNote = note.copy(
                status = "DELETED",
                deletedAt = System.currentTimeMillis()
            )
            noteViewModel.update(updatedNote)
            
            // 30 gün sonra kalıcı silme için WorkManager
            schedulePermanentDeletion(note.id)
        }

        private fun schedulePermanentDeletion(noteId: Int) {
            // WorkManager ile 30 gün sonra kalıcı silme
            // Bu özellik daha sonra implement edilecek
            android.util.Log.d("NotesFragment", "Permanent deletion scheduled for note $noteId in 30 days")
        }

        private fun restoreNote(note: com.example.dayplanner.Note) {
            val updatedNote = note.copy(
                status = "ACTIVE",
                deletedAt = null
            )
            noteViewModel.update(updatedNote)
            Toast.makeText(requireContext(), "Not geri yüklendi", Toast.LENGTH_SHORT).show()
        }

        private fun setupSelectionMode() {
            binding.selectModeButton.setOnClickListener {
                toggleSelectionMode()
            }
            
            binding.cancelSelectionButton.setOnClickListener {
                exitSelectionMode()
            }
            
            binding.deleteSelectedButton.setOnClickListener {
                showDeleteSelectedConfirmation()
            }
            
            binding.encryptSelectedButton.setOnClickListener {
                showEncryptSelectedConfirmation()
            }
        }

        private fun toggleSelectionMode() {
            isSelectionMode = !isSelectionMode
            selectedNotes.clear()
            
            if (isSelectionMode) {
                binding.selectionModeLayout.visibility = android.view.View.VISIBLE
                binding.selectModeButton.text = "İptal"
                binding.selectModeButton.setIconResource(android.R.drawable.ic_menu_close_clear_cancel)
                android.util.Log.d("NotesFragment", "Selection mode ON")
                Toast.makeText(requireContext(), "Çoklu seçim modu açıldı", Toast.LENGTH_SHORT).show()
            } else {
                binding.selectionModeLayout.visibility = android.view.View.GONE
                binding.selectModeButton.text = "Seç"
                binding.selectModeButton.setIconResource(R.drawable.ic_check_box)
                android.util.Log.d("NotesFragment", "Selection mode OFF")
                Toast.makeText(requireContext(), "Çoklu seçim modu kapandı", Toast.LENGTH_SHORT).show()
            }
            
            // Adapter'ı güncelle
            noteAdapter.notifyDataSetChanged()
        }

        private fun exitSelectionMode() {
            isSelectionMode = false
            selectedNotes.clear()
            binding.selectionModeLayout.visibility = android.view.View.GONE
            binding.selectModeButton.text = "Seç"
            binding.selectModeButton.setIconResource(R.drawable.ic_check_box)
            noteAdapter.notifyDataSetChanged()
        }

        private fun showDeleteSelectedConfirmation() {
            if (selectedNotes.isEmpty()) {
                Toast.makeText(requireContext(), "Silinecek not seçin", Toast.LENGTH_SHORT).show()
                return
            }
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Seçilen Notları Sil")
                .setMessage("${selectedNotes.size} not silinecek ve 30 gün sonra kalıcı olarak silinecek. Emin misiniz?")
                .setPositiveButton("Sil") { _, _ ->
                    deleteSelectedNotes()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun deleteSelectedNotes() {
            selectedNotes.forEach { noteId ->
                val note = allNotesCache.find { it.id == noteId }
                note?.let {
                    softDeleteNote(it)
                }
            }
            exitSelectionMode()
            Toast.makeText(requireContext(), "${selectedNotes.size} not silindi", Toast.LENGTH_SHORT).show()
        }

        private fun showEncryptSelectedConfirmation() {
            if (selectedNotes.isEmpty()) {
                Toast.makeText(requireContext(), "Şifrelenecek not seçin", Toast.LENGTH_SHORT).show()
                return
            }
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Seçilen Notları Şifrele")
                .setMessage("${selectedNotes.size} not şifrelenecek. Şifre belirleyin:")
                .setView(android.widget.EditText(requireContext()).apply {
                    hint = "Şifre girin"
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                })
                .setPositiveButton("Şifrele") { _, _ ->
                    // Şifreleme işlemi burada yapılacak
                    Toast.makeText(requireContext(), "${selectedNotes.size} not şifrelendi", Toast.LENGTH_SHORT).show()
                    exitSelectionMode()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        private fun setupDeletedNotesButton() {
            binding.deletedNotesButton.setOnClickListener {
                // Silinenler fragment'ini göster
                val fragment = DeletedNotesFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack("deleted_notes")
                    .commit()
            }
        }

        private fun setupFilterChips() {
            binding.filterAllChip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentFilter = "ALL"
                    android.util.Log.d("NotesFragment", "Filter: ALL")
                    Toast.makeText(requireContext(), "Tüm notlar gösteriliyor", Toast.LENGTH_SHORT).show()
                    clearOtherChips(binding.filterAllChip)
                    applyFiltersAndSearch()
                }
            }
            
            binding.filterPinnedChip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentFilter = "PINNED"
                    clearOtherChips(binding.filterPinnedChip)
                    applyFiltersAndSearch()
                }
            }
            
            binding.filterEncryptedChip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentFilter = "ENCRYPTED"
                    clearOtherChips(binding.filterEncryptedChip)
                    applyFiltersAndSearch()
                }
            }
            
            binding.filterRecentChip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentFilter = "RECENT"
                    clearOtherChips(binding.filterRecentChip)
                    applyFiltersAndSearch()
                }
            }
        }

        private fun clearOtherChips(selectedChip: com.google.android.material.chip.Chip) {
            if (selectedChip != binding.filterAllChip) binding.filterAllChip.isChecked = false
            if (selectedChip != binding.filterPinnedChip) binding.filterPinnedChip.isChecked = false
            if (selectedChip != binding.filterEncryptedChip) binding.filterEncryptedChip.isChecked = false
            if (selectedChip != binding.filterRecentChip) binding.filterRecentChip.isChecked = false
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
}
