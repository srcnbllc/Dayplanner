package com.example.dayplanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    val deletedNotes: LiveData<List<Note>>
    
    // Filter LiveData properties for Task 1
    val allNotesSorted: LiveData<List<Note>>
    val recentlyAddedNotes: LiveData<List<Note>>
    val pinnedNotes: LiveData<List<Note>>
    val encryptedNotes: LiveData<List<Note>>
    val passwordProtectedNotes: LiveData<List<Note>>

    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        val folderDao = NoteDatabase.getDatabase(application).folderDao()
        repository = NoteRepository(noteDao, folderDao)
        allNotes = repository.allNotes
        deletedNotes = repository.deletedNotes
        
        // Initialize filter LiveData
        allNotesSorted = noteDao.getAllNotesSorted()
        recentlyAddedNotes = noteDao.getRecentlyAddedNotes()
        pinnedNotes = noteDao.getPinnedNotes()
        encryptedNotes = noteDao.getEncryptedNotes()
        passwordProtectedNotes = noteDao.getPasswordProtectedNotes()
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

    fun softDeleteById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.softDeleteById(id)
        }
    }

    fun restoreById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreById(id)
        }
    }

    fun deleteNotePermanently(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotePermanently(id)
        }
    }

    fun restoreNote(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreNote(id)
        }
    }

    fun softDeleteNote(id: Int, deletedAt: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.softDeleteNote(id, deletedAt)
        }
    }

    fun deleteOldDeletedNotes(cutoffTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteOldDeletedNotes(cutoffTime)
        }
    }

    fun moveNewNotesToNotes(cutoffTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.moveNewNotesToNotes(cutoffTime)
        }
    }

    fun searchNotes(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query)
    }

    fun getNotesByStatusSortedByDate(status: String): LiveData<List<Note>> {
        return repository.getNotesByStatusSortedByDate(status)
    }

    fun getAllNotesSync(): List<Note> {
        return runBlocking { repository.getAllNotesSync() }
    }

    fun setPinned(id: Int, pinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setPinned(id, pinned)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(note)
        }
    }

    suspend fun moveToTrashIfDecrypted(noteId: Int): Result<Unit> {
        return repository.moveToTrashIfDecrypted(noteId)
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


