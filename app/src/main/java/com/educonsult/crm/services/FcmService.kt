package com.educonsult.crm.services

import android.os.Build
import android.app.PendingIntent
import android.content.Intent
import com.educonsult.crm.MainActivity
import com.educonsult.crm.domain.usecase.employee.UpdateFcmTokenUseCase
import com.educonsult.crm.ui.navigation.Screen
import com.educonsult.crm.util.AppNotificationManager
import com.educonsult.crm.util.NotificationChannels
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            updateFcmTokenUseCase(
                token = token,
                deviceInfo = buildDeviceInfo()
            )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "EduConsult CRM"
        val body = data["message"] ?: message.notification?.body ?: "You have a new update."

        val route = when {
            data["leadId"] != null -> Screen.LeadDetail.createRoute(data.getValue("leadId"))
            data["courseId"] != null -> Screen.CourseDetail.createRoute(data.getValue("courseId"))
            else -> Screen.Dashboard.route
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_ROUTE, route)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        AppNotificationManager(this).show(
            channelId = NotificationChannels.GENERAL,
            title = title,
            message = body,
            notificationId = System.currentTimeMillis().toInt(),
            intent = pendingIntent
        )
    }

    private fun buildDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "sdk" to Build.VERSION.SDK_INT.toString()
        )
    }

    companion object {
        const val EXTRA_NAV_ROUTE = "nav_route"
    }
}
