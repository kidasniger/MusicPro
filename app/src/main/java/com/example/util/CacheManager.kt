package com.example.util

import android.content.Context
import java.io.File
import java.text.DecimalFormat

/**
 * Gestionnaire du cache local de l'application (images Coil, chunks audio temporaires, etc.).
 */
object CacheManager {

    fun getCacheSizeBytes(context: Context): Long {
        var size = 0L
        try {
            context.cacheDir?.let { size += getFolderSize(it) }
            context.externalCacheDir?.let { size += getFolderSize(it) }
        } catch (_: Exception) {}
        return size
    }

    fun getFormattedCacheSize(context: Context): String {
        val bytes = getCacheSizeBytes(context)
        return formatFileSize(bytes)
    }

    fun clearCache(context: Context): Boolean {
        var success = true
        try {
            context.cacheDir?.let { success = deleteDir(it) && success }
            context.externalCacheDir?.let { success = deleteDir(it) && success }
        } catch (_: Exception) {
            success = false
        }
        return success
    }

    private fun getFolderSize(file: File): Long {
        var size = 0L
        val files = file.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) {
                getFolderSize(f)
            } else {
                f.length()
            }
        }
        return size
    }

    private fun deleteDir(dir: File): Boolean {
        val children = dir.listFiles() ?: return true
        var success = true
        for (child in children) {
            val deleted = if (child.isDirectory) {
                deleteDir(child)
            } else {
                child.delete()
            }
            if (!deleted) success = false
        }
        return success
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 Ko"
        val units = arrayOf("o", "Ko", "Mo", "Go")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
    }
}
