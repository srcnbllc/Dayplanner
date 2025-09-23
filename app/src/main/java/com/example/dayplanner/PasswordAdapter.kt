package com.example.dayplanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.passwords.Password

class PasswordAdapter(
    private val onClick: (Password) -> Unit,
    private val onCopy: (Password) -> Unit,
    private val onView: (Password) -> Unit
) : ListAdapter<Password, PasswordAdapter.PasswordViewHolder>(PasswordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password, parent, false)
        return PasswordViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.passwordIcon)
        private val titleView: TextView = itemView.findViewById(R.id.passwordTitle)
        private val usernameView: TextView = itemView.findViewById(R.id.passwordUsername)
        private val categoryView: TextView = itemView.findViewById(R.id.passwordCategory)
        private val copyButton: ImageButton = itemView.findViewById(R.id.copyButton)
        private val viewButton: ImageButton = itemView.findViewById(R.id.viewButton)

        fun bind(password: Password) {
            titleView.text = password.title
            usernameView.text = password.username
            categoryView.text = password.category

            // Set icon based on category
            val iconRes = when (password.category.lowercase()) {
                "sosyal medya" -> android.R.drawable.ic_menu_share
                "e-posta" -> android.R.drawable.ic_menu_send
                "banka" -> android.R.drawable.ic_menu_myplaces
                "iş" -> android.R.drawable.ic_menu_edit
                "e-ticaret" -> android.R.drawable.ic_menu_edit
                else -> android.R.drawable.ic_dialog_alert
            }
            iconView.setImageResource(iconRes)

            itemView.setOnClickListener { onClick(password) }
            copyButton.setOnClickListener { onCopy(password) }
            viewButton.setOnClickListener { onView(password) }
        }
    }

    class PasswordDiffCallback : DiffUtil.ItemCallback<Password>() {
        override fun areItemsTheSame(oldItem: Password, newItem: Password): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Password, newItem: Password): Boolean {
            return oldItem == newItem
        }
    }
}
