package com.educonsult.crm.data.remote.dto.call.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncResult(
    val created: Int,
    val updated: Int,
    @SerialName("leads_matched") val leadsMatched: Int
)

@Serializable
data class CallLogListResponse(
    @SerialName("call_logs") val callLogs: List<CallLogDto>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    val total: Int,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_pages") val totalPages: Int
)

@Serializable
data class CallLogDto(
    val id: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("call_type") val callType: String,
    val duration: Int,
    @SerialName("call_at") val callAt: String,
    @SerialName("sim_slot") val simSlot: Int? = null,
    @SerialName("device_call_id") val deviceCallId: String? = null,
    @SerialName("contact_name") val contactName: String? = null,
    val notes: String? = null,
    @SerialName("lead_id") val leadId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val lead: LeadSummary? = null,
    val user: UserSummary? = null,
    val recording: RecordingDto? = null,
    @SerialName("inserted_at") val insertedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class LeadSummary(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String? = null,
    val phone: String
)

@Serializable
data class UserSummary(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String? = null
)

@Serializable
data class RecordingDto(
    val id: String,
    val status: String,
    val duration: Int? = null,
    val format: String? = null,
    @SerialName("file_size") val fileSize: Long? = null
)

@Serializable
data class UploadUrlResponse(
    @SerialName("recording_id") val recordingId: String,
    @SerialName("upload_url") val uploadUrl: String,
    @SerialName("storage_key") val storageKey: String,
    @SerialName("expires_in") val expiresIn: Int
)
