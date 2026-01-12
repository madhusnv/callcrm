package com.educonsult.crm.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.educonsult.crm.data.local.db.entity.LeadNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadNoteDao {

    @Query("SELECT * FROM lead_notes WHERE leadId = :leadId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getByLeadId(leadId: String): Flow<List<LeadNoteEntity>>

    @Query("SELECT * FROM lead_notes WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): LeadNoteEntity?

    @Query("SELECT * FROM lead_notes WHERE syncStatus != 0")
    suspend fun getPendingSync(): List<LeadNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: LeadNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<LeadNoteEntity>)

    @Update
    suspend fun update(note: LeadNoteEntity)

    @Query("UPDATE lead_notes SET syncStatus = :status, lastSyncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int, syncedAt: Long)

    @Delete
    suspend fun delete(note: LeadNoteEntity)

    @Query("UPDATE lead_notes SET deletedAt = :deletedAt, syncStatus = 3, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("SELECT COUNT(*) FROM lead_notes WHERE leadId = :leadId AND deletedAt IS NULL")
    suspend fun countByLeadId(leadId: String): Int

    @Query("DELETE FROM lead_notes WHERE leadId = :leadId")
    suspend fun deleteByLeadId(leadId: String)
}
