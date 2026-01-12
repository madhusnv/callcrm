package com.educonsult.crm.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_logs",
    foreignKeys = [
        ForeignKey(
            entity = LeadEntity::class,
            parentColumns = ["id"],
            childColumns = ["leadId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["leadId"]),
        Index(value = ["callAt"]),
        Index(value = ["callType"]),
        Index(value = ["syncStatus"]),
        Index(value = ["deviceCallId"], unique = true)
    ]
)
data class CallLogEntity(
    @PrimaryKey val id: String,
    
    val phoneNumber: String,
    val callType: String,           // "incoming", "outgoing", "missed"
    val duration: Int = 0,          // Duration in seconds
    val callAt: Long,               // Timestamp when call occurred
    val simSlot: Int? = null,
    val deviceCallId: String,       // Unique ID from device call log
    val contactName: String? = null,
    val notes: String? = null,
    
    // Association
    val leadId: String? = null,
    
    // Sync status: 0 = pending, 1 = synced, 2 = error
    val syncStatus: Int = 0,
    val lastSyncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_INCOMING = "incoming"
        const val TYPE_OUTGOING = "outgoing"
        const val TYPE_MISSED = "missed"
        
        const val SYNC_PENDING = 0
        const val SYNC_SYNCED = 1
        const val SYNC_ERROR = 2
    }
}
