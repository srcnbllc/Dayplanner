package com.example.dayplanner

import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(private val activityLogDao: ActivityLogDao) {
    
    fun getAllLogs(): Flow<List<ActivityLog>> = activityLogDao.getAllLogs()
    
    fun getLogsByAction(action: String): Flow<List<ActivityLog>> = activityLogDao.getLogsByAction(action)
    
    fun getLogsByNoteId(noteId: Int): Flow<List<ActivityLog>> = activityLogDao.getLogsByNoteId(noteId)
    
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityLog>> = 
        activityLogDao.getLogsByDateRange(startTime, endTime)
    
    suspend fun insertLog(log: ActivityLog) = activityLogDao.insert(log)
    
    suspend fun deleteLog(log: ActivityLog) = activityLogDao.delete(log)
    
    suspend fun deleteOldLogs(cutoffTime: Long) = activityLogDao.deleteOldLogs(cutoffTime)
    
    suspend fun getLogCount(): Int = activityLogDao.getLogCount()
    
    // Kolay kullanım için yardımcı fonksiyonlar
    suspend fun logNoteAction(action: String, noteId: Int, noteTitle: String, description: String) {
        val log = ActivityLog(
            action = action,
            description = description,
            noteId = noteId,
            noteTitle = noteTitle,
            timestamp = System.currentTimeMillis()
        )
        insertLog(log)
    }
    
    suspend fun logGeneralAction(action: String, description: String, details: String? = null) {
        val log = ActivityLog(
            action = action,
            description = description,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        insertLog(log)
    }
}
