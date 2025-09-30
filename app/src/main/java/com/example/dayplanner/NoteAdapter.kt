package com.example.dayplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.databinding.ItemNoteBinding

class NoteAdapter(
    private val onClick: (Note) -> Unit,
    private val onLockToggle: (Note, Boolean) -> Unit,
    private val onSoftDelete: (Note) -> Unit,
    private val onPinToggle: (Note, Boolean) -> Unit
) :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    private var isSelectionMode = false
    private var selectedNotes = mutableSetOf<Int>()
    private var onSelectionChanged: ((Set<Int>) -> Unit)? = null

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        if (!enabled) {
            selectedNotes.clear()
        }
        notifyItemRangeChanged(0, itemCount)
    }

    fun setSelectedNotes(notes: Set<Int>) {
        selectedNotes.clear()
        selectedNotes.addAll(notes)
        notifyItemRangeChanged(0, itemCount)
    }

    fun getSelectedNotes(): Set<Int> = selectedNotes.toSet()

    fun setOnSelectionChangedListener(listener: (Set<Int>) -> Unit) {
        onSelectionChanged = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.titleTextView.text = note.title
            binding.descriptionTextView.text = note.description
            binding.dateTextView.text = note.getFormattedDate() // Formatlı tarih gösterimi

            // Selection checkbox - optimized
            binding.selectionCheckbox.visibility = if (isSelectionMode) android.view.View.VISIBLE else android.view.View.GONE
            binding.selectionCheckbox.isChecked = selectedNotes.contains(note.id)
            
            // Only set listener once, not on every bind
            if (binding.selectionCheckbox.tag == null) {
                binding.selectionCheckbox.tag = true
                binding.selectionCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    val noteId = getItem(bindingAdapterPosition).id
                    if (isChecked) {
                        selectedNotes.add(noteId)
                    } else {
                        selectedNotes.remove(noteId)
                    }
                    onSelectionChanged?.invoke(selectedNotes.toSet())
                }
            }

            // Lock icon: show only if locked/encrypted, red if locked
            val lockIcon: ImageView = binding.lockImageView
            val isLocked = note.isLocked || note.isEncrypted || note.encryptedBlob != null
            lockIcon.visibility = if (isLocked) android.view.View.VISIBLE else android.view.View.GONE
            if (isLocked) {
                // Kite Design kırmızı renk kullan
                val colorRes = R.color.kite_red
                lockIcon.setColorFilter(ContextCompat.getColor(itemView.context, colorRes))
            }

            // Pin icon: show if pinned, green color
            val pinIcon: ImageView = binding.pinImageView
            pinIcon.visibility = if (note.isPinned) android.view.View.VISIBLE else android.view.View.GONE
            if (note.isPinned) {
                // Kite Design yeşil renk kullan
                val colorRes = R.color.kite_green
                pinIcon.setColorFilter(ContextCompat.getColor(itemView.context, colorRes))
            }

            itemView.setOnClickListener {
                if (isSelectionMode) {
                    binding.selectionCheckbox.isChecked = !binding.selectionCheckbox.isChecked
                } else {
                    onClick(note)
                }
            }

            // Overflow menu
            binding.overflowButton.setOnClickListener {
                val popup = PopupMenu(itemView.context, binding.overflowButton)
                popup.menuInflater.inflate(R.menu.menu_note_item, popup.menu)
                
                // Show only relevant actions - single option per action type
                val isLocked = note.isLocked || note.isEncrypted || note.encryptedBlob != null
                popup.menu.findItem(R.id.action_lock).isVisible = !isLocked
                popup.menu.findItem(R.id.action_unlock).isVisible = isLocked
                popup.menu.findItem(R.id.action_pin).isVisible = !note.isPinned
                popup.menu.findItem(R.id.action_unpin).isVisible = note.isPinned
                
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_lock -> {
                            onLockToggle(note, true)
                            true
                        }
                        R.id.action_unlock -> {
                            onLockToggle(note, false)
                            true
                        }
                        R.id.action_pin -> {
                            onPinToggle(note, true)
                            true
                        }
                        R.id.action_unpin -> {
                            onPinToggle(note, false)
                            true
                        }
                        R.id.action_delete -> {
                            onSoftDelete(note)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
