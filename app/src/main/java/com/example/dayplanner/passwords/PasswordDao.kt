package com.example.dayplanner.passwords

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PasswordDao {
    @Query("SELECT * FROM password_item ORDER BY id DESC")
    fun getAllItems(): LiveData<List<PasswordItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PasswordItem)

    @Update
    suspend fun updateItem(item: PasswordItem)

    @Delete
    suspend fun deleteItem(item: PasswordItem)

    @Query("SELECT * FROM password_history WHERE itemId = :itemId ORDER BY changedAt DESC LIMIT 3")
    fun getLastThree(itemId: Int): LiveData<List<PasswordHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PasswordHistory)
}

