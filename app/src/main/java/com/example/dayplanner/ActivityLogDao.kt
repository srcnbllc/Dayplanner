package com.example.dayplanner

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_log_table ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_log_table WHERE action = :action ORDER BY timestamp DESC")
    fun getLogsByAction(action: String): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_log_table WHERE noteId = :noteId ORDER BY timestamp DESC")
    fun getLogsByNoteId(noteId: Int): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_log_table WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityLog>>
    
    @Insert
    suspend fun insert(log: ActivityLog)
    
    @Delete
    suspend fun delete(log: ActivityLog)
    
    @Query("DELETE FROM activity_log_table WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
    
    @Query("SELECT COUNT(*) FROM activity_log_table")
    suspend fun getLogCount(): Int
}
