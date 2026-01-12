package com.educonsult.crm.data.mapper

import com.educonsult.crm.data.local.db.entity.LeadEntity
import com.educonsult.crm.data.local.db.entity.LeadNoteEntity
import com.educonsult.crm.data.local.db.entity.LeadStatusEntity
import com.educonsult.crm.data.remote.dto.lead.request.SaveLeadRequest
import com.educonsult.crm.data.remote.dto.lead.request.SaveNoteRequest
import com.educonsult.crm.data.remote.dto.lead.response.LeadDto
import com.educonsult.crm.data.remote.dto.lead.response.LeadStatusDto
import com.educonsult.crm.data.remote.dto.lead.response.NoteDto
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.model.LeadNote
import com.educonsult.crm.domain.model.LeadPriority
import com.educonsult.crm.domain.model.LeadStatus
import com.educonsult.crm.domain.model.NoteType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val json = Json { ignoreUnknownKeys = true }
private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

fun LeadEntity.toDomain(status: LeadStatusEntity? = null): Lead {
    return Lead(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        secondaryPhone = secondaryPhone,
        countryCode = countryCode,
        email = email,
        studentName = studentName,
        parentName = parentName,
        relationship = relationship,
        dateOfBirth = dateOfBirth?.let { 
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() 
        },
        currentEducation = currentEducation,
        currentInstitution = currentInstitution,
        percentage = percentage,
        stream = stream,
        graduationYear = graduationYear,
        interestedCourses = interestedCourses?.let { parseJsonList(it) } ?: emptyList(),
        preferredCountries = preferredCountries?.let { parseJsonList(it) } ?: emptyList(),
        preferredInstitutions = preferredInstitutions?.let { parseJsonList(it) } ?: emptyList(),
        budgetMin = budgetMin,
        budgetMax = budgetMax,
        intakePreference = intakePreference,
        status = status?.toDomain(),
        priority = priority.toLeadPriority(),
        source = source,
        assignedTo = assignedTo,
        branchId = branchId,
        lastContactDate = lastContactDate?.toLocalDateTime(),
        nextFollowUpDate = nextFollowUpDate?.toLocalDateTime(),
        reminderNote = reminderNote,
        totalCalls = totalCalls,
        totalNotes = totalNotes,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime()
    )
}

fun Lead.toEntity(syncStatus: Int = 0): LeadEntity {
    return LeadEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        secondaryPhone = secondaryPhone,
        countryCode = countryCode,
        email = email,
        studentName = studentName,
        parentName = parentName,
        relationship = relationship,
        dateOfBirth = dateOfBirth?.toEpochMilli(),
        currentEducation = currentEducation,
        currentInstitution = currentInstitution,
        percentage = percentage,
        stream = stream,
        graduationYear = graduationYear,
        interestedCourses = interestedCourses.toJsonString(),
        preferredCountries = preferredCountries.toJsonString(),
        preferredInstitutions = preferredInstitutions.toJsonString(),
        budgetMin = budgetMin,
        budgetMax = budgetMax,
        intakePreference = intakePreference,
        statusId = status?.id,
        priority = priority.name.lowercase(),
        source = source,
        assignedTo = assignedTo,
        branchId = branchId,
        lastContactDate = lastContactDate?.toEpochMilli(),
        nextFollowUpDate = nextFollowUpDate?.toEpochMilli(),
        reminderNote = reminderNote,
        totalCalls = totalCalls,
        totalNotes = totalNotes,
        syncStatus = syncStatus,
        lastSyncedAt = null,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}

