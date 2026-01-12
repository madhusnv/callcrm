package com.educonsult.crm.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.educonsult.crm.R

object NotificationChannels {
    const val GENERAL = "general"
    const val REMINDERS = "reminders"

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel(
                GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }
}

class AppNotificationManager(private val context: Context) {

    fun show(
        channelId: String,
        title: String,
        message: String,
        notificationId: Int,
        intent: android.app.PendingIntent? = null
    ) {
        NotificationChannels.ensureChannels(context)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (intent != null) {
            builder.setContentIntent(intent)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
