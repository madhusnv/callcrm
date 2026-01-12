package com.educonsult.crm.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.educonsult.crm.data.local.db.entity.CallRecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordingDao {

    @Query("SELECT * FROM call_recordings ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CallRecordingEntity>>

    @Query("SELECT * FROM call_recordings WHERE id = :id")
    suspend fun getById(id: String): CallRecordingEntity?

    @Query("SELECT * FROM call_recordings WHERE callLogId = :callLogId")
    suspend fun getByCallLogId(callLogId: String): CallRecordingEntity?

    @Query("SELECT * FROM call_recordings WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getByStatus(status: String): List<CallRecordingEntity>

    @Query("SELECT * FROM call_recordings WHERE status IN ('pending', 'finding', 'compressing', 'uploading') ORDER BY createdAt ASC")
    suspend fun getPendingUploads(): List<CallRecordingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: CallRecordingEntity)

    @Update
    suspend fun update(recording: CallRecordingEntity)

    @Query("""
        UPDATE call_recordings 
        SET status = :status, uploadProgress = :progress, updatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updateStatus(id: String, status: String, progress: Int, updatedAt: Long)

    @Query("""
        UPDATE call_recordings 
        SET localFilePath = :path, originalFileSize = :size, status = 'pending', updatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updateLocalFile(id: String, path: String, size: Long, updatedAt: Long)

    @Query("""
        UPDATE call_recordings 
        SET storageKey = :key, storageUrl = :url, status = 'uploaded', uploadProgress = 100, updatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun markUploaded(id: String, key: String, url: String?, updatedAt: Long)

    @Query("""
        UPDATE call_recordings 
        SET status = 'failed', lastError = :error, retryCount = retryCount + 1, updatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun markFailed(id: String, error: String, updatedAt: Long)

    @Delete
    suspend fun delete(recording: CallRecordingEntity)

    @Query("DELETE FROM call_recordings WHERE callLogId = :callLogId")
    suspend fun deleteByCallLogId(callLogId: String)

    @Query("SELECT COUNT(*) FROM call_recordings WHERE status = 'pending'")
    suspend fun countPending(): Int
}
