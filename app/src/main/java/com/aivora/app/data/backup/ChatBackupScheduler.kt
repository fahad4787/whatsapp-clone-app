package com.aivora.app.data.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aivora.app.data.local.BackupPrefs
import java.util.concurrent.TimeUnit

/** Schedules / cancels unique periodic chat backup (Wave 48). */
object ChatBackupScheduler {
    private const val UNIQUE = "aivora_chat_backup"

    fun apply(context: Context) {
        val app = context.applicationContext
        val freq = BackupPrefs.frequency(app)
        val wm = WorkManager.getInstance(app)
        if (freq == BackupPrefs.FREQ_OFF) {
            wm.cancelUniqueWork(UNIQUE)
            return
        }
        val intervalDays = when (freq) {
            BackupPrefs.FREQ_WEEKLY -> 7L
            BackupPrefs.FREQ_MONTHLY -> 30L
            else -> 1L // Daily
        }
        val network = if (BackupPrefs.wifiOnly(app)) {
            NetworkType.UNMETERED
        } else {
            NetworkType.CONNECTED
        }
        val request = PeriodicWorkRequestBuilder<ChatBackupWorker>(intervalDays, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(network)
                    .build(),
            )
            .build()
        wm.enqueueUniquePeriodicWork(
            UNIQUE,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
