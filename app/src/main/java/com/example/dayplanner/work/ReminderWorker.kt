package com.example.dayplanner.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.dayplanner.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val title = inputData.getString("title") ?: "Hatırlatma"
            val message = inputData.getString("message") ?: "Planlanan öğe zamanı"
            val noteId = inputData.getInt("noteId", -1)
            val reminderType = inputData.getString("reminderType") ?: "note"
            
            // Create enhanced notification
            NotificationHelper.showEnhancedNotification(
                applicationContext, 
                title, 
                message, 
                noteId,
                reminderType
            )
            
            // Log reminder completion
            android.util.Log.d("ReminderWorker", "Reminder sent for: $title")
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("ReminderWorker", "Error sending reminder", e)
            Result.retry()
        }
    }
    
    companion object {
        const val TITLE_KEY = "title"
        const val MESSAGE_KEY = "message"
        const val NOTE_ID_KEY = "noteId"
        const val REMINDER_TYPE_KEY = "reminderType"
        
        fun createInputData(
            title: String,
            message: String,
            noteId: Int = -1,
            reminderType: String = "note"
        ) = workDataOf(
            TITLE_KEY to title,
            MESSAGE_KEY to message,
            NOTE_ID_KEY to noteId,
            REMINDER_TYPE_KEY to reminderType
        )
    }
}


