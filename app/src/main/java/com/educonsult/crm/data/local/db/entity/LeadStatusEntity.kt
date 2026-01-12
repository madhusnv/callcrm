package com.educonsult.crm.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lead_statuses",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["sortOrder"])
    ]
)
data class LeadStatusEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val description: String?,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
