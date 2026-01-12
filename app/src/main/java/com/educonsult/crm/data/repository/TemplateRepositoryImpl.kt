package com.educonsult.crm.data.repository

import com.educonsult.crm.data.remote.api.TemplateApi
import com.educonsult.crm.data.remote.dto.template.MessageTemplateDto
import com.educonsult.crm.data.remote.dto.template.NoteTemplateDto
import com.educonsult.crm.data.remote.dto.template.RenderTemplateRequest
import com.educonsult.crm.domain.repository.TemplateRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val templateApi: TemplateApi
) : TemplateRepository {

    override suspend fun getNoteTemplates(): Result<List<NoteTemplateDto>> {
        return try {
            val response = templateApi.getNoteTemplates()
            Result.success(response.noteTemplates.filter { it.active })
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch note templates")
            Result.failure(e)
        }
    }

    override suspend fun getMessageTemplates(): Result<List<MessageTemplateDto>> {
        return try {
            val response = templateApi.getMessageTemplates()
            Result.success(response.messageTemplates.filter { it.active })
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch message templates")
            Result.failure(e)
        }
    }

    override suspend fun renderTemplate(
        templateId: String,
        values: Map<String, String>
    ): Result<String> {
        return try {
            val response = templateApi.renderTemplate(
                RenderTemplateRequest(templateId, values)
            )
            Result.success(response.renderedContent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to render template")
            Result.failure(e)
        }
    }
}
