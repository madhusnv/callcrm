package com.educonsult.crm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CallLog
import android.telephony.TelephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PhoneStateReceiver(
    private val onCallEnded: (CallInfo) -> Unit
) : BroadcastReceiver() {

    private var callStartTime: Long = 0
    private var isIncoming: Boolean = false
    private var currentNumber: String = ""
    private var currentState: Int = TelephonyManager.CALL_STATE_IDLE

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // Incoming call
                isIncoming = true
                currentNumber = phoneNumber
                currentState = TelephonyManager.CALL_STATE_RINGING
            }
            
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call answered or outgoing call started
                val wasRinging = currentState == TelephonyManager.CALL_STATE_RINGING
                
                if (!wasRinging) {
                    // Outgoing call
                    isIncoming = false
                    currentNumber = phoneNumber
                }
                
                callStartTime = System.currentTimeMillis()
                currentState = TelephonyManager.CALL_STATE_OFFHOOK
            }
            
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended
                if (currentState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    // We were in a call, now ended
                    val duration = ((System.currentTimeMillis() - callStartTime) / 1000).toInt()
                    
                    // Delay slightly to let system update call log
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(500)
                        val callInfo = getCallInfo(context, currentNumber, duration, isIncoming)
                        onCallEnded(callInfo)
                    }
                } else if (currentState == TelephonyManager.CALL_STATE_RINGING) {
                    // Missed call
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(500)
                        val callInfo = CallInfo(
                            phoneNumber = currentNumber,
                            callType = CallType.MISSED.value,
                            duration = 0,
                            callAt = System.currentTimeMillis(),
                            simSlot = null,
                            deviceCallId = System.currentTimeMillis().toString(),
                            contactName = null
                        )
                        onCallEnded(callInfo)
                    }
                }
                
                // Reset state
                currentState = TelephonyManager.CALL_STATE_IDLE
                callStartTime = 0
                currentNumber = ""
                isIncoming = false
            }
        }
    }

    private fun getCallInfo(
        context: Context,
        phoneNumber: String,
        duration: Int,
        isIncoming: Boolean
    ): CallInfo {
        var callInfo = CallInfo(
            phoneNumber = phoneNumber,
            callType = if (isIncoming) CallType.INCOMING.value else CallType.OUTGOING.value,
            duration = duration,
            callAt = System.currentTimeMillis(),
            simSlot = null,
            deviceCallId = System.currentTimeMillis().toString(),
            contactName = null
        )

        // Try to get more details from system call log
        try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE,
                    CallLog.Calls.CACHED_NAME
                ),
                "${CallLog.Calls.NUMBER} LIKE ?",
                arrayOf("%${phoneNumber.takeLast(10)}%"),
                "${CallLog.Calls.DATE} DESC LIMIT 1"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                    val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val actualDuration = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))

                    callInfo = callInfo.copy(
                        deviceCallId = id.toString(),
                        duration = actualDuration,
                        callAt = date,
                        contactName = name,
                        callType = when (type) {
                            CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING.value
                            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING.value
                            CallLog.Calls.MISSED_TYPE -> CallType.MISSED.value
                            else -> callInfo.callType
                        }
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or other error, use fallback values
        }

        return callInfo
    }

    data class CallInfo(
        val phoneNumber: String,
        val callType: String,
        val duration: Int,
        val callAt: Long,
        val simSlot: Int?,
        val deviceCallId: String,
        val contactName: String?
    )

    enum class CallType(val value: String) {
        INCOMING("incoming"),
        OUTGOING("outgoing"),
        MISSED("missed")
    }
}
