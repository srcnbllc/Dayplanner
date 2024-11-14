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
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>  // Tüm notlar listesi, LiveData ile döndürülür

    // Notu güncellemek için
    @Update
    suspend fun update(note: Note)
}
