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
    val deletedNotes: LiveData<List<Note>>

    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
        deletedNotes = repository.deletedNotes
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
}
