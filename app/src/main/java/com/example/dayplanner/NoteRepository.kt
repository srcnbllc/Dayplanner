package com.example.dayplanner

import androidx.lifecycle.LiveData

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

    // Folder APIs
    fun getAllFolders(): LiveData<List<Folder>> = folderDao.getAllFolders()
    suspend fun insertFolder(folder: Folder): Long = folderDao.insert(folder)
    suspend fun updateFolder(folder: Folder) = folderDao.update(folder)
    suspend fun deleteFolder(folder: Folder) = folderDao.delete(folder)
}
