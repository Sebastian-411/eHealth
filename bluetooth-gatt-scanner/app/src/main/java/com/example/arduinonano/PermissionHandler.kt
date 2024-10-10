package com.example.arduinonano

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Handles Bluetooth and location permissions for the app.
 *
 * @param activity The activity where permissions are being requested.
 * @param onPermissionsGranted Callback invoked when all permissions are granted or denied.
 */
class PermissionHandler(
    private val activity: AppCompatActivity,
    private val onPermissionsGranted: (Boolean) -> Unit
) {

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        onPermissionsGranted(allGranted)
    }

    /**
     * Checks and requests necessary Bluetooth permissions based on the Android version.
     */
    fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        requestPermissionLauncher.launch(permissions)
    }
}
