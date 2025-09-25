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
    private val onSoftDelete: (Note) -> Unit
) :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

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

            // Lock icon: red if locked, green if unlocked
            val lockIcon: ImageView = binding.lockImageView
            val colorRes = if (note.isEncrypted) android.R.color.holo_red_dark else android.R.color.holo_green_dark
            lockIcon.setColorFilter(ContextCompat.getColor(itemView.context, colorRes))

            itemView.setOnClickListener {
                onClick(note)
            }

            // Overflow menu
            binding.overflowButton.setOnClickListener {
                val popup = PopupMenu(itemView.context, binding.overflowButton)
                popup.menuInflater.inflate(R.menu.menu_note_item, popup.menu)
                // Show only relevant action
                popup.menu.findItem(R.id.action_lock).isVisible = !note.isEncrypted
                popup.menu.findItem(R.id.action_unlock).isVisible = note.isEncrypted
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
