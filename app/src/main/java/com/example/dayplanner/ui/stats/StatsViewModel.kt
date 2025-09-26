package com.example.dayplanner.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.dayplanner.Note
import com.example.dayplanner.NoteDatabase
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.passwords.PasswordDao
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val db = NoteDatabase.getDatabase(app)
    val allNotesLiveData: LiveData<List<Note>> = db.noteDao().getAllNotes()

    private val _completedCount = MutableLiveData(0)
    val completedCount: LiveData<Int> = _completedCount

    private val _streakDays = MutableLiveData(0)
    val streakDays: LiveData<Int> = _streakDays

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData<Double>()
    val totalExpense: LiveData<Double> = _totalExpense

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _passwordsCount = MutableLiveData<Int>()
    val passwordsCount: LiveData<Int> = _passwordsCount

    private val _weakPasswordsCount = MutableLiveData<Int>()
    val weakPasswordsCount: LiveData<Int> = _weakPasswordsCount

    private val notesObserver = Observer<List<Note>> { notes ->
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val todayNotes = notes.filter { it.date == todayStr }
        _completedCount.postValue(todayNotes.size)
        _streakDays.postValue(computeStreak(notes))
    }

    init {
        allNotesLiveData.observeForever(notesObserver)
        loadAllStats()
    }

    private fun computeStreak(notes: List<Note>): Int {
        // Basit örnek: ardışık gün sayısı
        return notes.size.coerceAtMost(7) // placeholder
    }

    fun loadAllStats() {
        viewModelScope.launch {
            try {
                // val financeDao = db.financeDao() // FinanceDao not implemented yet
                // val passwordDao = db.passwordDao() // PasswordDao not implemented yet

                // Load finance statistics - Placeholder values
                // val transactions = financeDao.getAllTransactions()
                // val income = transactions.filter { it.type.name == "INCOME" }.sumOf { it.amount }
                // val expense = transactions.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }
                val income = 0.0
                val expense = 0.0
                val balance = income - expense

                _totalIncome.postValue(income)
                _totalExpense.postValue(expense)
                _balance.postValue(balance)

                // Load password statistics - Placeholder values
                // val passwords = passwordDao.getAllPasswords()
                // _passwordsCount.postValue(passwords.size)
                // _weakPasswordsCount.postValue(passwords.count { isWeakPassword(it.password) })
                _passwordsCount.postValue(0)
                _weakPasswordsCount.postValue(0)

            } catch (e: Exception) {
                // Handle error - set default values
                setDefaultValues()
            }
        }
    }

    private fun isWeakPassword(password: String): Boolean {
        return password.length < 8 || 
               !password.any { it.isDigit() } ||
               !password.any { it.isUpperCase() } ||
               !password.any { it.isLowerCase() }
    }

    private fun setDefaultValues() {
        _totalIncome.postValue(0.0)
        _totalExpense.postValue(0.0)
        _balance.postValue(0.0)
        _passwordsCount.postValue(0)
        _weakPasswordsCount.postValue(0)
    }

    override fun onCleared() {
        super.onCleared()
        allNotesLiveData.removeObserver(notesObserver)
    }

    // İsteğe bağlı mock veri
    fun setMockData(completed: Int, streak: Int) {
        _completedCount.postValue(completed)
        _streakDays.postValue(streak)
    }
}
