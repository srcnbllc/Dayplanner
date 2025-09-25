package com.example.dayplanner

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao, private val folderDao: FolderDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    // getNoteById fonksiyonu, nullable (null olabilen) LiveData döndürecek
    fun getNoteById(noteId: Int): LiveData<Note?> {
        return noteDao.getNoteById(noteId)
    }

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    fun getNotesByFolder(folderId: Int?): LiveData<List<Note>> = noteDao.getNotesByFolder(folderId)

    suspend fun setPinned(id: Int, pinned: Boolean) = noteDao.setPinned(id, pinned)

    fun getNotesByStatus(status: String): LiveData<List<Note>> = noteDao.getNotesByStatus(status)

    // Silinen notlar için metodlar
    fun getDeletedNotes(): LiveData<List<Note>> = noteDao.getDeletedNotes()
    suspend fun restoreNote(id: Int) = noteDao.restoreNote(id)
    suspend fun restoreAllNotes() = noteDao.restoreAllNotes()
    suspend fun deleteNotePermanently(id: Int) = noteDao.deleteNotePermanently(id)
    suspend fun deleteAllNotesPermanently() = noteDao.deleteAllNotesPermanently()
    suspend fun deleteOldDeletedNotes(cutoffTime: Long) = noteDao.deleteOldDeletedNotes(cutoffTime)
    suspend fun softDeleteNote(id: Int, deletedAt: Long) = noteDao.softDeleteNote(id, deletedAt)

    // High-level business logic for moving notes to trash
    suspend fun moveToTrashIfDecrypted(noteId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext Result.failure(Exception("Note not found"))
            
            if (note.isEncrypted || note.encryptedBlob != null) {
                return@withContext Result.failure(Exception("ENCRYPTED"))
            }
            
            val deletedAt = System.currentTimeMillis()
            noteDao.softDeleteNote(noteId, deletedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Search functionality
    fun searchNotes(query: String): LiveData<List<Note>> = noteDao.searchNotes(query)

    // Sorting functionality
    fun getNotesByStatusSortedByDate(status: String): LiveData<List<Note>> = noteDao.getNotesByStatusSortedByDate(status)
    fun getNotesByStatusSortedByTitle(status: String): LiveData<List<Note>> = noteDao.getNotesByStatusSortedByTitle(status)
    fun getNotesByStatusSortedByLastEdited(status: String): LiveData<List<Note>> = noteDao.getNotesByStatusSortedByLastEdited(status)

    // Auto-move NEW notes to NOTES after 2 days
    suspend fun moveNewNotesToNotes(cutoffTime: Long) = noteDao.moveNewNotesToNotes(cutoffTime)

    // Export/Import functionality
    suspend fun getAllNotesSync(): List<Note> = noteDao.getAllNotesSync()

    // Folder APIs
    fun getAllFolders(): LiveData<List<Folder>> = folderDao.getAllFolders()
    suspend fun insertFolder(folder: Folder): Long = folderDao.insert(folder)
    suspend fun updateFolder(folder: Folder) = folderDao.update(folder)
    suspend fun deleteFolder(folder: Folder) = folderDao.delete(folder)
}
