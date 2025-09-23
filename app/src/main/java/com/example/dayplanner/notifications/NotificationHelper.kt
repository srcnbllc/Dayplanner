package com.example.dayplanner.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.dayplanner.MainActivity
import com.example.dayplanner.R
import com.example.dayplanner.AddNoteActivity

object NotificationHelper {
    private const val CHANNEL_ID = "reminders"
    private const val CHANNEL_ID_IMPORTANT = "important_reminders"
    private const val CHANNEL_ID_FINANCE = "finance_reminders"
    private const val CHANNEL_ID_PASSWORD = "password_reminders"

    fun initializeChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            
            // General reminders channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID, 
                "Genel Hatırlatmalar", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notlar ve genel hatırlatmalar için"
                enableVibration(true)
                enableLights(true)
            }
            
            // Important reminders channel
            val importantChannel = NotificationChannel(
                CHANNEL_ID_IMPORTANT, 
                "Önemli Hatırlatmalar", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Önemli notlar ve acil hatırlatmalar için"
                enableVibration(true)
                enableLights(true)
            }
            
            // Finance reminders channel
            val financeChannel = NotificationChannel(
                CHANNEL_ID_FINANCE, 
                "Finans Hatırlatmaları", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Finansal işlemler ve ödemeler için"
                enableVibration(true)
            }
            
            // Password reminders channel
            val passwordChannel = NotificationChannel(
                CHANNEL_ID_PASSWORD, 
                "Şifre Hatırlatmaları", 
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Şifre güncellemeleri ve güvenlik hatırlatmaları için"
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(generalChannel, importantChannel, financeChannel, passwordChannel)
            )
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        showEnhancedNotification(context, title, message, -1, "general")
    }

    fun showEnhancedNotification(
        context: Context, 
        title: String, 
        message: String, 
        noteId: Int = -1,
        reminderType: String = "note"
    ) {
        initializeChannels(context)
        
        val channelId = when (reminderType) {
            "important" -> CHANNEL_ID_IMPORTANT
            "finance" -> CHANNEL_ID_FINANCE
            "password" -> CHANNEL_ID_PASSWORD
            else -> CHANNEL_ID
        }
        
        val priority = when (reminderType) {
            "important" -> NotificationCompat.PRIORITY_HIGH
            "finance" -> NotificationCompat.PRIORITY_DEFAULT
            "password" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
        
        val icon = when (reminderType) {
            "finance" -> R.drawable.ic_money
            "password" -> R.drawable.ic_security
            "important" -> R.drawable.ic_priority
            else -> R.drawable.ic_note
        }
        
        // Create intent to open the app
        val intent = if (noteId != -1) {
            Intent(context, AddNoteActivity::class.java).apply {
                putExtra("noteId", noteId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            noteId.takeIf { it != -1 } ?: System.currentTimeMillis().toInt(),
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
            
        val notificationId = if (noteId != -1) noteId else System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}


