package com.educonsult.crm.data.remote.dto.call.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncCallLogsRequest(
    @SerialName("call_logs") val callLogs: List<CallLogRequest>
)

@Serializable
data class CallLogRequest(
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("call_type") val callType: String,
    val duration: Int,
    @SerialName("call_at") val callAt: String,  // ISO 8601 format
    @SerialName("device_call_id") val deviceCallId: String,
    @SerialName("sim_slot") val simSlot: Int? = null,
    @SerialName("contact_name") val contactName: String? = null,
    val notes: String? = null
)

@Serializable
data class SyncNoteRequest(
    @SerialName("call_log_id") val callLogId: String,
    val notes: String
)

@Serializable
data class GetCallsByLeadRequest(
    @SerialName("lead_id") val leadId: String,
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 20
)

@Serializable
data class GetUploadUrlRequest(
    @SerialName("call_log_id") val callLogId: String,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
    val format: String = "mp3"
)

@Serializable
data class ConfirmUploadRequest(
    @SerialName("recording_id") val recordingId: String,
    @SerialName("compressed_file_size") val compressedFileSize: Long? = null,
    val duration: Int? = null,
    val format: String? = null,
    val bitrate: Int? = null
)
