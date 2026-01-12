package com.educonsult.crm.data.remote.dto.template

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============== Note Templates ==============

@Serializable
data class NoteTemplateListResponse(
    @SerialName("note_templates")
    val noteTemplates: List<NoteTemplateDto>
)

@Serializable
data class NoteTemplateDto(
    val id: String,
    val name: String,
    val content: String,
    val category: String,
    val shortcut: String? = null,
    val order: Int = 0,
    val active: Boolean = true
)

// ============== Message Templates ==============

@Serializable
data class MessageTemplateListResponse(
    @SerialName("message_templates")
    val messageTemplates: List<MessageTemplateDto>
)

@Serializable
data class MessageTemplateDto(
    val id: String,
    val name: String,
    val content: String,
    val category: String,
    @SerialName("dynamic_fields")
    val dynamicFields: List<String> = emptyList(),
    @SerialName("whatsapp_enabled")
    val whatsappEnabled: Boolean = true,
    @SerialName("sms_enabled")
    val smsEnabled: Boolean = true,
    val order: Int = 0,
    val active: Boolean = true
)

// ============== Render Template ==============

@Serializable
data class RenderTemplateRequest(
    @SerialName("template_id")
    val templateId: String,
    val values: Map<String, String>
)

@Serializable
data class RenderTemplateResponse(
    @SerialName("rendered_content")
    val renderedContent: String
)
