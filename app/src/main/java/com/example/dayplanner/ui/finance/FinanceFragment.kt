package com.example.dayplanner.ui.finance

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.TransactionAdapter
import com.example.dayplanner.databinding.FragmentFinanceBinding
import com.example.dayplanner.finance.FinanceRepository
import com.example.dayplanner.finance.CurrencyConverter
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.TransactionType
import com.example.dayplanner.NoteDatabase
import java.text.NumberFormat
import java.util.*
import com.example.dayplanner.utils.CustomToast
import com.example.dayplanner.R

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var financeRepository: FinanceRepository
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

        // Initialize database and repository
        val database = NoteDatabase.getDatabase(requireContext())
        val currencyConverter = CurrencyConverter()
        financeRepository = FinanceRepository(database.financeDao(), currencyConverter)
        
        // Initialize ViewModel with factory
        val factory = FinanceViewModelFactory(financeRepository)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[FinanceViewModel::class.java]

        setupRecyclerView()
        setupFab()
        setupAnalyticsButton()
        setupCurrencyConverterButton()
        setupFilterSpinner()
        setupDateRangeSpinner()
        observeData()
        
        // Ensure all buttons are visible
        ensureButtonsVisible()
    }

    private fun ensureButtonsVisible() {
        // Force visibility of all buttons and layouts
        binding.addTransactionFab.visibility = View.VISIBLE
        binding.analyticsButton.visibility = View.VISIBLE
        binding.currencyConverterButton.visibility = View.VISIBLE
        
        // Debug log removed
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

    private fun setupFab() {
        // Setup Floating Action Button
        binding.addTransactionFab.setOnClickListener {
            // Open add transaction dialog
            showAddTransactionDialog()
        }
    }

    private fun setupAnalyticsButton() {
        binding.analyticsButton.visibility = View.VISIBLE
        binding.analyticsButton.setOnClickListener {
            // Navigate to analytics fragment
            navigateToAnalytics()
        }
    }

    private fun setupCurrencyConverterButton() {
        binding.currencyConverterButton.visibility = View.VISIBLE
        binding.currencyConverterButton.setOnClickListener {
            // Navigate to currency converter fragment
            navigateToCurrencyConverter()
        }
    }

    private fun setupFilterSpinner() {
        // Setup filter chips
        binding.allChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setTypeFilter("ALL")
            }
        }
        
        binding.incomeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setTypeFilter("INCOME")
            }
        }
        
        binding.expenseChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setTypeFilter("EXPENSE")
            }
        }
    }

    private fun setupDateRangeSpinner() {
        // Setup date range chips
        binding.dailyChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setFilter("DAILY")
            }
        }
        
        binding.weeklyChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setFilter("WEEKLY")
            }
        }
        
        binding.monthlyChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setFilter("MONTHLY")
            }
        }
        
        binding.yearlyChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setFilter("YEARLY")
            }
        }
        
        // Set default to monthly
        binding.monthlyChip.isChecked = true
    }

    // Performance optimization: Cache formatters
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    
    private fun observeData() {
        // Observe transactions with performance optimization
        lifecycleScope.launch {
            viewModel.optimizedTransactions.collect { transactions ->
                transactionAdapter.submitList(transactions)
            }
        }

        // Observe summary data with cached formatter
        lifecycleScope.launch {
            viewModel.totals.collect { totals ->
                binding.totalIncomeText.text = currencyFormatter.format(totals.income)
                binding.totalExpenseText.text = currencyFormatter.format(totals.expense)
                binding.balanceText.text = currencyFormatter.format(totals.balance)
                
                // Use Kite Design colors
                val color = if (totals.balance >= 0) {
                    requireContext().getColor(R.color.kite_green)
                } else {
                    requireContext().getColor(R.color.kite_red)
                }
                binding.balanceText.setTextColor(color)
            }
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
                viewModel.deleteTransaction(transaction.id)
                // Transaction deleted successfully
            }
            .setNegativeButton("İptal") { _, _ ->
                transactionAdapter.notifyDataSetChanged() // Refresh to undo swipe
            }
            .show()
    }

    private fun showAddTransactionDialog() {
        val dialog = AddTransactionDialogAdvanced.newInstance { transaction ->
            viewModel.addTransaction(transaction)
            // Transaction added successfully
        }
        dialog.show(parentFragmentManager, "AddTransactionDialogAdvanced")
    }

    private fun navigateToAnalytics() {
        // Navigate to analytics fragment using Navigation Component
        try {
            findNavController().navigate(R.id.analyticsFragment)
        } catch (e: Exception) {
            // Navigation failed silently
        }
    }

    private fun navigateToCurrencyConverter() {
        // Navigate to currency converter fragment using Navigation Component
        try {
            findNavController().navigate(R.id.currencyConverterFragment)
        } catch (e: Exception) {
            // Navigation failed silently
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}