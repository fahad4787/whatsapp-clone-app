package com.aivora.app.data

import android.content.Context
import java.io.File

/** Deletes disposable cache dirs. Keeps filesDir/backups and the session. */
object CacheCleaner {
    private val dirs = listOf(
        "coil_images",
        "export",
        "backup",
        "backup_open",
        "transcripts",
        "account-info",
        "voice",
        "capture",
    )

    fun clear(context: Context): Long {
        var freed = 0L
        for (name in dirs) {
            val dir = File(context.cacheDir, name)
            freed += sizeOf(dir)
            dir.deleteRecursively()
        }
        return freed
    }

    private fun sizeOf(file: File): Long {
        if (!file.exists()) return 0
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf { sizeOf(it) } ?: 0
    }
}
