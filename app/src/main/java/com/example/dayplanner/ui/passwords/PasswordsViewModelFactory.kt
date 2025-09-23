package com.example.dayplanner.ui.passwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.passwords.PasswordDao

class PasswordsViewModelFactory(private val passwordDao: PasswordDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordsViewModel(passwordDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




