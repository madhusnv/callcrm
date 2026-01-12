package com.educonsult.crm.data.remote.api

import com.educonsult.crm.data.remote.dto.lead.request.GetByNumberRequest
import com.educonsult.crm.data.remote.dto.lead.request.GetLeadsRequest
import com.educonsult.crm.data.remote.dto.lead.request.GetNotesRequest
import com.educonsult.crm.data.remote.dto.lead.request.SaveLeadRequest
import com.educonsult.crm.data.remote.dto.lead.request.SaveNoteRequest
import com.educonsult.crm.data.remote.dto.lead.response.LeadListResponse
import com.educonsult.crm.data.remote.dto.lead.response.LeadResponse
import com.educonsult.crm.data.remote.dto.lead.response.LeadStatusDto
import com.educonsult.crm.data.remote.dto.lead.response.NoteDto
import com.educonsult.crm.data.remote.dto.lead.response.NoteResponse
import com.educonsult.crm.data.remote.dto.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LeadApi {

    @POST("lead/getData")
    suspend fun getLeads(@Body request: GetLeadsRequest): Response<BaseResponse<LeadListResponse>>

    @POST("lead/save")
    suspend fun saveLead(@Body request: SaveLeadRequest): Response<BaseResponse<LeadResponse>>

    @POST("lead/saveNote")
    suspend fun saveNote(@Body request: SaveNoteRequest): Response<BaseResponse<NoteResponse>>

    @POST("lead/status")
    suspend fun getStatuses(): Response<BaseResponse<List<LeadStatusDto>>>

    @POST("lead/getByNumber")
    suspend fun getByNumber(@Body request: GetByNumberRequest): Response<BaseResponse<LeadResponse>>

    @POST("lead/note")
    suspend fun getNotes(@Body request: GetNotesRequest): Response<BaseResponse<List<NoteDto>>>

    @POST("lead/delete")
    suspend fun deleteLead(@Body request: Map<String, String>): Response<BaseResponse<Unit>>
}
