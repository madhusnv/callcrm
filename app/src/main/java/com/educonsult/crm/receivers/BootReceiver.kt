package com.educonsult.crm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.educonsult.crm.services.CallMonitorService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Restart call monitoring service on boot
            CallMonitorService.startService(context)
        }
    }
}
