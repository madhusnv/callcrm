package com.educonsult.crm.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leads",
    foreignKeys = [
        ForeignKey(
            entity = LeadStatusEntity::class,
            parentColumns = ["id"],
            childColumns = ["statusId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["phone"]),
        Index(value = ["statusId"]),
        Index(value = ["assignedTo"]),
        Index(value = ["syncStatus"]),
        Index(value = ["nextFollowUpDate"]),
        Index(value = ["deletedAt"])
    ]
)
data class LeadEntity(
    @PrimaryKey val id: String,

    // Personal Info
    val firstName: String,
    val lastName: String?,
    val phone: String,
    val secondaryPhone: String?,
    val countryCode: Int = 91,
    val email: String?,

    // Student Info
    val studentName: String?,
    val parentName: String?,
    val relationship: String?,
    val dateOfBirth: Long?,

    // Education
    val currentEducation: String?,
    val currentInstitution: String?,
    val percentage: Float?,
    val stream: String?,
    val graduationYear: Int?,

    // Inquiry (store as JSON strings)
    val interestedCourses: String?,
    val preferredCountries: String?,
    val preferredInstitutions: String?,
    val budgetMin: Long?,
    val budgetMax: Long?,
    val intakePreference: String?,

    // Status
    val statusId: String?,
    val priority: String = "medium",
    val source: String?,

    // Assignment
    val assignedTo: String?,
    val branchId: String?,

    // Follow-up
    val lastContactDate: Long?,
    val nextFollowUpDate: Long?,
    val reminderNote: String?,

    // Stats
    val totalCalls: Int = 0,
    val totalNotes: Int = 0,

    // Sync
    val syncStatus: Int = 0,
    val lastSyncedAt: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
