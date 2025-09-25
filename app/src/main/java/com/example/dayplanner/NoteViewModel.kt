package com.example.dayplanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    val allFolders: LiveData<List<Folder>>
    private var currentStatus: String? = null

    init {
        val db = NoteDatabase.getDatabase(application)
        val noteDao = db.noteDao()
        val folderDao = db.folderDao()
        repository = NoteRepository(noteDao, folderDao)
        allNotes = repository.allNotes
        allFolders = repository.getAllFolders()
    }

    // getNoteById fonksiyonu nullable dönebilir
    fun getNoteById(noteId: Int): LiveData<Note?> {
        return repository.getNoteById(noteId)
    }

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(note)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(note)
        }
    }

    fun setPinned(id: Int, pinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setPinned(id, pinned)
        }
    }

    fun getNotesByFolder(folderId: Int?): LiveData<List<Note>> = repository.getNotesByFolder(folderId)

    fun insertFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFolder(Folder(name = name))
        }
    }

    fun notesByStatus(status: String): LiveData<List<Note>> {
        currentStatus = status
        return repository.getNotesByStatus(status)
    }

    // Silinen notlar için metodlar
    fun getDeletedNotes(): LiveData<List<Note>> {
        return repository.getDeletedNotes()
    }

    suspend fun restoreNote(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreNote(id)
        }
    }

    suspend fun deleteNotePermanently(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotePermanently(id)
        }
    }

    suspend fun softDeleteNote(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.softDeleteNote(id, System.currentTimeMillis())
        }
    }

    suspend fun moveToTrashIfDecrypted(noteId: Int): Result<Unit> {
        return repository.moveToTrashIfDecrypted(noteId)
    }

    suspend fun deleteOldDeletedNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
            repository.deleteOldDeletedNotes(thirtyDaysAgo)
        }
    }

    // Search functionality
    fun searchNotes(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query)
    }

    // Sorting functionality
    fun getNotesByStatusSortedByDate(status: String): LiveData<List<Note>> {
        return repository.getNotesByStatusSortedByDate(status)
    }

    fun getNotesByStatusSortedByTitle(status: String): LiveData<List<Note>> {
        return repository.getNotesByStatusSortedByTitle(status)
    }

    fun getNotesByStatusSortedByLastEdited(status: String): LiveData<List<Note>> {
        return repository.getNotesByStatusSortedByLastEdited(status)
    }

    // Auto-move NEW notes to NOTES after 2 days
    suspend fun moveNewNotesToNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
            repository.moveNewNotesToNotes(twoDaysAgo)
        }
    }

    // Export/Import functionality
    suspend fun getAllNotesSync(): List<Note> {
        return repository.getAllNotesSync()
    }

    // Additional convenience methods
    fun softDeleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.softDeleteNote(note.id, System.currentTimeMillis())
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreNote(note.id)
        }
    }

    fun deleteNotePermanently(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotePermanently(note.id)
        }
    }

    fun restoreAllNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreAllNotes()
        }
    }

    fun deleteAllNotesPermanently() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotesPermanently()
        }
    }
}
