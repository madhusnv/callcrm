package com.educonsult.crm.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Lead(
    val id: String,
    val firstName: String,
    val lastName: String?,
    val phone: String,
    val secondaryPhone: String?,
    val countryCode: Int = 91,
    val email: String?,
    val studentName: String?,
    val parentName: String?,
    val relationship: String?,
    val dateOfBirth: LocalDate?,
    val currentEducation: String?,
    val currentInstitution: String?,
    val percentage: Float?,
    val stream: String?,
    val graduationYear: Int?,
    val interestedCourses: List<String>,
    val preferredCountries: List<String>,
    val preferredInstitutions: List<String>,
    val budgetMin: Long?,
    val budgetMax: Long?,
    val intakePreference: String?,
    val status: LeadStatus?,
    val priority: LeadPriority,
    val source: String?,
    val assignedTo: String?,
    val branchId: String?,
    val lastContactDate: LocalDateTime?,
    val nextFollowUpDate: LocalDateTime?,
    val reminderNote: String?,
    val totalCalls: Int,
    val totalNotes: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ")

    val displayName: String
        get() = studentName ?: fullName
}

enum class LeadPriority {
    LOW, MEDIUM, HIGH, URGENT
}
