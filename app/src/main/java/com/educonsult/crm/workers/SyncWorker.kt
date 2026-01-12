package com.educonsult.crm.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.educonsult.crm.domain.repository.LeadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker for syncing leads with the server.
 * Uses WorkManager for reliable execution even when app is in background.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val leadRepository: LeadRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "lead_sync_worker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting lead sync...")
        
        return try {
            val result = leadRepository.syncLeads()
            
            result.fold(
                onSuccess = { count ->
                    Log.d(TAG, "Sync completed: $count items synced")
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Sync failed: ${error.message}")
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
