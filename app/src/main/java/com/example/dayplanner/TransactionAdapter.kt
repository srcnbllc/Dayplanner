package com.example.dayplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.TransactionType
import com.example.dayplanner.R
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
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val typeIndicator: View = itemView.findViewById(R.id.typeIndicator)
        
        // Performance optimization: Cache formatters
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(transaction: Transaction) {
            titleTextView.text = transaction.title
            categoryTextView.text = transaction.category
            
            // Use cached formatters for better performance
            amountTextView.text = currencyFormatter.format(transaction.amount)
            dateTextView.text = dateFormatter.format(Date(transaction.date))
            
            // Use Kite Design colors
            val color = if (transaction.type == TransactionType.INCOME) {
                itemView.context.getColor(R.color.kite_green)
            } else {
                itemView.context.getColor(R.color.kite_red)
            }
            typeIndicator.setBackgroundColor(color)
            
            itemView.setOnClickListener {
                onTransactionClick(transaction)
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