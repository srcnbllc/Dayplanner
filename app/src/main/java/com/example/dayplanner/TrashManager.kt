package com.example.dayplanner

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object TrashManager {
    private const val CLEANUP_WORK_NAME = "trash_cleanup_work"
    private const val NEW_NOTES_MOVE_WORK_NAME = "new_notes_move_work"

    /**
     * Schedule periodic cleanup of old deleted notes (30 days)
     */
    fun scheduleCleanup(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
            1, TimeUnit.DAYS // Run daily
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                CLEANUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )

        // Schedule NEW notes to move to NOTES after 2 days
        val moveNewNotesRequest = PeriodicWorkRequestBuilder<NewNotesMoveWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                NEW_NOTES_MOVE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                moveNewNotesRequest
            )
    }

    /**
     * Cancel scheduled cleanup
     */
    fun cancelCleanup(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CLEANUP_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(NEW_NOTES_MOVE_WORK_NAME)
    }
}

/**
 * Worker to clean up old deleted notes (30+ days old)
 */
class TrashCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = NoteDatabase.getDatabase(applicationContext)
            val noteDao = db.noteDao()
            
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
            val deletedCount = noteDao.deleteOldDeletedNotes(thirtyDaysAgo)
            
            android.util.Log.d("TrashCleanupWorker", "Cleaned up $deletedCount old deleted notes")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("TrashCleanupWorker", "Error cleaning up trash: ${e.message}", e)
            Result.retry()
        }
    }
}

/**
 * Worker to move NEW notes to NOTES after 2 days
 */
class NewNotesMoveWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = NoteDatabase.getDatabase(applicationContext)
            val noteDao = db.noteDao()
            
            val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
            val movedCount = noteDao.moveNewNotesToNotes(twoDaysAgo)
            
            android.util.Log.d("NewNotesMoveWorker", "Moved $movedCount NEW notes to NOTES")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("NewNotesMoveWorker", "Error moving NEW notes: ${e.message}", e)
            Result.retry()
        }
    }
}