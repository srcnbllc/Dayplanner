package com.example.dayplanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(private val activityLogDao: ActivityLogDao) {
    
    fun getAllLogs(): Flow<List<ActivityLog>> = activityLogDao.getAllLogs()
    
    fun getLogsByAction(action: String): Flow<List<ActivityLog>> = activityLogDao.getLogsByAction(action)
    
    fun getLogsByNoteId(noteId: Int): Flow<List<ActivityLog>> = activityLogDao.getLogsByNoteId(noteId)
    
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityLog>> = 
        activityLogDao.getLogsByDateRange(startTime, endTime)
    
    suspend fun insert(log: ActivityLog) = activityLogDao.insert(log)
    
    suspend fun delete(log: ActivityLog) = activityLogDao.delete(log)
    
    suspend fun deleteOldLogs(cutoffTime: Long) = activityLogDao.deleteOldLogs(cutoffTime)
    
    suspend fun getLogCount(): Int = activityLogDao.getLogCount()
    
    suspend fun logNoteAction(action: String, noteId: Int, noteTitle: String, description: String) {
        val log = ActivityLog(
            action = action,
            description = description,
            noteId = noteId,
            noteTitle = noteTitle,
            timestamp = System.currentTimeMillis()
        )
        insert(log)
    }
    
    suspend fun logGeneralAction(action: String, description: String, details: String? = null) {
        val log = ActivityLog(
            action = action,
            description = description,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        insert(log)
    }
}