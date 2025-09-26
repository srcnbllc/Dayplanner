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

    // Tüm notları sorgulamak için (silinmemiş olanlar)
    @Query("SELECT * FROM note_table WHERE status != 'DELETED' ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>  // Tüm notlar listesi, LiveData ile döndürülür

    // Notu güncellemek için
    @Update
    suspend fun update(note: Note)

    // Çöpteki notları listelemek için
    @Query("SELECT * FROM note_table WHERE status = 'DELETED' ORDER BY id DESC")
    fun getDeletedNotes(): LiveData<List<Note>>

    // Notu soft delete yapmak için
    @Query("UPDATE note_table SET status = 'DELETED', deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Int, deletedAt: Long = System.currentTimeMillis())

    // Soft delete'i geri almak için
    @Query("UPDATE note_table SET status = 'NOTES', deletedAt = NULL WHERE id = :id")
    suspend fun restoreById(id: Int)

    // Notu kalıcı olarak silmek için
    @Query("DELETE FROM note_table WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Klasöre göre notları getirmek için
    @Query("SELECT * FROM note_table WHERE folderId = :folderId AND status != 'DELETED' ORDER BY id DESC")
    fun getNotesByFolder(folderId: Int?): LiveData<List<Note>>

    // Sabitleme durumunu güncellemek için
    @Query("UPDATE note_table SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Int, pinned: Boolean)

    // Duruma göre notları getirmek için
    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY id DESC")
    fun getNotesByStatus(status: String): LiveData<List<Note>>

    // Notu geri yüklemek için
    @Query("UPDATE note_table SET status = 'NOTES', deletedAt = NULL WHERE id = :id")
    suspend fun restoreNote(id: Int)

    // Tüm notları geri yüklemek için
    @Query("UPDATE note_table SET status = 'NOTES', deletedAt = NULL WHERE status = 'DELETED'")
    suspend fun restoreAllNotes()

    // Notu kalıcı olarak silmek için
    @Query("DELETE FROM note_table WHERE id = :id")
    suspend fun deleteNotePermanently(id: Int)

    // Tüm notları kalıcı olarak silmek için
    @Query("DELETE FROM note_table WHERE status = 'DELETED'")
    suspend fun deleteAllNotesPermanently()

    // Eski silinmiş notları temizlemek için
    @Query("DELETE FROM note_table WHERE status = 'DELETED' AND deletedAt < :cutoffTime")
    suspend fun deleteOldDeletedNotes(cutoffTime: Long)

    // Soft delete yapmak için
    @Query("UPDATE note_table SET status = 'DELETED', deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteNote(id: Int, deletedAt: Long)

    // Notu ID'ye göre senkron getirmek için
    @Query("SELECT * FROM note_table WHERE id = :id")
    suspend fun getNoteByIdSync(id: Int): Note?

    // Arama yapmak için
    @Query("SELECT * FROM note_table WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND status != 'DELETED' ORDER BY id DESC")
    fun searchNotes(query: String): LiveData<List<Note>>

    // Duruma göre tarihe göre sıralı getirmek için
    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY createdAt DESC")
    fun getNotesByStatusSortedByDate(status: String): LiveData<List<Note>>

    // Duruma göre başlığa göre sıralı getirmek için
    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY title ASC")
    fun getNotesByStatusSortedByTitle(status: String): LiveData<List<Note>>

    // Duruma göre son düzenlemeye göre sıralı getirmek için
    @Query("SELECT * FROM note_table WHERE status = :status ORDER BY createdAt DESC")
    fun getNotesByStatusSortedByLastEdited(status: String): LiveData<List<Note>>

    // Yeni notları NOTES'e taşımak için
    @Query("UPDATE note_table SET status = 'NOTES' WHERE status = 'NEW' AND createdAt < :cutoffTime")
    suspend fun moveNewNotesToNotes(cutoffTime: Long)

    // Tüm notları senkron getirmek için
    @Query("SELECT * FROM note_table WHERE status != 'DELETED' ORDER BY id DESC")
    suspend fun getAllNotesSync(): List<Note>
}
