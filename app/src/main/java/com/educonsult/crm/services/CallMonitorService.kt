package com.educonsult.crm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.educonsult.crm.R
import com.educonsult.crm.data.local.db.dao.CallLogDao
import com.educonsult.crm.data.local.db.dao.LeadDao
import com.educonsult.crm.data.local.db.entity.CallLogEntity
import com.educonsult.crm.domain.repository.CallRepository
import com.educonsult.crm.receivers.PhoneStateReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class CallMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "call_monitor_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "com.educonsult.crm.START_CALL_MONITOR"
        const val ACTION_STOP = "com.educonsult.crm.STOP_CALL_MONITOR"
        
        fun startService(context: Context) {
            val intent = Intent(context, CallMonitorService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, CallMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    @Inject
    lateinit var callRepository: CallRepository
    
    @Inject
    lateinit var callLogDao: CallLogDao
    
    @Inject
    lateinit var leadDao: LeadDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var phoneStateReceiver: PhoneStateReceiver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification())
                registerPhoneStateReceiver()
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterPhoneStateReceiver()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors incoming and outgoing calls"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call Tracking Active")
            .setContentText("Monitoring calls for lead sync")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun registerPhoneStateReceiver() {
        if (phoneStateReceiver == null) {
            phoneStateReceiver = PhoneStateReceiver { callInfo ->
                handleCallEnded(callInfo)
            }
            
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            registerReceiver(phoneStateReceiver, filter)
        }
    }

    private fun unregisterPhoneStateReceiver() {
        phoneStateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Already unregistered
            }
            phoneStateReceiver = null
        }
    }

    private fun handleCallEnded(callInfo: PhoneStateReceiver.CallInfo) {
        serviceScope.launch {
            try {
                // Create call log entity
                val callLog = CallLogEntity(
                    id = UUID.randomUUID().toString(),
                    phoneNumber = callInfo.phoneNumber,
                    callType = callInfo.callType,
                    duration = callInfo.duration,
                    callAt = callInfo.callAt,
                    simSlot = callInfo.simSlot,
                    deviceCallId = callInfo.deviceCallId,
                    contactName = callInfo.contactName,
                    syncStatus = CallLogEntity.SYNC_PENDING
                )

                // Save to local database
                callRepository.insertCallLog(callLog)

                // Schedule sync worker
                scheduleSyncWorker()

                // Check if this is a known lead - if so, show note popup
                val lead = leadDao.getByPhone(normalizePhone(callInfo.phoneNumber))
                    ?: leadDao.getByPhone(callInfo.phoneNumber)

                if (lead != null && callInfo.duration > 0) {
                    showNotePopup(callLog.id, lead.id, lead.firstName, callInfo.phoneNumber, callInfo.duration)
                }
            } catch (e: Exception) {
                // Log error but don't crash service
            }
        }
    }

    private fun scheduleSyncWorker() {
        // Schedule WorkManager to sync call logs
        // This will be implemented via CallSyncWorker
    }

    private fun showNotePopup(callLogId: String, leadId: String, leadName: String, phone: String, duration: Int) {
        // Launch NotePopupActivity as overlay
        // Note: Requires SYSTEM_ALERT_WINDOW permission
        val intent = Intent(this, NotePopupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("call_log_id", callLogId)
            putExtra("lead_id", leadId)
            putExtra("lead_name", leadName)
            putExtra("phone_number", phone)
            putExtra("duration", duration)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Failed to show popup
        }
    }

    private fun normalizePhone(phone: String): String {
        return phone
            .replace(Regex("[\\s\\-\\(\\)]"), "")
            .replace(Regex("^\\+91"), "")
            .replace(Regex("^91(?=\\d{10}$)"), "")
    }
}
