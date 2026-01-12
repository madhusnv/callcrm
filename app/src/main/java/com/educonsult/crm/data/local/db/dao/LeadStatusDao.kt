package com.educonsult.crm.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.educonsult.crm.data.local.db.entity.LeadStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadStatusDao {

    @Query("SELECT * FROM lead_statuses WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<LeadStatusEntity>>

    @Query("SELECT * FROM lead_statuses WHERE id = :id")
    suspend fun getById(id: String): LeadStatusEntity?

    @Query("SELECT * FROM lead_statuses WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): LeadStatusEntity?

    @Query("SELECT * FROM lead_statuses WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): LeadStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(status: LeadStatusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<LeadStatusEntity>)

    @Update
    suspend fun update(status: LeadStatusEntity)

    @Delete
    suspend fun delete(status: LeadStatusEntity)

    @Query("DELETE FROM lead_statuses")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lead_statuses WHERE isActive = 1")
    suspend fun count(): Int
}
