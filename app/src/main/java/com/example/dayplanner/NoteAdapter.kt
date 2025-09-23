package com.example.dayplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.databinding.ItemNoteBinding

class NoteAdapter(
        private val onClick: (Note) -> Unit,
        private val onPinToggle: (Note) -> Unit,
        private val onLockToggle: (Note) -> Unit,
        private val onMoreClick: (Note) -> Unit
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
            
            // Şifreli notlar için özel gösterim
            if (note.encryptedBlob != null) {
                binding.descriptionTextView.text = "🔒 Şifrelenmiş not - Açmak için tıklayın"
                binding.descriptionTextView.setTextColor(binding.root.context.getColor(android.R.color.holo_blue_dark))
            } else {
                binding.descriptionTextView.text = note.description
                binding.descriptionTextView.setTextColor(binding.root.context.getColor(android.R.color.black))
            }
            
            binding.dateTextView.text = note.getFormattedDate()

                // Update status indicators
                binding.pinIndicator.visibility = if (note.isPinned) android.view.View.VISIBLE else android.view.View.GONE
                binding.lockIndicator.visibility = if (note.isLocked) android.view.View.VISIBLE else android.view.View.GONE
                binding.encryptedIndicator.visibility = if (note.encryptedBlob != null) android.view.View.VISIBLE else android.view.View.GONE

            // Update button icons and colors
            binding.pinButton.setIconResource(
                if (note.isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )
            binding.pinButton.iconTint = if (note.isPinned) 
                androidx.core.content.ContextCompat.getColorStateList(binding.root.context, android.R.color.holo_blue_bright) 
            else null

            binding.lockButton.setIconResource(
                if (note.isLocked) android.R.drawable.ic_lock_idle_lock else android.R.drawable.ic_lock_lock
            )
            binding.lockButton.iconTint = if (note.isLocked) 
                androidx.core.content.ContextCompat.getColorStateList(binding.root.context, android.R.color.holo_red_dark) 
            else null

            // Show tags if available
            if (!note.tags.isNullOrBlank()) {
                binding.tagsTextView.text = note.tags
                binding.tagsTextView.visibility = android.view.View.VISIBLE
            } else {
                binding.tagsTextView.visibility = android.view.View.GONE
            }

            itemView.setOnClickListener {
                onClick(note)
            }

            // Long press ile pin/encrypt seçenekleri
            itemView.setOnLongClickListener {
                onMoreClick(note)
                true
            }

                binding.pinButton.setOnClickListener { onPinToggle(note) }
                binding.lockButton.setOnClickListener { onLockToggle(note) }
                binding.moreButton.setOnClickListener { onMoreClick(note) }
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
