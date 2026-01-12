package com.educonsult.crm.domain.repository

import com.educonsult.crm.data.local.db.entity.CallLogEntity
import com.educonsult.crm.data.local.db.entity.CallRecordingEntity
import com.educonsult.crm.data.remote.dto.call.response.UploadUrlResponse
import kotlinx.coroutines.flow.Flow

interface CallRepository {
    
    // Call Logs
    fun getAllCallLogs(): Flow<List<CallLogEntity>>
    fun getCallLogsByLead(leadId: String): Flow<List<CallLogEntity>>
    suspend fun getCallLogById(id: String): CallLogEntity?
    suspend fun getCallLogByDeviceId(deviceCallId: String): CallLogEntity?
    suspend fun insertCallLog(callLog: CallLogEntity)
    suspend fun updateCallLogNotes(id: String, notes: String)
    suspend fun matchCallToLead(callLogId: String, leadId: String)
    
    // Sync
    suspend fun syncCallLogs(): Result<Int>
    suspend fun getPendingSyncCount(): Int
    
    // Recordings
    suspend fun getRecordingByCallLogId(callLogId: String): CallRecordingEntity?
    suspend fun createRecordingEntry(callLogId: String): String
    suspend fun updateRecordingLocalFile(recordingId: String, filePath: String, fileSize: Long)
    suspend fun getUploadUrl(callLogId: String): Result<UploadUrlResponse>
    suspend fun confirmUpload(recordingId: String, fileSize: Long, duration: Int): Result<Unit>
    suspend fun markRecordingFailed(recordingId: String, error: String)
    suspend fun getPendingRecordings(): List<CallRecordingEntity>
    
    // Streaming
    suspend fun getStreamUrl(recordingId: String): Result<String>
}
