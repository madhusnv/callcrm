package com.educonsult.crm.di

import android.content.Context
import androidx.room.Room
import com.educonsult.crm.data.local.db.AppDatabase
import com.educonsult.crm.data.local.db.dao.CallLogDao
import com.educonsult.crm.data.local.db.dao.CallRecordingDao
import com.educonsult.crm.data.local.db.dao.LeadDao
import com.educonsult.crm.data.local.db.dao.LeadNoteDao
import com.educonsult.crm.data.local.db.dao.LeadStatusDao
import com.educonsult.crm.data.local.db.dao.UserDao
import com.educonsult.crm.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideLeadDao(appDatabase: AppDatabase): LeadDao {
        return appDatabase.leadDao()
    }

    @Provides
    @Singleton
    fun provideLeadStatusDao(appDatabase: AppDatabase): LeadStatusDao {
        return appDatabase.leadStatusDao()
    }

    @Provides
    @Singleton
    fun provideLeadNoteDao(appDatabase: AppDatabase): LeadNoteDao {
        return appDatabase.leadNoteDao()
    }

    @Provides
    @Singleton
    fun provideCallLogDao(appDatabase: AppDatabase): CallLogDao {
        return appDatabase.callLogDao()
    }

    @Provides
    @Singleton
    fun provideCallRecordingDao(appDatabase: AppDatabase): CallRecordingDao {
        return appDatabase.callRecordingDao()
    }
}
