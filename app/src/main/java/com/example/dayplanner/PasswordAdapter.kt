package com.example.dayplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        val password = getItem(position)
        holder.bind(password)
    }

    inner class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.titleTextView)
        private val usernameView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val categoryView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val websiteView: TextView = itemView.findViewById(R.id.websiteTextView)
        private val iconView: ImageView = itemView.findViewById(R.id.passwordIcon)

        fun bind(password: Password) {
            titleView.text = password.title
            usernameView.text = password.username
            categoryView.text = password.category
            websiteView.text = password.website ?: ""
            websiteView.visibility = if (password.website.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Set category icon
            val iconRes = when (password.category.lowercase()) {
                "sosyal medya" -> R.drawable.ic_share
                "e-posta" -> R.drawable.ic_mail
                "banka" -> R.drawable.ic_money
                "iş" -> R.drawable.ic_work_note
                "e-ticaret" -> R.drawable.ic_shopping_cart
                else -> R.drawable.ic_security
            }
            iconView.setImageResource(iconRes)

            itemView.setOnClickListener { onClick(password) }
            itemView.setOnLongClickListener {
                onCopy(password)
                true
            }
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