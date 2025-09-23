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
}
