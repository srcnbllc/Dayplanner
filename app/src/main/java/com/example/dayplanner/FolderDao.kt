package com.example.dayplanner

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder_table ORDER BY name ASC")
    fun getAllFolders(): LiveData<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: Folder)

    @Update
    suspend fun update(folder: Folder)

    @Delete
    suspend fun delete(folder: Folder)

    @Query("SELECT * FROM folder_table WHERE id = :id")
    suspend fun getFolderById(id: Int): Folder?

    @Query("DELETE FROM folder_table WHERE id = :id")
    suspend fun deleteById(id: Int)
}