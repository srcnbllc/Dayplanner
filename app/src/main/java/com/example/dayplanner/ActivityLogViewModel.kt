package com.example.dayplanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ActivityLogViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ActivityLogRepository
    private val _allLogs = MutableLiveData<List<ActivityLog>>()
    val allLogs: LiveData<List<ActivityLog>> = _allLogs
    
    init {
        val database = NoteDatabase.getDatabase(application)
        repository = ActivityLogRepository(database.activityLogDao())
        
        // Tüm logları yükle
        viewModelScope.launch {
            repository.getAllLogs().collect { logs ->
                _allLogs.value = logs
            }
        }
    }
    
    fun getLogsByAction(action: String): LiveData<List<ActivityLog>> {
        val result = MutableLiveData<List<ActivityLog>>()
        viewModelScope.launch {
            repository.getLogsByAction(action).collect { logs ->
                result.value = logs
            }
        }
        return result
    }
    
    fun getLogsByDateRange(startTime: Long, endTime: Long): LiveData<List<ActivityLog>> {
        val result = MutableLiveData<List<ActivityLog>>()
        viewModelScope.launch {
            repository.getLogsByDateRange(startTime, endTime).collect { logs ->
                result.value = logs
            }
        }
        return result
    }
    
    fun logNoteAction(action: String, noteId: Int, noteTitle: String, description: String) {
        viewModelScope.launch {
            repository.logNoteAction(action, noteId, noteTitle, description)
        }
    }
    
    fun logGeneralAction(action: String, description: String, details: String? = null) {
        viewModelScope.launch {
            repository.logGeneralAction(action, description, details)
        }
    }
    
    fun deleteOldLogs(cutoffTime: Long) {
        viewModelScope.launch {
            repository.deleteOldLogs(cutoffTime)
        }
    }
}
