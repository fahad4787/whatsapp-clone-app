package com.aivora.app.data.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aivora.app.R
import com.aivora.app.data.ChatBackup
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.BackupPrefs
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.push.MessageNotifier

/** Periodic chat backup zip → filesDir/backups (Wave 48). */
class ChatBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (SessionStore.accessToken(applicationContext).isNullOrBlank()) {
            return Result.success()
        }
        if (BackupPrefs.frequency(applicationContext) == BackupPrefs.FREQ_OFF) {
            return Result.success()
        }
        if (BackupPrefs.wifiOnly(applicationContext) && !ChatBackup.onWifi(applicationContext)) {
            return Result.retry()
        }
        val ok = ChatBackup.downloadAuto(applicationContext, NetworkModule.api())
        if (ok) notifySaved(applicationContext)
        return if (ok) Result.success() else Result.retry()
    }

    private fun notifySaved(context: Context) {
        if (!MessageNotifier.canPost(context)) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Backups",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply { description = "Automatic chat backup status" },
            )
        }
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_aivora)
            .setContentTitle("Chat backup saved")
            .setContentText("Stored on this device · ${BackupPrefs.formatSize(context)}")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        nm.notify(NOTIF_ID, n)
    }

    companion object {
        private const val CHANNEL_ID = "backups"
        private const val NOTIF_ID = 0xA17_B00
    }
}
