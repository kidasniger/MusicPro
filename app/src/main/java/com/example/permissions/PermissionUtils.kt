package com.example.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionUtils {

    /**
     * Retourne la permission audio appropriée selon la version d'Android :
     * - Android 13+ (API 33+) : READ_MEDIA_AUDIO
     * - Android 12 et inférieur : READ_EXTERNAL_STORAGE
     */
    fun getAudioPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Retourne l'ensemble des permissions demandées au premier démarrage.
     */
    fun getAppPermissions(): List<String> {
        val list = mutableListOf<String>()
        list.add(getAudioPermission())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return list
    }

    /**
     * Vérifie si une permission spécifique est accordée.
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Vérifie si la permission audio essentielle est accordée.
     */
    fun isAudioPermissionGranted(context: Context): Boolean {
        return isPermissionGranted(context, getAudioPermission())
    }

    /**
     * Vérifie si la permission de notification est accordée.
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }

    /**
     * Ouvre les paramètres système de l'application pour que l'utilisateur puisse
     * accorder manuellement les permissions refusées.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
