package com.educonsult.crm.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lead_notes",
    foreignKeys = [
        ForeignKey(
            entity = LeadEntity::class,
            parentColumns = ["id"],
            childColumns = ["leadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["leadId"]),
        Index(value = ["createdAt"]),
        Index(value = ["syncStatus"])
    ]
)
data class LeadNoteEntity(
    @PrimaryKey val id: String,
    val leadId: String,
    val content: String,
    val noteType: String = "general",
    val createdBy: String,
    val syncStatus: Int = 0,
    val lastSyncedAt: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
