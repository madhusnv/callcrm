package com.educonsult.crm.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Helper class for managing runtime permissions required for call tracking.
 */
object PermissionHelper {

    // Permissions needed for call monitoring
    val CALL_PERMISSIONS = buildList {
        add(Manifest.permission.READ_PHONE_STATE)
        add(Manifest.permission.READ_CALL_LOG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Permissions needed for reading recordings
    val STORAGE_PERMISSIONS = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // All permissions combined
    val ALL_PERMISSIONS = CALL_PERMISSIONS + STORAGE_PERMISSIONS

    fun hasCallPermissions(context: Context): Boolean {
        return CALL_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasStoragePermissions(context: Context): Boolean {
        return STORAGE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasAllPermissions(context: Context): Boolean {
        return hasCallPermissions(context) && hasStoragePermissions(context)
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun getMissingPermissions(context: Context): List<String> {
        return ALL_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Opens system settings for overlay permission.
     */
    fun requestOverlayPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        activity.startActivity(intent)
    }

    /**
     * Opens app settings page.
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    /**
     * Creates a permission launcher for use in ComponentActivity.
     */
    fun createPermissionLauncher(
        activity: ComponentActivity,
        onResult: (Map<String, Boolean>) -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            onResult(results)
        }
    }

    /**
     * Checks if permission is permanently denied (user selected "Don't ask again").
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }

    /**
     * Permission status for UI display.
     */
    data class PermissionStatus(
        val callTracking: Boolean,
        val storage: Boolean,
        val overlay: Boolean
    ) {
        val allGranted: Boolean get() = callTracking && storage && overlay
    }

    fun getPermissionStatus(context: Context): PermissionStatus {
        return PermissionStatus(
            callTracking = hasCallPermissions(context),
            storage = hasStoragePermissions(context),
            overlay = hasOverlayPermission(context)
        )
    }
}
