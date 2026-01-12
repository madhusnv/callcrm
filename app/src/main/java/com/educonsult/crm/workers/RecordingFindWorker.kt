package com.educonsult.crm.workers

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.educonsult.crm.data.local.db.dao.CallLogDao
import com.educonsult.crm.data.local.db.dao.CallRecordingDao
import com.educonsult.crm.data.local.db.entity.CallRecordingEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.io.File
import java.util.UUID

/**
 * Worker to find native call recordings on the device.
 * Scans common recording locations and matches to call logs.
 */
@HiltWorker
class RecordingFindWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val callLogDao: CallLogDao,
    private val callRecordingDao: CallRecordingDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_CALL_LOG_ID = "call_log_id"
        const val KEY_RECORDING_ID = "recording_id"
        const val KEY_RECORDING_PATH = "recording_path"
        
        // Common call recording directories
        private val RECORDING_PATHS = listOf(
            "MIUI/sound_recorder/call_rec",
            "Recordings/Call",
            "Call",
            "PhoneRecord",
            "Record/Call",
            "Recorder",
            "VoiceRecorder",
            "Samsung/Voice Recorder",
            "Sounds/CallRecord"
        )
    }

    override suspend fun doWork(): Result {
        val callLogId = inputData.getString(KEY_CALL_LOG_ID)
            ?: return Result.failure()

        Timber.d("RecordingFindWorker: Looking for recording for call $callLogId")

        try {
            val callLog = callLogDao.getById(callLogId)
                ?: return Result.failure()

            // Skip if recording already exists
            val existingRecording = callRecordingDao.getByCallLogId(callLogId)
            if (existingRecording?.localFilePath != null) {
                Timber.d("Recording already found at ${existingRecording.localFilePath}")
                return Result.success(
                    Data.Builder()
                        .putString(KEY_RECORDING_ID, existingRecording.id)
                        .putString(KEY_RECORDING_PATH, existingRecording.localFilePath)
                        .build()
                )
            }

            // Search for recording file
            val recordingFile = findRecordingFile(
                phoneNumber = callLog.phoneNumber,
                callTime = callLog.callAt,
                duration = callLog.duration
            )

            if (recordingFile != null && recordingFile.exists()) {
                Timber.d("Found recording at ${recordingFile.absolutePath}")

                // Create or update recording entity
                val recordingId = existingRecording?.id ?: UUID.randomUUID().toString()
                val recording = CallRecordingEntity(
                    id = recordingId,
                    callLogId = callLogId,
                    localFilePath = recordingFile.absolutePath,
                    originalFileName = recordingFile.name,
                    originalFileSize = recordingFile.length(),
                    status = CallRecordingEntity.STATUS_PENDING
                )
                callRecordingDao.insert(recording)

                return Result.success(
                    Data.Builder()
                        .putString(KEY_RECORDING_ID, recordingId)
                        .putString(KEY_RECORDING_PATH, recordingFile.absolutePath)
                        .build()
                )
            }

            // Also search via MediaStore
            val mediaFile = findRecordingViaMediaStore(callLog.callAt, callLog.duration)
            if (mediaFile != null && mediaFile.exists()) {
                Timber.d("Found recording via MediaStore at ${mediaFile.absolutePath}")

                val recordingId = existingRecording?.id ?: UUID.randomUUID().toString()
                val recording = CallRecordingEntity(
                    id = recordingId,
                    callLogId = callLogId,
                    localFilePath = mediaFile.absolutePath,
                    originalFileName = mediaFile.name,
                    originalFileSize = mediaFile.length(),
                    status = CallRecordingEntity.STATUS_PENDING
                )
                callRecordingDao.insert(recording)

                return Result.success(
                    Data.Builder()
                        .putString(KEY_RECORDING_ID, recordingId)
                        .putString(KEY_RECORDING_PATH, mediaFile.absolutePath)
                        .build()
                )
            }

            Timber.w("No recording found for call $callLogId")
            return Result.failure()

        } catch (e: Exception) {
            Timber.e(e, "Error finding recording")
            return Result.retry()
        }
    }

    private fun findRecordingFile(
        phoneNumber: String,
        callTime: Long,
        duration: Int
    ): File? {
        val externalStorage = Environment.getExternalStorageDirectory()
        val normalizedPhone = phoneNumber.takeLast(10)
        
        // Calculate time window (recording might start slightly before/after call)
        val startWindow = callTime - 30_000 // 30 seconds before
        val endWindow = callTime + (duration * 1000L) + 60_000 // call duration + 1 minute after

        for (path in RECORDING_PATHS) {
            val dir = File(externalStorage, path)
            if (!dir.exists() || !dir.isDirectory) continue

            val matchingFile = dir.listFiles()?.firstOrNull { file ->
                if (!file.isFile) return@firstOrNull false
                
                // Check if file name contains phone number
                val containsPhone = file.name.contains(normalizedPhone)
                
                // Check if file was modified within the time window
                val modifiedInWindow = file.lastModified() in startWindow..endWindow
                
                // Check file extension
                val isAudioFile = file.extension.lowercase() in listOf("mp3", "m4a", "amr", "ogg", "3gp", "wav", "aac")
                
                (containsPhone || modifiedInWindow) && isAudioFile
            }

            if (matchingFile != null) {
                return matchingFile
            }
        }

        return null
    }

    private fun findRecordingViaMediaStore(callTime: Long, duration: Int): File? {
        val contentResolver: ContentResolver = context.contentResolver
        
        val startWindow = callTime - 30_000
        val endWindow = callTime + (duration * 1000L) + 60_000

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.DATE_ADDED} >= ? AND ${MediaStore.Audio.Media.DATE_ADDED} <= ?"
        val selectionArgs = arrayOf(
            (startWindow / 1000).toString(),
            (endWindow / 1000).toString()
        )

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val path = it.getString(dataIndex)
                return File(path)
            }
        }

        return null
    }
}
