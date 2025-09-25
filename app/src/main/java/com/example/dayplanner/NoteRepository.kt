package com.example.dayplanner

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val deletedNotes: LiveData<List<Note>> = noteDao.getDeletedNotes()

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

    suspend fun softDeleteById(id: Int) {
        noteDao.softDeleteById(id)
    }

    suspend fun restoreById(id: Int) {
        noteDao.restoreById(id)
    }
}
