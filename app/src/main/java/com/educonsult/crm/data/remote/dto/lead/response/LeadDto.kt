package com.educonsult.crm.data.remote.dto.lead.response

import kotlinx.serialization.Serializable

@Serializable
data class LeadDto(
    val id: String,
    val firstName: String,
    val lastName: String? = null,
    val phone: String,
    val secondaryPhone: String? = null,
    val countryCode: Int = 91,
    val email: String? = null,
    val studentName: String? = null,
    val parentName: String? = null,
    val relationship: String? = null,
    val dateOfBirth: String? = null,
    val currentEducation: String? = null,
    val currentInstitution: String? = null,
    val percentage: Float? = null,
    val stream: String? = null,
    val graduationYear: Int? = null,
    val interestedCourses: List<String>? = null,
    val preferredCountries: List<String>? = null,
    val preferredInstitutions: List<String>? = null,
    val budgetMin: Long? = null,
    val budgetMax: Long? = null,
    val intakePreference: String? = null,
    val statusId: String? = null,
    val status: LeadStatusDto? = null,
    val priority: String = "medium",
    val source: String? = null,
    val assignedTo: String? = null,
    val branchId: String? = null,
    val lastContactDate: String? = null,
    val nextFollowUpDate: String? = null,
    val reminderNote: String? = null,
    val totalCalls: Int = 0,
    val totalNotes: Int = 0,
    val createdAt: String,
    val updatedAt: String
)
