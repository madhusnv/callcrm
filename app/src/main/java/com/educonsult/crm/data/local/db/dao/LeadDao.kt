package com.educonsult.crm.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.educonsult.crm.data.local.db.entity.LeadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {

    @Query("SELECT * FROM leads WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): LeadEntity?

    @Query("SELECT * FROM leads WHERE phone = :phone AND deletedAt IS NULL LIMIT 1")
    suspend fun getByPhone(phone: String): LeadEntity?

    @Query("SELECT * FROM leads WHERE statusId = :statusId AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getByStatus(statusId: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE syncStatus != 0")
    suspend fun getPendingSync(): List<LeadEntity>

    @Query("SELECT * FROM leads WHERE syncStatus = :status AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getBySyncStatus(status: Int): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE nextFollowUpDate <= :date AND deletedAt IS NULL ORDER BY nextFollowUpDate ASC")
    fun getFollowUpsDue(date: Long): Flow<List<LeadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lead: LeadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(leads: List<LeadEntity>)

    @Update
    suspend fun update(lead: LeadEntity)

    @Query("UPDATE leads SET syncStatus = :status, lastSyncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int, syncedAt: Long)

    @Delete
    suspend fun delete(lead: LeadEntity)

    @Query("UPDATE leads SET deletedAt = :deletedAt, syncStatus = 3, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("SELECT COUNT(*) FROM leads WHERE deletedAt IS NULL")
    suspend fun count(): Int

    @Query("""
        SELECT * FROM leads 
        WHERE deletedAt IS NULL 
        AND (
            firstName LIKE '%' || :query || '%' 
            OR lastName LIKE '%' || :query || '%' 
            OR phone LIKE '%' || :query || '%' 
            OR email LIKE '%' || :query || '%'
            OR studentName LIKE '%' || :query || '%'
        )
        ORDER BY updatedAt DESC
    """)
    fun searchLeads(query: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE assignedTo = :userId AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getByAssignee(userId: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE branchId = :branchId AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getByBranch(branchId: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE priority = :priority AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getByPriority(priority: String): Flow<List<LeadEntity>>

    @Query("UPDATE leads SET totalCalls = totalCalls + 1, lastContactDate = :contactDate, updatedAt = :contactDate WHERE id = :id")
    suspend fun incrementCallCount(id: String, contactDate: Long)

    @Query("UPDATE leads SET totalNotes = totalNotes + 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun incrementNoteCount(id: String, updatedAt: Long)
}
