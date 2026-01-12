package com.educonsult.crm.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.educonsult.crm.data.local.db.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {

    @Query("SELECT * FROM call_logs ORDER BY callAt DESC")
    fun getAll(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE id = :id")
    suspend fun getById(id: String): CallLogEntity?

    @Query("SELECT * FROM call_logs WHERE leadId = :leadId ORDER BY callAt DESC")
    fun getByLeadId(leadId: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE phoneNumber = :phone ORDER BY callAt DESC LIMIT 1")
    suspend fun getLatestByPhone(phone: String): CallLogEntity?

    @Query("SELECT * FROM call_logs WHERE deviceCallId = :deviceCallId LIMIT 1")
    suspend fun getByDeviceCallId(deviceCallId: String): CallLogEntity?

    @Query("SELECT * FROM call_logs WHERE syncStatus = 0 ORDER BY callAt DESC")
    suspend fun getPendingSync(): List<CallLogEntity>

    @Query("SELECT COUNT(*) FROM call_logs WHERE syncStatus = 0")
    suspend fun getPendingSyncCount(): Int

    @Query("SELECT * FROM call_logs WHERE callAt >= :startTime AND callAt <= :endTime ORDER BY callAt DESC")
    fun getByDateRange(startTime: Long, endTime: Long): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE callType = :callType ORDER BY callAt DESC")
    fun getByType(callType: String): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callLog: CallLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(callLogs: List<CallLogEntity>)

    @Update
    suspend fun update(callLog: CallLogEntity)

    @Query("UPDATE call_logs SET syncStatus = :status, lastSyncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int, syncedAt: Long)

    @Query("UPDATE call_logs SET leadId = :leadId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLeadId(id: String, leadId: String?, updatedAt: Long)

    @Query("UPDATE call_logs SET notes = :notes, updatedAt = :updatedAt, syncStatus = 0 WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String?, updatedAt: Long)

    @Delete
    suspend fun delete(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM call_logs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM call_logs WHERE leadId = :leadId")
    suspend fun countByLead(leadId: String): Int

    // Get today's calls
    @Query("""
        SELECT * FROM call_logs 
        WHERE callAt >= :todayStart 
        ORDER BY callAt DESC
    """)
    fun getTodaysCalls(todayStart: Long): Flow<List<CallLogEntity>>

    // Get calls without associated lead
    @Query("SELECT * FROM call_logs WHERE leadId IS NULL ORDER BY callAt DESC")
    fun getUnmatchedCalls(): Flow<List<CallLogEntity>>
}
