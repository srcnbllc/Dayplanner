package com.example.dayplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dayplanner.utils.CustomToast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.databinding.ItemNoteBinding

class NoteAdapter(
    private val onClick: (Note) -> Unit,          // Normal tıklama → Notu aç
    private val onPinToggle: (Note) -> Unit,      // Pin butonu → sabitle/çöz
    private val onLockToggle: (Note) -> Unit,     // Lock butonu → kilitle/çöz
    private val onMoreClick: (Note) -> Unit,      // Daha fazla seçenek
    private val onLongPress: (Note) -> Unit,      // Long press → Quick preview
    private val onSwipeLeft: (Note) -> Unit,      // Swipe left → Delete
    private val onSwipeRight: (Note) -> Unit,     // Swipe right → Pin/Unpin
    private val onSelectionModeChanged: (Boolean) -> Unit = {}, // Selection mode callback
    private var isSelectionMode: Boolean = false,  // Seçim modu
    private var selectedNotes: MutableSet<Int> = mutableSetOf()  // Seçili notlar
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }
    
    fun updateSelectionMode(isSelectionMode: Boolean, selectedNotes: MutableSet<Int>) {
        this.isSelectionMode = isSelectionMode
        this.selectedNotes = selectedNotes
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.titleTextView.text = note.title

            // 🔐 Şifreli not ise kullanıcıya özel mesaj göster
            if (note.isEncrypted || note.encryptedBlob != null) {
                binding.descriptionTextView.text = "🔒 Şifreli not"
                binding.descriptionTextView.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.holo_green_dark)
                )
            } else if (note.isLocked) {
                binding.descriptionTextView.text = "🔒 Kilitli not - Açmak için tıklayın"
                binding.descriptionTextView.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.holo_red_dark)
                )
            } else {
                binding.descriptionTextView.text = note.description
                binding.descriptionTextView.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.black)
                )
            }

            // Tarih formatlı göster
            binding.dateTextView.text = note.getFormattedDate()

            // Durum ikonları
            binding.pinIndicator.visibility = if (note.isPinned) View.VISIBLE else View.GONE
            
            // Kilit simgesi - şifreli ise yeşil, kilitli ise kırmızı
            binding.lockIndicator.visibility = View.VISIBLE
            if (note.isEncrypted || note.encryptedBlob != null) {
                // Encrypted - show green lock icon
                binding.lockIndicator.setImageResource(android.R.drawable.ic_lock_idle_lock)
                binding.lockIndicator.setColorFilter(ContextCompat.getColor(binding.root.context, android.R.color.holo_green_dark))
            } else if (note.isLocked) {
                // Locked - show red lock icon
                binding.lockIndicator.setImageResource(android.R.drawable.ic_lock_lock)
                binding.lockIndicator.setColorFilter(ContextCompat.getColor(binding.root.context, android.R.color.holo_red_dark))
            } else {
                // Unlocked - show open lock icon in green
                binding.lockIndicator.setImageResource(android.R.drawable.ic_lock_idle_lock)
                binding.lockIndicator.setColorFilter(ContextCompat.getColor(binding.root.context, android.R.color.holo_green_dark))
            }

            // Pin butonu - pin işareti ile
            binding.pinButton.setIconResource(
                if (note.isPinned) R.drawable.ic_pin else R.drawable.ic_pin
            )
            binding.pinButton.iconTint = if (note.isPinned) {
                ContextCompat.getColorStateList(binding.root.context, android.R.color.holo_blue_bright)
            } else {
                ContextCompat.getColorStateList(binding.root.context, android.R.color.darker_gray)
            }

            // Lock butonu
            binding.lockButton.setIconResource(
                if (note.isLocked) android.R.drawable.ic_lock_idle_lock else android.R.drawable.ic_lock_lock
            )
            binding.lockButton.iconTint = if (note.isLocked) {
                ContextCompat.getColorStateList(binding.root.context, android.R.color.holo_red_dark)
            } else null

            // Etiketler
            if (!note.tags.isNullOrBlank()) {
                binding.tagsTextView.text = note.tags
                binding.tagsTextView.visibility = View.VISIBLE
            } else {
                binding.tagsTextView.visibility = View.GONE
            }

            // Checkbox sistemi
            binding.selectionCheckBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            binding.selectionCheckBox.isChecked = selectedNotes.contains(note.id)
            
            binding.selectionCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedNotes.add(note.id)
                } else {
                    selectedNotes.remove(note.id)
                }
            }

            // 🖱️ Normal tıklama → Not açma (seçim modunda değilse)
            itemView.setOnClickListener { 
                if (!isSelectionMode) {
                    onClick(note)
                } else {
                    // Seçim modunda checkbox'ı toggle et
                    binding.selectionCheckBox.isChecked = !binding.selectionCheckBox.isChecked
                }
            }

            // 📌 Pin toggle
            binding.pinButton.setOnClickListener {
                onPinToggle(note)
            }

            // 🔐 Lock toggle
            binding.lockButton.setOnClickListener {
                onLockToggle(note)
            }

            // ⋮ Daha fazla seçenek
            binding.moreButton.setOnClickListener {
                onMoreClick(note)
            }

            // 📌 Uzun basma → Selection mode veya Quick preview
            itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    // Enter selection mode
                    isSelectionMode = true
                    selectedNotes.add(note.id)
                    onSelectionModeChanged(true)
                    notifyDataSetChanged()
                } else {
                    // Toggle selection
                    if (selectedNotes.contains(note.id)) {
                        selectedNotes.remove(note.id)
                    } else {
                        selectedNotes.add(note.id)
                    }
                    notifyDataSetChanged()
                }
                true
            }
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        if (!enabled) {
            selectedNotes.clear()
        }
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): Set<Int> = selectedNotes.toSet()

    fun clearSelection() {
        selectedNotes.clear()
        notifyDataSetChanged()
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem
    }
}
