package com.educonsult.crm.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.educonsult.crm.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages notifications for the recording pipeline progress.
 */
@Singleton
class RecordingNotificationManager @Inject constructor(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "recording_pipeline"
        const val CHANNEL_NAME = "Recording Upload"
        const val NOTIFICATION_ID_BASE = 5000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of call recording uploads"
                setShowBadge(false)
            }
            
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showFindingNotification(callLogId: String) {
        showNotification(
            callLogId = callLogId,
            title = "Finding Recording",
            message = "Searching for call recording...",
            progress = 10
        )
    }

    fun showCompressingNotification(callLogId: String) {
        showNotification(
            callLogId = callLogId,
            title = "Processing Recording",
            message = "Compressing audio file...",
            progress = 40
        )
    }

    fun showUploadingNotification(callLogId: String, percent: Int = 0) {
        val adjustedProgress = 60 + (percent * 40 / 100)
        showNotification(
            callLogId = callLogId,
            title = "Uploading Recording",
            message = "Uploading to server... ${percent}%",
            progress = adjustedProgress
        )
    }

    fun showSuccessNotification(callLogId: String) {
        if (!hasNotificationPermission()) return
        
        val notificationId = callLogId.hashCode() + NOTIFICATION_ID_BASE
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recording Uploaded")
            .setContentText("Call recording synced successfully")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
        
        // Auto-dismiss after 3 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            NotificationManagerCompat.from(context).cancel(notificationId)
        }, 3000)
    }

    fun showErrorNotification(callLogId: String, error: String) {
        if (!hasNotificationPermission()) return
        
        val notificationId = callLogId.hashCode() + NOTIFICATION_ID_BASE
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recording Upload Failed")
            .setContentText(error)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun cancelNotification(callLogId: String) {
        val notificationId = callLogId.hashCode() + NOTIFICATION_ID_BASE
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun showNotification(
        callLogId: String,
        title: String,
        message: String,
        progress: Int
    ) {
        if (!hasNotificationPermission()) return
        
        val notificationId = callLogId.hashCode() + NOTIFICATION_ID_BASE
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
