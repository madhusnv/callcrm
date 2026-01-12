package com.educonsult.crm.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.educonsult.crm.data.local.db.dao.CallLogDao
import com.educonsult.crm.data.local.db.dao.CallRecordingDao
import com.educonsult.crm.data.local.db.dao.LeadDao
import com.educonsult.crm.data.local.db.dao.LeadNoteDao
import com.educonsult.crm.data.local.db.dao.LeadStatusDao
import com.educonsult.crm.data.local.db.dao.UserDao
import com.educonsult.crm.data.local.db.entity.CallLogEntity
import com.educonsult.crm.data.local.db.entity.CallRecordingEntity
import com.educonsult.crm.data.local.db.entity.LeadEntity
import com.educonsult.crm.data.local.db.entity.LeadNoteEntity
import com.educonsult.crm.data.local.db.entity.LeadStatusEntity
import com.educonsult.crm.data.local.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        LeadEntity::class,
        LeadStatusEntity::class,
        LeadNoteEntity::class,
        CallLogEntity::class,
        CallRecordingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun leadDao(): LeadDao
    abstract fun leadStatusDao(): LeadStatusDao
    abstract fun leadNoteDao(): LeadNoteDao
    abstract fun callLogDao(): CallLogDao
    abstract fun callRecordingDao(): CallRecordingDao
}
