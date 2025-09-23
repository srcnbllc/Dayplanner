package com.example.dayplanner.ui.finance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.TransactionType
import com.example.dayplanner.finance.Category
import kotlinx.coroutines.launch
import java.util.*

class FinanceViewModel(private val financeDao: FinanceDao) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData<Double>()
    val totalExpense: LiveData<Double> = _totalExpense

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val allTransactions = financeDao.getAllTransactions()
            _transactions.value = allTransactions
            
            // Calculate totals
            val income = allTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = allTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            
            _totalIncome.value = income
            _totalExpense.value = expense
            _balance.value = income - expense
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categoryList = financeDao.getAllCategories()
            _categories.value = categoryList
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            financeDao.insertTransaction(transaction)
            loadTransactions()
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            financeDao.updateTransaction(transaction)
            loadTransactions()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            financeDao.deleteTransaction(transaction)
            loadTransactions()
        }
    }

    fun addCategory(category: String) {
        viewModelScope.launch {
            financeDao.insertCategory(Category(name = category))
            loadCategories()
        }
    }

    fun getTransactionsByCategory(category: String) {
        viewModelScope.launch {
            val filteredTransactions = if (category == "Tümü") {
                financeDao.getAllTransactions()
            } else {
                financeDao.getTransactionsByCategory(category)
            }
            _transactions.value = filteredTransactions
        }
    }

    fun getTransactionsByType(type: TransactionType) {
        viewModelScope.launch {
            val filteredTransactions = financeDao.getTransactionsByType(type)
            _transactions.value = filteredTransactions
        }
    }
}
