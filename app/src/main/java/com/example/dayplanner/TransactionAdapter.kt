package com.example.dayplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.R
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit,
    private val onTransactionDelete: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.transactionIcon)
        private val titleView: TextView = itemView.findViewById(R.id.transactionTitle)
        private val categoryView: TextView = itemView.findViewById(R.id.transactionCategory)
        private val dateView: TextView = itemView.findViewById(R.id.transactionDate)
        private val amountView: TextView = itemView.findViewById(R.id.transactionAmount)

        fun bind(transaction: Transaction) {
            titleView.text = transaction.title
            categoryView.text = transaction.category
            dateView.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(transaction.date))
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
            val amount = if (transaction.type == com.example.dayplanner.finance.TransactionType.INCOME) transaction.amount else -transaction.amount
            amountView.text = formatter.format(amount.toDouble())
            
            // Set color based on transaction type
            val color = if (transaction.type == com.example.dayplanner.finance.TransactionType.INCOME) 
                android.R.color.holo_green_dark else android.R.color.holo_red_dark
            amountView.setTextColor(itemView.context.getColor(color))
            
            // Set icon based on category
            val iconRes = when (transaction.category.lowercase()) {
                "yemek", "restoran" -> android.R.drawable.ic_menu_myplaces
                "ulaşım", "benzin" -> android.R.drawable.ic_menu_directions
                "alışveriş" -> android.R.drawable.ic_menu_manage
                "fatura" -> android.R.drawable.ic_menu_edit
                "maaş" -> android.R.drawable.ic_menu_myplaces
                else -> android.R.drawable.ic_menu_edit
            }
            iconView.setImageResource(iconRes)

            itemView.setOnClickListener { onTransactionClick(transaction) }
            itemView.setOnLongClickListener { 
                onTransactionDelete(transaction)
                true
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
