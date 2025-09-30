package com.example.dayplanner.ui.finance

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.TransactionAdapter
import com.example.dayplanner.databinding.FragmentFinanceBinding
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.TransactionType
import com.example.dayplanner.NoteDatabase
import java.text.NumberFormat
import java.util.*
import com.example.dayplanner.utils.CustomToast

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var financeDao: FinanceDao
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var viewModel: FinanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        val database = NoteDatabase.getDatabase(requireContext())
        financeDao = database.financeDao()
        
        // Initialize ViewModel after DAO is ready
        viewModel = FinanceViewModel(financeDao)

        setupRecyclerView()
        setupAddButton()
        setupFilterSpinner()
        setupDateRangeSpinner()
        observeData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onTransactionClick = { transaction ->
                // Handle transaction click - show details or edit
                showTransactionDetails(transaction)
            },
            onTransactionDelete = { transaction ->
                // Handle transaction delete
                showDeleteConfirmation(transaction)
            }
        )

        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecyclerView.adapter = transactionAdapter

        // Swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val transaction = transactionAdapter.currentList[position]
                showDeleteConfirmation(transaction)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.transactionsRecyclerView)
    }

    private fun setupAddButton() {
        binding.addTransactionButton.setOnClickListener {
            // Open add transaction dialog
            showAddTransactionDialog()
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = listOf("Tümü", "Gelir", "Gider")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filterOptions)
        binding.categorySpinner.adapter = adapter
        
        binding.categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filter = when (position) {
                    0 -> "ALL"
                    1 -> "INCOME"
                    2 -> "EXPENSE"
                    else -> "ALL"
                }
                viewModel.setFilter(filter)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupDateRangeSpinner() {
        // Date range functionality will be added later
        // For now, just set default to monthly
        viewModel.setDateRange("MONTHLY")
    }

    private fun observeData() {
        // Observe transactions
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
        }

        // Observe summary data
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
            binding.totalIncomeText.text = formatter.format(income)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
            binding.totalExpenseText.text = formatter.format(expense)
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
            binding.balanceText.text = formatter.format(balance)
            
            // Change color based on balance
            val color = if (balance >= 0) {
                android.graphics.Color.parseColor("#4CAF50") // Green
                } else {
                android.graphics.Color.parseColor("#F44336") // Red
            }
            binding.balanceText.setTextColor(color)
        }
    }

    private fun showTransactionDetails(transaction: Transaction) {
        // Show transaction details dialog
        val message = """
            Başlık: ${transaction.title}
            Tutar: ${NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(transaction.amount)}
            Kategori: ${transaction.category}
            Tarih: ${Date(transaction.date).toString()}
            Açıklama: ${transaction.description}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("İşlem Detayları")
            .setMessage(message)
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun showDeleteConfirmation(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("İşlemi Sil")
            .setMessage("Bu işlemi silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _, _ ->
                viewModel.deleteTransaction(transaction)
                CustomToast.show(requireContext(), "İşlem silindi")
            }
            .setNegativeButton("İptal") { _, _ ->
                transactionAdapter.notifyDataSetChanged() // Refresh to undo swipe
            }
            .show()
    }

    private fun showAddTransactionDialog() {
        val dialog = AddTransactionDialog.newInstance { transaction ->
            viewModel.addTransaction(transaction)
            CustomToast.show(requireContext(), "İşlem eklendi")
        }
        dialog.show(parentFragmentManager, "AddTransactionDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}