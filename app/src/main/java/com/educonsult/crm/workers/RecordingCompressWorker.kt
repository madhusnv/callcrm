package com.educonsult.crm.workers

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.educonsult.crm.data.local.db.dao.CallRecordingDao
import com.educonsult.crm.data.local.db.entity.CallRecordingEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Worker to compress recordings.
 * 
 * NOTE: FFmpeg-kit library is archived. For production use, either:
 * 1. Download FFmpeg-kit AAR manually from releases
 * 2. Use a community fork
 * 3. Build ffmpeg-kit locally
 * 
 * Currently uses pass-through mode (copies file without compression).
 */
@HiltWorker
class RecordingCompressWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val callRecordingDao: CallRecordingDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_RECORDING_ID = "recording_id"
        const val KEY_RECORDING_PATH = "recording_path"
        const val KEY_COMPRESSED_PATH = "compressed_path"
        
        // Target settings when FFmpeg is available
        private const val TARGET_BITRATE = 32000  // 32kbps
        private const val TARGET_SAMPLE_RATE = 22050
        private const val TARGET_CHANNELS = 1
    }

    override suspend fun doWork(): Result {
        val recordingId = inputData.getString(KEY_RECORDING_ID)
            ?: return Result.failure()
        val inputPath = inputData.getString(KEY_RECORDING_PATH)
            ?: return Result.failure()

        Timber.d("RecordingCompressWorker: Processing $inputPath")

        try {
            val recording = callRecordingDao.getById(recordingId)
                ?: return Result.failure()

            // Update status to compressing
            callRecordingDao.updateStatus(
                recordingId, 
                CallRecordingEntity.STATUS_COMPRESSING, 
                0, 
                System.currentTimeMillis()
            )

            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                Timber.e("Input file does not exist: $inputPath")
                return Result.failure()
            }

            // Create output directory in app cache
            val outputDir = File(context.cacheDir, "compressed_recordings")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            // Get audio duration from MediaExtractor
            val durationSeconds = getAudioDuration(inputPath)
            
            // Determine output format based on input
            val inputExtension = inputFile.extension.lowercase()
            val outputExtension = when (inputExtension) {
                "amr", "3gp", "m4a", "aac", "wav" -> "mp3"  // Would convert with FFmpeg
                else -> inputExtension
            }
            
            val outputFile = File(outputDir, "${recordingId}.$outputExtension")

            // TODO: When FFmpeg-kit is available, use:
            // val command = "-i \"$inputPath\" -vn -ar $TARGET_SAMPLE_RATE -ac $TARGET_CHANNELS -b:a ${TARGET_BITRATE}k -f mp3 -y \"${outputFile.absolutePath}\""
            // val session = FFmpegKit.execute(command)
            
            // For now, use pass-through (copy file as-is)
            val compressionResult = passThrough(inputFile, outputFile)
            
            if (compressionResult) {
                Timber.d("Processing successful: ${outputFile.absolutePath}")
                Timber.d("Original size: ${inputFile.length()}, Output: ${outputFile.length()}")

                // Update recording with output file info
                callRecordingDao.update(
                    recording.copy(
                        localFilePath = outputFile.absolutePath,
                        compressedFileSize = outputFile.length(),
                        duration = durationSeconds,
                        format = outputExtension,
                        status = CallRecordingEntity.STATUS_PENDING,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                return Result.success(
                    Data.Builder()
                        .putString(KEY_RECORDING_ID, recordingId)
                        .putString(KEY_COMPRESSED_PATH, outputFile.absolutePath)
                        .build()
                )
            } else {
                Timber.e("Compression failed")
                callRecordingDao.markFailed(
                    recordingId,
                    "Compression failed",
                    System.currentTimeMillis()
                )
                return Result.failure()
            }

        } catch (e: Exception) {
            Timber.e(e, "Error compressing recording")
            callRecordingDao.markFailed(
                recordingId,
                "Compression error: ${e.message}",
                System.currentTimeMillis()
            )
            return Result.retry()
        }
    }

    /**
     * Pass-through mode: copies file without compression.
     * Replace with FFmpeg compression when library is available.
     */
    private fun passThrough(inputFile: File, outputFile: File): Boolean {
        return try {
            FileInputStream(inputFile).use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error copying file")
            false
        }
    }

    /**
     * Get audio duration using MediaExtractor.
     */
    private fun getAudioDuration(filePath: String): Int {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(filePath)
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                
                if (mime.startsWith("audio/")) {
                    val durationUs = format.getLong(MediaFormat.KEY_DURATION)
                    return (durationUs / 1_000_000).toInt()  // Convert to seconds
                }
            }
            
            0
        } catch (e: Exception) {
            Timber.e(e, "Error getting audio duration")
            0
        } finally {
            extractor.release()
        }
    }
}
