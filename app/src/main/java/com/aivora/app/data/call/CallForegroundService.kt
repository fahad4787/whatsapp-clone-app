package com.aivora.app.data.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.aivora.app.R
import com.aivora.app.ui.MainActivity
import com.aivora.app.ui.navigation.DeepLinks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Runs while a call is connected. Without a microphone foreground service Android mutes
 * the call as soon as the app leaves the screen. Also shows the "ongoing call" notification.
 */
class CallForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var updates: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HANG_UP) {
            CallActions.hangUp()
            stopSelf()
            return START_NOT_STICKY
        }
        if (ActiveCallSession.session.value == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        val started = runCatching {
            val type = if (Build.VERSION.SDK_INT >= 30) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            }
            ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
        }.onFailure { CallLog.w("call service could not start", it) }.isSuccess
        if (!started) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (updates == null) {
            updates = scope.launch {
                combine(ActiveCallSession.session, ActiveCallSession.liveSince) { s, _ -> s }
                    .collect { session ->
                        if (session != null) {
                            runCatching {
                                getSystemService(NotificationManager::class.java)
                                    ?.notify(NOTIFICATION_ID, buildNotification())
                            }
                        }
                    }
            }
        }
        return START_NOT_STICKY
    }

    /** Swiping the app away ends the call, so the other side is not left talking to nobody. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        CallActions.hangUp()
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Ongoing call", NotificationManager.IMPORTANCE_LOW),
            )
        }
        val session = ActiveCallSession.session.value
        val liveSince = ActiveCallSession.liveSince.value
        val kind = if (session?.media == "video") "video call" else "voice call"
        // Tapping the notification goes straight back to the call screen.
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .setAction(DeepLinks.ACTION_OPEN_CALL)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val hangUp = PendingIntent.getService(
            this,
            1,
            Intent(this, CallForegroundService::class.java).setAction(ACTION_HANG_UP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_aivora)
            .setContentTitle(session?.peerName?.ifBlank { null } ?: "Aivora call")
            .setContentText(if (liveSince == null) "Calling…" else "Ongoing $kind")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setColor(0xFF00BE3C.toInt())
            .setContentIntent(open)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(R.drawable.ic_call_decline, "Hang up", hangUp)
        if (liveSince != null) {
            builder
                .setUsesChronometer(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis() - (SystemClock.elapsedRealtime() - liveSince))
        } else {
            builder.setShowWhen(false)
        }
        return builder.build()
    }

    companion object {
        private const val CHANNEL_ID = "ongoing_call"
        private const val NOTIFICATION_ID = 0x0A17_0002
        private const val ACTION_HANG_UP = "com.aivora.app.action.HANG_UP"

        /** A plain start: if Android refuses to promote it, the app does not crash. */
        fun start(context: Context) {
            runCatching {
                context.startService(Intent(context, CallForegroundService::class.java))
            }.onFailure { CallLog.w("call service start failed", it) }
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, CallForegroundService::class.java))
            }
        }
    }
}
