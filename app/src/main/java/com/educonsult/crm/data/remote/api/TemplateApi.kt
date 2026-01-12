package com.educonsult.crm.data.remote.api

import com.educonsult.crm.data.remote.dto.template.NoteTemplateListResponse
import com.educonsult.crm.data.remote.dto.template.MessageTemplateListResponse
import com.educonsult.crm.data.remote.dto.template.RenderTemplateRequest
import com.educonsult.crm.data.remote.dto.template.RenderTemplateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TemplateApi {

    @POST("callnote/template/fetchAll")
    suspend fun getNoteTemplates(): NoteTemplateListResponse

    @POST("messagetemplate/fetchAll")
    suspend fun getMessageTemplates(): MessageTemplateListResponse

    @POST("messagetemplate/render")
    suspend fun renderTemplate(
        @Body request: RenderTemplateRequest
    ): RenderTemplateResponse
}
