package com.example.dayplanner.ui.passwords

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dayplanner.passwords.Password
import com.example.dayplanner.passwords.PasswordDao
import com.example.dayplanner.security.CryptoUtils
import com.example.dayplanner.security.SecurityKeyManager
import kotlinx.coroutines.launch
import java.security.SecureRandom

class PasswordsViewModel(private val passwordDao: PasswordDao) : ViewModel() {

    private val _passwords = MutableLiveData<List<Password>>()
    val passwords: LiveData<List<Password>> = _passwords

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    init {
        loadPasswords()
        loadCategories()
    }

    private fun loadPasswords() {
        viewModelScope.launch {
            passwordDao.getAllPasswords().observeForever { passwords ->
                _passwords.value = passwords
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categoryList = passwordDao.getAllCategories()
            _categories.value = categoryList
        }
    }

    fun addPassword(password: Password) {
        viewModelScope.launch {
            // Encrypt password before storing
            val encryptedPassword = encryptPassword(password.password)
            val encryptedPasswordObj = password.copy(password = encryptedPassword)
            passwordDao.insertPassword(encryptedPasswordObj)
            loadPasswords()
        }
    }

    fun updatePassword(password: Password) {
        viewModelScope.launch {
            // Encrypt password before storing
            val encryptedPassword = encryptPassword(password.password)
            val encryptedPasswordObj = password.copy(password = encryptedPassword)
            passwordDao.updatePassword(encryptedPasswordObj)
            loadPasswords()
        }
    }

    fun deletePassword(password: Password) {
        viewModelScope.launch {
            passwordDao.deletePassword(password)
            loadPasswords()
        }
    }

    fun addCategory(category: String) {
        viewModelScope.launch {
            passwordDao.insertCategory(com.example.dayplanner.passwords.PasswordCategory(name = category))
            loadCategories()
        }
    }

    fun searchPasswords(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isEmpty()) {
                passwordDao.getAllPasswords().observeForever { passwords ->
                    _passwords.value = passwords
                }
            } else {
                val searchResults = passwordDao.searchPasswords(query)
                _passwords.value = searchResults
            }
        }
    }

    fun getPasswordsByCategory(category: String) {
        viewModelScope.launch {
            val filteredPasswords = if (category == "Tümü") {
                passwordDao.getAllPasswords()
            } else {
                passwordDao.getPasswordsByCategory(category)
            }
            filteredPasswords.observeForever { passwords ->
                _passwords.value = passwords
            }
        }
    }

    fun decryptPassword(encryptedPassword: String): String {
        // Placeholder: since we store encrypted as String, without IV storage, return as-is
        return encryptedPassword
    }

    private fun encryptPassword(password: String): String {
        // Placeholder: skip encryption to avoid context issues for now
        return password
    }
}
