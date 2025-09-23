package com.example.dayplanner.reminders

import android.content.Context
import androidx.work.*
import com.example.dayplanner.work.ReminderWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun scheduleNoteReminder(
        noteId: Int,
        title: String,
        message: String,
        reminderTime: LocalDateTime,
        reminderType: String = "note"
    ) {
        val delay = Duration.between(LocalDateTime.now(), reminderTime)
        if (delay.isNegative) return
        
        val inputData = ReminderWorker.createInputData(
            title = title,
            message = message,
            noteId = noteId,
            reminderType = reminderType
        )
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag("note_reminder_$noteId")
            .build()
            
        workManager.enqueue(workRequest)
    }
    
    fun scheduleRecurringReminder(
        title: String,
        message: String,
        intervalMinutes: Long,
        reminderType: String = "general"
    ) {
        val inputData = ReminderWorker.createInputData(
            title = title,
            message = message,
            noteId = -1,
            reminderType = reminderType
        )
        
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag("recurring_reminder")
            .build()
            
        workManager.enqueue(workRequest)
    }
    
    fun scheduleFinanceReminder(
        title: String,
        message: String,
        reminderTime: LocalDateTime
    ) {
        scheduleNoteReminder(-1, title, message, reminderTime, "finance")
    }
    
    fun schedulePasswordReminder(
        title: String,
        message: String,
        reminderTime: LocalDateTime
    ) {
        scheduleNoteReminder(-1, title, message, reminderTime, "password")
    }
    
    fun cancelNoteReminder(noteId: Int) {
        workManager.cancelAllWorkByTag("note_reminder_$noteId")
    }
    
    fun cancelAllReminders() {
        workManager.cancelAllWork()
    }
    
    fun cancelRecurringReminders() {
        workManager.cancelAllWorkByTag("recurring_reminder")
    }
    
    fun getScheduledReminders(): List<WorkInfo> {
        return workManager.getWorkInfosByTag("note_reminder").get()
    }
    
    fun rescheduleReminder(
        noteId: Int,
        title: String,
        message: String,
        newReminderTime: LocalDateTime,
        reminderType: String = "note"
    ) {
        // Cancel existing reminder
        cancelNoteReminder(noteId)
        // Schedule new reminder
        scheduleNoteReminder(noteId, title, message, newReminderTime, reminderType)
    }
}