fun LeadDto.toEntity(): LeadEntity {
    return LeadEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        secondaryPhone = secondaryPhone,
        countryCode = countryCode,
        email = email,
        studentName = studentName,
        parentName = parentName,
        relationship = relationship,
        dateOfBirth = dateOfBirth?.parseDate()?.toEpochMilli(),
        currentEducation = currentEducation,
        currentInstitution = currentInstitution,
        percentage = percentage,
        stream = stream,
        graduationYear = graduationYear,
        interestedCourses = interestedCourses?.toJsonString(),
        preferredCountries = preferredCountries?.toJsonString(),
        preferredInstitutions = preferredInstitutions?.toJsonString(),
        budgetMin = budgetMin,
        budgetMax = budgetMax,
        intakePreference = intakePreference,
        statusId = statusId,
        priority = priority,
        source = source,
        assignedTo = assignedTo,
        branchId = branchId,
        lastContactDate = lastContactDate?.parseDateTime()?.toEpochMilli(),
        nextFollowUpDate = nextFollowUpDate?.parseDateTime()?.toEpochMilli(),
        reminderNote = reminderNote,
        totalCalls = totalCalls,
        totalNotes = totalNotes,
        syncStatus = 0,
        lastSyncedAt = System.currentTimeMillis(),
        createdAt = createdAt.parseDateTime()?.toEpochMilli() ?: System.currentTimeMillis(),
        updatedAt = updatedAt.parseDateTime()?.toEpochMilli() ?: System.currentTimeMillis()
    )
}

fun LeadDto.toDomain(): Lead {
    return Lead(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        secondaryPhone = secondaryPhone,
        countryCode = countryCode,
        email = email,
        studentName = studentName,
        parentName = parentName,
        relationship = relationship,
        dateOfBirth = dateOfBirth?.parseDate(),
        currentEducation = currentEducation,
        currentInstitution = currentInstitution,
        percentage = percentage,
        stream = stream,
        graduationYear = graduationYear,
        interestedCourses = interestedCourses ?: emptyList(),
        preferredCountries = preferredCountries ?: emptyList(),
        preferredInstitutions = preferredInstitutions ?: emptyList(),
        budgetMin = budgetMin,
        budgetMax = budgetMax,
        intakePreference = intakePreference,
        status = status?.toDomain(),
        priority = priority.toLeadPriority(),
        source = source,
        assignedTo = assignedTo,
        branchId = branchId,
        lastContactDate = lastContactDate?.parseDateTime(),
        nextFollowUpDate = nextFollowUpDate?.parseDateTime(),
        reminderNote = reminderNote,
        totalCalls = totalCalls,
        totalNotes = totalNotes,
        createdAt = createdAt.parseDateTime() ?: LocalDateTime.now(),
        updatedAt = updatedAt.parseDateTime() ?: LocalDateTime.now()
    )
}

fun Lead.toSaveRequest(): SaveLeadRequest {
    return SaveLeadRequest(
        id = id.takeIf { it.isNotBlank() && !it.startsWith("local_") },
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        secondaryPhone = secondaryPhone,
        countryCode = countryCode,
        email = email,
        studentName = studentName,
        parentName = parentName,
        relationship = relationship,
        dateOfBirth = dateOfBirth?.format(dateFormatter),
        currentEducation = currentEducation,
        currentInstitution = currentInstitution,
        percentage = percentage,
        stream = stream,
        graduationYear = graduationYear,
        interestedCourses = interestedCourses.takeIf { it.isNotEmpty() },
        preferredCountries = preferredCountries.takeIf { it.isNotEmpty() },
        preferredInstitutions = preferredInstitutions.takeIf { it.isNotEmpty() },
        budgetMin = budgetMin,
        budgetMax = budgetMax,
        intakePreference = intakePreference,
        statusId = status?.id,
        priority = priority.name.lowercase(),
        source = source,
        assignedTo = assignedTo,
        branchId = branchId,
        nextFollowUpDate = nextFollowUpDate?.format(dateTimeFormatter),
        reminderNote = reminderNote
    )
}

fun LeadStatusEntity.toDomain(): LeadStatus {
    return LeadStatus(
        id = id,
        name = name,
        color = color,
        description = description,
        sortOrder = sortOrder,
        isDefault = isDefault,
        isActive = isActive
    )
}

