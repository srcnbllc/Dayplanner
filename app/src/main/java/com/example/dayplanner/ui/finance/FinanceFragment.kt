package com.example.dayplanner.ui.finance

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.R
import com.example.dayplanner.TransactionAdapter
import com.example.dayplanner.databinding.FragmentFinanceBinding
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.TransactionType
import com.example.dayplanner.NoteDatabase
import java.text.NumberFormat
import java.util.*
import android.util.Log
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
        setupCategorySpinner()
        observeData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onClick = { transaction ->
                showEditTransactionDialog(transaction)
            },
            onLongClick = { transaction ->
                showDeleteConfirmation(transaction)
            }
        )

        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecyclerView.adapter = transactionAdapter

        // Swipe to delete
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.bindingAdapterPosition
                val transaction = transactionAdapter.currentList.getOrNull(position) ?: return
                viewModel.deleteTransaction(transaction)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.transactionsRecyclerView)
    }

    private fun setupAddButton() {
        binding.addTransactionButton.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf("Tümü")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryList = mutableListOf("Tümü")
            categoryList.addAll(categories)
            adapter.clear()
            adapter.addAll(categoryList)
            adapter.notifyDataSetChanged()
        }

        binding.categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position) as? String ?: "Tümü"
                viewModel.getTransactionsByCategory(selectedCategory)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeData() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
        }

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
        }
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_transaction, null)

        val typeSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.transactionTypeSpinner)
        val titleEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.titleEditText)
        val amountEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amountEditText)
        val categorySpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.categorySpinner)
        val descriptionEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionEditText)

        // Setup type spinner
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, 
            listOf("Gelir", "Gider"))
        typeSpinner.setAdapter(typeAdapter)

        // Setup category spinner
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, 
            listOf("Yemek", "Ulaşım", "Alışveriş", "Fatura", "Maaş", "Diğer"))
        categorySpinner.setAdapter(categoryAdapter)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Ekle") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val amountText = amountEditText.text.toString().trim()
                val category = categorySpinner.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val type = if (typeSpinner.text.toString() == "Gelir") TransactionType.INCOME else TransactionType.EXPENSE

                if (title.isNotEmpty() && amountText.isNotEmpty() && category.isNotEmpty()) {
                    try {
                        val amount = amountText.toDouble()
                        val transaction = Transaction(
                            id = 0,
                            title = title,
                            amount = amount,
                            category = category,
                            description = description,
                            type = type,
                            date = System.currentTimeMillis()
                        )
                        viewModel.addTransaction(transaction)
                    } catch (e: NumberFormatException) {
                        CustomToast.show(requireContext(), "Geçerli bir tutar girin")
                    }
                } else {
                    CustomToast.show(requireContext(), "Tüm alanları doldurun")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_transaction, null)

        val typeSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.transactionTypeSpinner)
        val titleEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.titleEditText)
        val amountEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amountEditText)
        val categorySpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.categorySpinner)
        val descriptionEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionEditText)

        // Pre-fill fields
        typeSpinner.setText(if (transaction.type == TransactionType.INCOME) "Gelir" else "Gider")
        titleEditText.setText(transaction.title)
        amountEditText.setText(transaction.amount.toString())
        categorySpinner.setText(transaction.category)
        descriptionEditText.setText(transaction.description)

        // Setup adapters
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, 
            listOf("Gelir", "Gider"))
        typeSpinner.setAdapter(typeAdapter)

        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, 
            listOf("Yemek", "Ulaşım", "Alışveriş", "Fatura", "Maaş", "Diğer"))
        categorySpinner.setAdapter(categoryAdapter)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Güncelle") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val amountText = amountEditText.text.toString().trim()
                val category = categorySpinner.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val type = if (typeSpinner.text.toString() == "Gelir") TransactionType.INCOME else TransactionType.EXPENSE

                if (title.isNotEmpty() && amountText.isNotEmpty() && category.isNotEmpty()) {
                    try {
                        val amount = amountText.toDouble()
                        val updatedTransaction = transaction.copy(
                            title = title,
                            amount = amount,
                            category = category,
                            description = description,
                            type = type
                        )
                        viewModel.updateTransaction(updatedTransaction)
                    } catch (e: NumberFormatException) {
                        CustomToast.show(requireContext(), "Geçerli bir tutar girin")
                    }
                } else {
                    CustomToast.show(requireContext(), "Tüm alanları doldurun")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showDeleteConfirmation(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("İşlemi Sil")
            .setMessage("Bu işlemi silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


