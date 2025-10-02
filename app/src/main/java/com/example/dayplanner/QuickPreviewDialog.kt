package com.example.dayplanner

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dayplanner.utils.CustomToast
import com.example.dayplanner.utils.getFormattedDate
import androidx.fragment.app.DialogFragment
import com.example.dayplanner.databinding.DialogQuickPreviewBinding
import com.example.dayplanner.security.PasswordManager
import java.text.SimpleDateFormat
import java.util.*

class QuickPreviewDialog : DialogFragment() {

    private var _binding: DialogQuickPreviewBinding? = null
    private val binding get() = _binding!!
    
    private var note: Note? = null
    private var onPinToggle: ((Note) -> Unit)? = null
    private var onEdit: ((Note) -> Unit)? = null
    private var onDelete: ((Note) -> Unit)? = null

    companion object {
        fun newInstance(
            note: Note,
            onPinToggle: (Note) -> Unit,
            onEdit: (Note) -> Unit,
            onDelete: (Note) -> Unit
        ): QuickPreviewDialog {
            val dialog = QuickPreviewDialog()
            dialog.note = note
            dialog.onPinToggle = onPinToggle
            dialog.onEdit = onEdit
            dialog.onDelete = onDelete
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogQuickPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        note?.let { note ->
            setupDialog(note)
        }
    }

    private fun setupDialog(note: Note) {
        // Set note content
        binding.titleText.text = note.title
        
        // Handle encrypted content
        if (note.isEncrypted || note.isLocked || note.encryptedBlob != null) {
            binding.contentText.text = "🔒 Bu not şifrelenmiştir. İçeriği görüntülemek için düzenle butonuna tıklayın."
            binding.contentText.setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
        } else {
            binding.contentText.text = note.description
        }
        
        // Set date
        binding.dateText.text = note.getFormattedDate()
        
        // Set tags
        if (!note.tags.isNullOrBlank()) {
            binding.tagsText.text = "Etiketler: ${note.tags}"
            binding.tagsText.visibility = View.VISIBLE
        } else {
            binding.tagsText.visibility = View.GONE
        }
        
        // Set pin status
        binding.pinButton.text = if (note.isPinned) "Çöz" else "Sabitle"
        binding.pinButton.setIconResource(
            if (note.isPinned) android.R.drawable.ic_menu_edit else android.R.drawable.ic_menu_edit
        )
        
        // Set lock status
        if (note.isEncrypted || note.isLocked || note.encryptedBlob != null) {
            binding.lockStatusText.text = "🔒 Şifrelenmiş"
            binding.lockStatusText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            binding.lockStatusText.text = "🔓 Açık"
            binding.lockStatusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        }
        
        // Button click listeners
        binding.pinButton.setOnClickListener {
            onPinToggle?.invoke(note)
            dismiss()
        }
        
        binding.editButton.setOnClickListener {
            onEdit?.invoke(note)
            dismiss()
        }
        
        binding.deleteButton.setOnClickListener {
            onDelete?.invoke(note)
            dismiss()
        }
        
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
