package com.example.dayplanner.ui.finance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.finance.Transaction
import com.example.dayplanner.finance.TransactionType
import com.example.dayplanner.finance.Category
import com.example.dayplanner.finance.CategoryTotal
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

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _incomeCategories = MutableLiveData<List<Category>>()
    val incomeCategories: LiveData<List<Category>> = _incomeCategories

    private val _expenseCategories = MutableLiveData<List<Category>>()
    val expenseCategories: LiveData<List<Category>> = _expenseCategories

    private val _categoryTotals = MutableLiveData<List<CategoryTotal>>()
    val categoryTotals: LiveData<List<CategoryTotal>> = _categoryTotals

    // Filter states
    private val _currentFilter = MutableLiveData<String>("ALL") // ALL, INCOME, EXPENSE, category name
    val currentFilter: LiveData<String> = _currentFilter

    private val _currentDateRange = MutableLiveData<String>("MONTHLY") // DAILY, WEEKLY, MONTHLY, YEARLY, ALL
    val currentDateRange: LiveData<String> = _currentDateRange

    init {
        loadTransactions()
        loadCategories()
        loadSummaryData()
    }

    fun setFilter(filter: String) {
        _currentFilter.value = filter
        loadTransactions()
        loadSummaryData()
    }

    fun setDateRange(range: String) {
        _currentDateRange.value = range
        loadTransactions()
        loadSummaryData()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                val filter = _currentFilter.value ?: "ALL"
                val range = _currentDateRange.value ?: "MONTHLY"
                
                val dateRange = getDateRange(range)
                val transactions = when (filter) {
                    "ALL" -> financeDao.getTransactionsByDateRange(dateRange.first, dateRange.second)
                    "INCOME" -> financeDao.getTransactionsByType(TransactionType.INCOME)
                    "EXPENSE" -> financeDao.getTransactionsByType(TransactionType.EXPENSE)
                    else -> financeDao.getTransactionsByCategory(filter)
                }
                
                transactions.observeForever { transactionList ->
                    _transactions.value = transactionList
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadSummaryData() {
        viewModelScope.launch {
            try {
                val range = _currentDateRange.value ?: "MONTHLY"
                val dateRange = getDateRange(range)
                
                val income = financeDao.getTotalIncome(dateRange.first, dateRange.second) ?: 0.0
                val expense = financeDao.getTotalExpense(dateRange.first, dateRange.second) ?: 0.0
                
                _totalIncome.value = income
                _totalExpense.value = expense
                _balance.value = income - expense
                
                // Load category totals
                val incomeTotals = financeDao.getCategoryTotals(TransactionType.INCOME, dateRange.first, dateRange.second)
                val expenseTotals = financeDao.getCategoryTotals(TransactionType.EXPENSE, dateRange.first, dateRange.second)
                _categoryTotals.value = incomeTotals + expenseTotals
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                financeDao.getAllCategories().observeForever { categoryList ->
                    _categories.value = categoryList
                    _incomeCategories.value = categoryList.filter { it.type == TransactionType.INCOME }
                    _expenseCategories.value = categoryList.filter { it.type == TransactionType.EXPENSE }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun getDateRange(range: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        val startTime = when (range) {
            "DAILY" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            "WEEKLY" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.timeInMillis
            }
            "MONTHLY" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            "YEARLY" -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            else -> 0L // ALL
        }
        
        return Pair(startTime, endTime)
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                financeDao.insertTransaction(transaction)
                loadTransactions()
                loadSummaryData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                financeDao.updateTransaction(transaction)
                loadTransactions()
                loadSummaryData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                financeDao.deleteTransaction(transaction)
                loadTransactions()
                loadSummaryData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}