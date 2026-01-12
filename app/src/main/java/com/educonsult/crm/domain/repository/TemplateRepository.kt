package com.educonsult.crm.domain.repository

import com.educonsult.crm.data.remote.dto.template.MessageTemplateDto
import com.educonsult.crm.data.remote.dto.template.NoteTemplateDto

interface TemplateRepository {
    suspend fun getNoteTemplates(): Result<List<NoteTemplateDto>>
    suspend fun getMessageTemplates(): Result<List<MessageTemplateDto>>
    suspend fun renderTemplate(templateId: String, values: Map<String, String>): Result<String>
}
