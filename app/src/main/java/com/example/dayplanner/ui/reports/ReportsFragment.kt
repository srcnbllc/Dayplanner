package com.example.dayplanner.ui.reports

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.NoteViewModel
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentReportsBinding
import com.example.dayplanner.ExportImportManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.example.dayplanner.utils.CustomToast

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    
    private val noteViewModel: NoteViewModel by viewModels()
    
    // Modern ActivityResultLauncher for file picking
    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            lifecycleScope.launch {
                try {
                    val success = ExportImportManager.importNotes(requireContext(), selectedUri, noteViewModel)
                    
                    if (success) {
                        refreshReports()
                    } else {
                        android.util.Log.e("ReportsFragment", "Import failed")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ReportsFragment", "Import error: ${e.message}", e)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupReports()
        observeData()
    }

    private fun setupReports() {
        // Export button
        binding.exportButton.setOnClickListener {
            exportNotes()
        }
        
        // Import button
        binding.importButton.setOnClickListener {
            importNotes()
        }
        
        // Refresh button
        binding.refreshButton.setOnClickListener {
            refreshReports()
        }
        
        // Trash card click - open trash page
        binding.trashCard.setOnClickListener {
            openTrashPage()
        }
    }

    private fun observeData() {
        // Observe all notes for statistics
        noteViewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            updateStatistics(notes)
        }
        
        // Observe deleted notes for trash statistics
        noteViewModel.deletedNotes.observe(viewLifecycleOwner) { deletedNotes ->
            updateTrashStatistics(deletedNotes)
        }
        
        // Observe finance data
        observeFinanceData()
        
        // Observe password data
        observePasswordData()
    }

    private fun updateStatistics(notes: List<com.example.dayplanner.Note>) {
        val totalNotes = notes.size
        val pinnedNotes = notes.count { it.isPinned }
        val encryptedNotes = notes.count { it.isEncrypted || it.isLocked || it.encryptedBlob != null }
        val recentNotes = notes.count { it.status == "NEW" }
        val regularNotes = notes.count { it.status == "NOTES" }
        
        // Update UI
        binding.totalNotesText.text = totalNotes.toString()
        binding.pinnedNotesText.text = pinnedNotes.toString()
        binding.encryptedNotesText.text = encryptedNotes.toString()
        binding.recentNotesText.text = recentNotes.toString()
        binding.regularNotesText.text = regularNotes.toString()
        
        // Calculate percentages
        val pinnedPercentage = if (totalNotes > 0) (pinnedNotes * 100 / totalNotes) else 0
        val encryptedPercentage = if (totalNotes > 0) (encryptedNotes * 100 / totalNotes) else 0
        
        binding.pinnedPercentageText.text = "$pinnedPercentage%"
        binding.encryptedPercentageText.text = "$encryptedPercentage%"
        
        // Update charts or progress bars
        binding.pinnedProgressBar.progress = pinnedPercentage
        binding.encryptedProgressBar.progress = encryptedPercentage
    }

    private fun updateTrashStatistics(deletedNotes: List<com.example.dayplanner.Note>) {
        val deletedCount = deletedNotes.size
        
        binding.deletedNotesText.text = deletedCount.toString()
        
        // Calculate days until permanent deletion
        val currentTime = System.currentTimeMillis()
        val thirtyDaysInMillis = 30 * 24 * 60 * 60 * 1000L
        
        val notesExpiringSoon = deletedNotes.count { note ->
            val daysSinceDeleted = (currentTime - (note.deletedAt ?: currentTime)) / (24 * 60 * 60 * 1000L)
            daysSinceDeleted >= 25 // Expiring in 5 days or less
        }
        
        binding.expiringSoonText.text = notesExpiringSoon.toString()
        
        // Update trash status
        if (deletedCount == 0) {
            binding.trashStatusText.text = "Çöp kutusu temiz"
            binding.trashStatusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        } else if (notesExpiringSoon > 0) {
            binding.trashStatusText.text = "$notesExpiringSoon not yakında silinecek"
            binding.trashStatusText.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
        } else {
            binding.trashStatusText.text = "Çöp kutusu dolu"
            binding.trashStatusText.setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
        }
    }

    private fun exportNotes() {
        lifecycleScope.launch {
            try {
                val notes = noteViewModel.getAllNotesSync()
                val uri = ExportImportManager.exportNotes(requireContext(), notes)
                
                if (uri != null) {
                    // Export successful
                } else {
                    // Export failed
                }
            } catch (e: Exception) {
                android.util.Log.e("ReportsFragment", "Export error: ${e.message}", e)
            }
        }
    }

    private fun importNotes() {
        // Launch the file picker using modern ActivityResultLauncher
        importLauncher.launch("application/json")
    }
    
    private fun openTrashPage() {
        try {
            // Navigate to TrashActivity instead of fragment to avoid duplicates
            val intent = Intent(requireContext(), com.example.dayplanner.TrashActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("ReportsFragment", "Error opening trash page: ${e.message}", e)
            CustomToast.show(requireContext(), "Çöp kutusu sayfası açılamadı")
        }
    }

    private fun observeFinanceData() {
        try {
            // Get finance data from database
            lifecycleScope.launch {
                val database = com.example.dayplanner.NoteDatabase.getDatabase(requireContext())
                val financeDao = database.financeDao()
                
                val transactions = financeDao.getAllTransactionsSync()
                val totalAmount = transactions.sumOf { it.amount }
                
                binding.totalFinanceRecordsText.text = transactions.size.toString()
                binding.totalAmountText.text = "₺${String.format("%.2f", totalAmount)}"
            }
        } catch (e: Exception) {
            android.util.Log.e("ReportsFragment", "Error loading finance data: ${e.message}", e)
            binding.totalFinanceRecordsText.text = "0"
            binding.totalAmountText.text = "₺0"
        }
    }

    private fun observePasswordData() {
        try {
            // Get password data from database
            lifecycleScope.launch {
                val database = com.example.dayplanner.NoteDatabase.getDatabase(requireContext())
                val passwordDao = database.passwordDao()
                
                val passwords = passwordDao.getAllPasswordsSync()
                val weakPasswords = passwords.count { it.password.length < 8 }
                
                binding.totalPasswordsText.text = passwords.size.toString()
                binding.weakPasswordsText.text = weakPasswords.toString()
            }
        } catch (e: Exception) {
            android.util.Log.e("ReportsFragment", "Error loading password data: ${e.message}", e)
            binding.totalPasswordsText.text = "0"
            binding.weakPasswordsText.text = "0"
        }
    }

    private fun refreshReports() {
        // Trigger data refresh
        observeData()
        // Reports refreshed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

