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
    suspend fun insert(note: Note): Long

    // Notu ID'ye göre sorgulamak için
    @Query("SELECT * FROM note_table WHERE id = :id")
    fun getNoteById(id: Int): LiveData<Note?>  // LiveData kullanarak verinin güncel takibi sağlanır

    // Tüm notları sorgulamak için (silinenler hariç)
    @Query("SELECT * FROM note_table WHERE status != 'DELETED' ORDER BY id DESC")
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

    // Silinen notlar için metodlar
    @Query("SELECT * FROM note_table WHERE status = 'DELETED' ORDER BY deletedAt DESC")
    fun getDeletedNotes(): LiveData<List<Note>>

    @Query("UPDATE note_table SET status = 'NOTES', deletedAt = NULL WHERE id = :id")
    suspend fun restoreNote(id: Int)

    @Query("UPDATE note_table SET status = 'NOTES', deletedAt = NULL WHERE status = 'DELETED'")
    suspend fun restoreAllNotes()

    @Query("DELETE FROM note_table WHERE id = :id")
    suspend fun deleteNotePermanently(id: Int)

    @Query("DELETE FROM note_table WHERE status = 'DELETED'")
    suspend fun deleteAllNotesPermanently()

    // Soft delete functionality
    @Query("UPDATE note_table SET status = 'DELETED', deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteNote(id: Int, deletedAt: Long)

    // Auto-delete old deleted notes (30 days)
    @Query("DELETE FROM note_table WHERE status = 'DELETED' AND deletedAt < :cutoffTime")
    suspend fun deleteOldDeletedNotes(cutoffTime: Long)

    // Search functionality
    @Query("SELECT * FROM note_table WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND status != 'DELETED' ORDER BY isPinned DESC, createdAt DESC")
    fun searchNotes(query: String): LiveData<List<Note>>

    // Sorting queries
    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesByStatusSortedByDate(status: String): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY isPinned DESC, title ASC")
    fun getNotesByStatusSortedByTitle(status: String): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY isPinned DESC, id DESC")
    fun getNotesByStatusSortedByLastEdited(status: String): LiveData<List<Note>>

    // Recently added notes (NEW status) - move to NOTES after 2 days
    @Query("SELECT * FROM note_table WHERE status = 'NEW' AND createdAt < :cutoffTime")
    suspend fun getOldNewNotes(cutoffTime: Long): List<Note>

    @Query("UPDATE note_table SET status = 'NOTES' WHERE status = 'NEW' AND createdAt < :cutoffTime")
    suspend fun moveNewNotesToNotes(cutoffTime: Long)

    // Sync method for AddNoteActivity
    @Query("SELECT * FROM note_table WHERE id = :id")
    suspend fun getNoteByIdSync(id: Int): Note?

    // Synchronous methods for export/import
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    suspend fun getAllNotesSync(): List<Note>
}