fun LeadStatusDto.toEntity(): LeadStatusEntity {
    return LeadStatusEntity(
        id = id,
        name = name,
        color = color,
        description = description,
        sortOrder = sortOrder,
        isDefault = isDefault,
        isActive = isActive
    )
}

fun LeadStatusDto.toDomain(): LeadStatus {
    return LeadStatus(
        id = id,
        name = name,
        color = color,
        description = description,
        sortOrder = sortOrder,
        isDefault = isDefault,
        isActive = isActive
    )
}

fun LeadNoteEntity.toDomain(): LeadNote {
    return LeadNote(
        id = id,
        leadId = leadId,
        content = content,
        noteType = noteType.toNoteType(),
        createdBy = createdBy,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime()
    )
}

fun LeadNote.toEntity(syncStatus: Int = 0): LeadNoteEntity {
    return LeadNoteEntity(
        id = id,
        leadId = leadId,
        content = content,
        noteType = noteType.name.lowercase(),
        createdBy = createdBy,
        syncStatus = syncStatus,
        lastSyncedAt = null,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}

fun NoteDto.toEntity(): LeadNoteEntity {
    return LeadNoteEntity(
        id = id,
        leadId = leadId,
        content = content,
        noteType = noteType,
        createdBy = createdBy,
        syncStatus = 0,
        lastSyncedAt = System.currentTimeMillis(),
        createdAt = createdAt.parseDateTime()?.toEpochMilli() ?: System.currentTimeMillis(),
        updatedAt = updatedAt.parseDateTime()?.toEpochMilli() ?: System.currentTimeMillis()
    )
}

fun NoteDto.toDomain(): LeadNote {
    return LeadNote(
        id = id,
        leadId = leadId,
        content = content,
        noteType = noteType.toNoteType(),
        createdBy = createdBy,
        createdAt = createdAt.parseDateTime() ?: LocalDateTime.now(),
        updatedAt = updatedAt.parseDateTime() ?: LocalDateTime.now()
    )
}

fun LeadNote.toSaveRequest(): SaveNoteRequest {
    return SaveNoteRequest(
        id = id.takeIf { it.isNotBlank() && !it.startsWith("local_") },
        leadId = leadId,
        content = content,
        noteType = noteType.name.lowercase()
    )
}

private fun String.toLeadPriority(): LeadPriority {
    return when (this.lowercase()) {
        "low" -> LeadPriority.LOW
        "medium" -> LeadPriority.MEDIUM
        "high" -> LeadPriority.HIGH
        "urgent" -> LeadPriority.URGENT
        else -> LeadPriority.MEDIUM
    }
}

private fun String.toNoteType(): NoteType {
    return when (this.lowercase()) {
        "general" -> NoteType.GENERAL
        "call" -> NoteType.CALL
        "meeting" -> NoteType.MEETING
        "email" -> NoteType.EMAIL
        "follow_up" -> NoteType.FOLLOW_UP
        "status_change" -> NoteType.STATUS_CHANGE
        else -> NoteType.GENERAL
    }
}

private fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

private fun LocalDateTime.toEpochMilli(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun LocalDate.toEpochMilli(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun String.parseDate(): LocalDate? {
    return try {
        LocalDate.parse(this, dateFormatter)
    } catch (e: Exception) {
        try {
            LocalDate.parse(this.take(10))
        } catch (e: Exception) {
            null
        }
    }
}

private fun String.parseDateTime(): LocalDateTime? {
    return try {
        LocalDateTime.parse(this, dateTimeFormatter)
    } catch (e: Exception) {
        try {
            Instant.parse(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
        } catch (e: Exception) {
            null
        }
    }
}

private fun parseJsonList(jsonString: String): List<String> {
    return try {
        json.decodeFromString<List<String>>(jsonString)
    } catch (e: Exception) {
        emptyList()
    }
}

private fun List<String>.toJsonString(): String? {
    return if (isEmpty()) null else json.encodeToString(this)
}
