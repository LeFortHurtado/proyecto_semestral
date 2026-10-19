package com.example.dsy1105_006d_lc7.ui.qr

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Utilidad para comprobar el estado del permiso de cámara en tiempo de ejecución.
 * Basado en la implementación de camara2026.
 */
object CameraPermissionHelper {
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
