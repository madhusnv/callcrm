package com.educonsult.crm.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.educonsult.crm.data.local.db.dao.CallRecordingDao
import com.educonsult.crm.data.local.db.entity.CallRecordingEntity
import com.educonsult.crm.domain.repository.CallRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Worker to upload compressed recordings to S3 via presigned URL.
 */
@HiltWorker
class RecordingUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val callRepository: CallRepository,
    private val callRecordingDao: CallRecordingDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_RECORDING_ID = "recording_id"
        const val KEY_COMPRESSED_PATH = "compressed_path"
        const val KEY_CALL_LOG_ID = "call_log_id"
        
        private const val UPLOAD_TIMEOUT_SECONDS = 120L
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        val recordingId = inputData.getString(KEY_RECORDING_ID)
            ?: return Result.failure()
        val compressedPath = inputData.getString(KEY_COMPRESSED_PATH)
            ?: return Result.failure()
        val callLogId = inputData.getString(KEY_CALL_LOG_ID)
            ?: return Result.failure()

        Timber.d("RecordingUploadWorker: Uploading $recordingId")

        try {
            val recording = callRecordingDao.getById(recordingId)
                ?: return Result.failure()

            val file = File(compressedPath)
            if (!file.exists()) {
                Timber.e("Compressed file does not exist: $compressedPath")
                return Result.failure()
            }

            // Update status to uploading
            callRecordingDao.updateStatus(
                recordingId,
                CallRecordingEntity.STATUS_UPLOADING,
                0,
                System.currentTimeMillis()
            )

            // Get presigned upload URL from backend
            val uploadUrlResult = callRepository.getUploadUrl(callLogId)
            if (uploadUrlResult.isFailure) {
                Timber.e("Failed to get upload URL: ${uploadUrlResult.exceptionOrNull()}")
                callRecordingDao.markFailed(
                    recordingId,
                    "Failed to get upload URL",
                    System.currentTimeMillis()
                )
                return Result.retry()
            }

            val uploadResponse = uploadUrlResult.getOrThrow()
            val presignedUrl = uploadResponse.uploadUrl
            val storageKey = uploadResponse.storageKey

            // Upload file to S3 using presigned PUT URL
            val uploadSuccess = uploadToS3(file, presignedUrl)
            
            if (uploadSuccess) {
                Timber.d("Upload successful for $recordingId")

                // Confirm upload with backend
                val confirmResult = callRepository.confirmUpload(
                    recordingId = uploadResponse.recordingId,
                    fileSize = file.length(),
                    duration = recording.duration ?: 0
                )

                if (confirmResult.isSuccess) {
                    // Mark as uploaded in local DB
                    callRecordingDao.markUploaded(
                        recordingId,
                        storageKey,
                        null,
                        System.currentTimeMillis()
                    )

                    // Clean up local compressed file
                    file.delete()

                    return Result.success(
                        Data.Builder()
                            .putString(KEY_RECORDING_ID, recordingId)
                            .putBoolean("upload_success", true)
                            .build()
                    )
                } else {
                    Timber.e("Failed to confirm upload")
                    callRecordingDao.markFailed(
                        recordingId,
                        "Failed to confirm upload",
                        System.currentTimeMillis()
                    )
                    return Result.retry()
                }
            } else {
                Timber.e("S3 upload failed")
                callRecordingDao.markFailed(
                    recordingId,
                    "S3 upload failed",
                    System.currentTimeMillis()
                )
                return Result.retry()
            }

        } catch (e: Exception) {
            Timber.e(e, "Error uploading recording")
            callRecordingDao.markFailed(
                recordingId,
                "Upload error: ${e.message}",
                System.currentTimeMillis()
            )
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun uploadToS3(file: File, presignedUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = file.asRequestBody("audio/mpeg".toMediaType())
                
                val request = Request.Builder()
                    .url(presignedUrl)
                    .put(requestBody)
                    .header("Content-Type", "audio/mpeg")
                    .build()

                val response = httpClient.newCall(request).execute()
                
                val success = response.isSuccessful
                Timber.d("S3 upload response: ${response.code}")
                
                response.close()
                success
            } catch (e: Exception) {
                Timber.e(e, "S3 upload exception")
                false
            }
        }
    }
}
