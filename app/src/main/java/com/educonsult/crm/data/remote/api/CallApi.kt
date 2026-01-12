package com.educonsult.crm.data.remote.api

import com.educonsult.crm.data.remote.dto.call.request.ConfirmUploadRequest
import com.educonsult.crm.data.remote.dto.call.request.GetCallsByLeadRequest
import com.educonsult.crm.data.remote.dto.call.request.GetUploadUrlRequest
import com.educonsult.crm.data.remote.dto.call.request.SyncCallLogsRequest
import com.educonsult.crm.data.remote.dto.call.request.SyncNoteRequest
import com.educonsult.crm.data.remote.dto.call.response.CallLogDto
import com.educonsult.crm.data.remote.dto.call.response.CallLogListResponse
import com.educonsult.crm.data.remote.dto.call.response.SyncResult
import com.educonsult.crm.data.remote.dto.call.response.UploadUrlResponse
import com.educonsult.crm.data.remote.dto.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CallApi {

    @POST("callLog/sync")
    suspend fun syncCallLogs(@Body request: SyncCallLogsRequest): Response<BaseResponse<SyncResult>>

    @POST("callLog/sync/note")
    suspend fun syncNote(@Body request: SyncNoteRequest): Response<BaseResponse<Unit>>

    @POST("callLog/getByLead")
    suspend fun getCallsByLead(@Body request: GetCallsByLeadRequest): Response<BaseResponse<CallLogListResponse>>

    @POST("callRecording/getUploadUrl")
    suspend fun getUploadUrl(@Body request: GetUploadUrlRequest): Response<BaseResponse<UploadUrlResponse>>

    @POST("callRecording/confirmUpload")
    suspend fun confirmUpload(@Body request: ConfirmUploadRequest): Response<BaseResponse<Unit>>

    @GET("callRecording/stream/{id}")
    suspend fun getStreamUrl(@Path("id") recordingId: String): Response<BaseResponse<StreamUrlResponse>>

    data class StreamUrlResponse(
        val streamUrl: String,
        val expiresIn: Int
    )
}
