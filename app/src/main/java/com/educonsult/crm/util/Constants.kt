package com.educonsult.crm.util

object Constants {
    const val DATASTORE_NAME = "edu_consult_preferences"
    const val DATABASE_NAME = "edu_consult_database"
    
    const val API_TIMEOUT_SECONDS = 30L
    const val API_CONNECT_TIMEOUT_SECONDS = 15L
    const val API_READ_TIMEOUT_SECONDS = 30L
    const val API_WRITE_TIMEOUT_SECONDS = 30L
    
    const val MAX_RETRY_COUNT = 3
    const val RETRY_DELAY_MS = 1000L
    
    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40
    
    const val SYNC_INTERVAL_MINUTES = 15L
    const val REMINDER_CHECK_INTERVAL_MINUTES = 5L
    
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val TIME_FORMAT_DISPLAY = "hh:mm a"
    const val DATETIME_FORMAT_DISPLAY = "dd MMM yyyy, hh:mm a"
    const val DATETIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_NAME_LENGTH = 100
    const val MAX_NOTES_LENGTH = 1000
    
    const val PHONE_NUMBER_MIN_LENGTH = 10
    const val PHONE_NUMBER_MAX_LENGTH = 15
    
    object WorkerTags {
        const val SYNC_WORKER = "sync_worker"
        const val REMINDER_WORKER = "reminder_worker"
        const val UPLOAD_WORKER = "upload_worker"
    }
    
    object NotificationChannels {
        const val REMINDERS_CHANNEL_ID = "reminders_channel"
        const val SYNC_CHANNEL_ID = "sync_channel"
        const val CALLS_CHANNEL_ID = "calls_channel"
    }
}
