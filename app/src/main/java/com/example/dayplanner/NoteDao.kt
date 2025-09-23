package com.example.dayplanner

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface NoteDao {

    // Yeni bir not eklemek için
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    // Notu ID'ye göre sorgulamak için
    @Query("SELECT * FROM note_table WHERE id = :id")
    fun getNoteById(id: Int): LiveData<Note?>  // LiveData kullanarak verinin güncel takibi sağlanır

    // Tüm notları sorgulamak için
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>  // Tüm notlar listesi, LiveData ile döndürülür

    // Notu güncellemek için
    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("UPDATE note_table SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Int, pinned: Boolean)

    @Query("SELECT * FROM note_table WHERE folderId = :folderId ORDER BY isPinned DESC, id DESC")
    fun getNotesByFolder(folderId: Int?): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY id DESC")
    fun getNotesByStatus(status: String): LiveData<List<Note>>

    @Query("UPDATE note_table SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Int, status: String)

    // Synchronous methods for export/import
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    suspend fun getAllNotesSync(): List<Note>
}
