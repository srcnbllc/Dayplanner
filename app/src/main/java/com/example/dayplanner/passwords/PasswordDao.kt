package com.example.dayplanner.passwords

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY title ASC")
    fun getAllPasswords(): Flow<List<Password>>

    @Query("SELECT * FROM password_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<PasswordCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: Password)

    @Update
    suspend fun updatePassword(password: Password)

    @Delete
    suspend fun deletePassword(password: Password)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: PasswordCategory)

    @Query("SELECT * FROM passwords WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR website LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchPasswords(query: String): Flow<List<Password>>

    @Query("SELECT * FROM passwords WHERE category = :category ORDER BY title ASC")
    fun getPasswordsByCategory(category: String): Flow<List<Password>>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Int): Password?

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Int)

    @Query("SELECT * FROM passwords ORDER BY title ASC")
    suspend fun getAllPasswordsSync(): List<Password>
}