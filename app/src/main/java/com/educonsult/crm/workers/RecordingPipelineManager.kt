package com.educonsult.crm.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the recording pipeline: Find → Compress → Upload
 */
@Singleton
class RecordingPipelineManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Starts the recording pipeline for a call log.
     * Chains: FindWorker → CompressWorker → UploadWorker
     */
    fun startPipeline(callLogId: String) {
        Timber.d("Starting recording pipeline for call $callLogId")

        val inputData = Data.Builder()
            .putString(RecordingFindWorker.KEY_CALL_LOG_ID, callLogId)
            .build()

        // Constraints for upload - require network
        val uploadConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Find worker
        val findWork = OneTimeWorkRequestBuilder<RecordingFindWorker>()
            .setInputData(inputData)
            .addTag("recording_$callLogId")
            .build()

        // Compress worker
        val compressWork = OneTimeWorkRequestBuilder<RecordingCompressWorker>()
            .addTag("recording_$callLogId")
            .build()

        // Upload worker
        val uploadWork = OneTimeWorkRequestBuilder<RecordingUploadWorker>()
            .setConstraints(uploadConstraints)
            .setInputData(
                Data.Builder()
                    .putString(RecordingUploadWorker.KEY_CALL_LOG_ID, callLogId)
                    .build()
            )
            .addTag("recording_$callLogId")
            .build()

        // Chain: Find → Compress → Upload
        workManager
            .beginUniqueWork(
                "recording_pipeline_$callLogId",
                ExistingWorkPolicy.REPLACE,
                findWork
            )
            .then(compressWork)
            .then(uploadWork)
            .enqueue()

        Timber.d("Recording pipeline enqueued for $callLogId")
    }

    /**
     * Starts pipeline for all calls that have pending recordings.
     */
    fun processPendingRecordings(pendingCallLogIds: List<String>) {
        pendingCallLogIds.forEach { callLogId ->
            startPipeline(callLogId)
        }
    }

    /**
     * Cancels the pipeline for a specific call log.
     */
    fun cancelPipeline(callLogId: String) {
        workManager.cancelUniqueWork("recording_pipeline_$callLogId")
    }

    /**
     * Cancels all recording pipelines.
     */
    fun cancelAllPipelines() {
        workManager.cancelAllWorkByTag("recording")
    }
}
