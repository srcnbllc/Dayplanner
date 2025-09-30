package com.example.dayplanner.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.finance.FinanceDao

class FinanceViewModelFactory(private val financeDao: FinanceDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(financeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}








