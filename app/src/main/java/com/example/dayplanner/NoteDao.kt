package com.example.dayplanner

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    // Yeni bir not eklemek için
    @Insert
    suspend fun insert(note: Note)

    // Notu ID'ye göre sorgulamak için
    @Query("SELECT * FROM note_table WHERE id = :id")
    fun getNoteById(id: Int): LiveData<Note?>  // LiveData kullanarak verinin güncel takibi sağlanır

    // Tüm notları sorgulamak için
    @Query("SELECT * FROM note_table WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>  // Tüm notlar listesi, LiveData ile döndürülür

    // Notu güncellemek için
    @Update
    suspend fun update(note: Note)

    // Çöpteki notları listelemek için
    @Query("SELECT * FROM note_table WHERE isDeleted = 1 ORDER BY id DESC")
    fun getDeletedNotes(): LiveData<List<Note>>

    // Notu soft delete yapmak için
    @Query("UPDATE note_table SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteById(id: Int)

    // Soft delete'i geri almak için
    @Query("UPDATE note_table SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreById(id: Int)
}
