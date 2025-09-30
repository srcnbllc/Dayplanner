package com.example.dayplanner.passwords

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PasswordDao {
    // Original methods for PasswordItem
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PasswordItem): Long

    @Update
    suspend fun updateItem(item: PasswordItem)

    @Delete
    suspend fun deleteItem(item: PasswordItem)

    @Query("SELECT * FROM password_item ORDER BY service ASC")
    fun getAllItems(): LiveData<List<PasswordItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PasswordHistory): Long

    @Query("SELECT * FROM password_history WHERE passwordItemId = :itemId ORDER BY changedAt DESC LIMIT 3")
    fun getLastThree(itemId: Int): LiveData<List<PasswordHistory>>

    // New methods for Password
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: Password): Long

    @Update
    suspend fun updatePassword(password: Password)

    @Delete
    suspend fun deletePassword(password: Password)

    @Query("SELECT * FROM passwords ORDER BY title ASC")
    fun getAllPasswords(): LiveData<List<Password>>

    @Query("SELECT * FROM passwords WHERE category = :category ORDER BY title ASC")
    fun getPasswordsByCategory(category: String): LiveData<List<Password>>

    @Query("SELECT * FROM passwords WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY title ASC")
    suspend fun searchPasswords(query: String): List<Password>

    @Query("SELECT * FROM passwords WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchPasswordsLive(query: String): LiveData<List<Password>>

    // Category methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: PasswordCategory)

    @Query("SELECT name FROM password_categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<String>

    // Synchronous methods for export/import
    @Query("SELECT * FROM passwords ORDER BY title ASC")
    suspend fun getAllPasswordsSync(): List<Password>
}


