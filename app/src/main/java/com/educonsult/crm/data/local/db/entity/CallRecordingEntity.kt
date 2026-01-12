package com.educonsult.crm.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_recordings",
    foreignKeys = [
        ForeignKey(
            entity = CallLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["callLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["callLogId"], unique = true),
        Index(value = ["status"])
    ]
)
data class CallRecordingEntity(
    @PrimaryKey val id: String,
    
    val callLogId: String,
    
    // File info
    val localFilePath: String? = null,
    val originalFileName: String? = null,
    val originalFileSize: Long? = null,
    val compressedFileSize: Long? = null,
    val duration: Int? = null,          // Duration in seconds
    val format: String = "mp3",
    
    // Remote storage
    val storageKey: String? = null,
    val storageUrl: String? = null,
    
    // Status: pending, compressing, uploading, uploaded, failed
    val status: String = STATUS_PENDING,
    val uploadProgress: Int = 0,        // 0-100 percent
    val retryCount: Int = 0,
    val lastError: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_FINDING = "finding"
        const val STATUS_COMPRESSING = "compressing"
        const val STATUS_UPLOADING = "uploading"
        const val STATUS_UPLOADED = "uploaded"
        const val STATUS_FAILED = "failed"
    }
}
