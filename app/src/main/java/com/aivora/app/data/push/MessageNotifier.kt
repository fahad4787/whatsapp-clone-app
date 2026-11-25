package com.aivora.app.data.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aivora.app.R
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.ui.MainActivity

/**
 * Shows a heads-up for a new message when that chat is not open on screen.
 * Used by FCM (background/killed) and WebSocket (app open on home).
 */
object MessageNotifier {
    private const val CHANNEL_ID = "messages"
    private const val CALLS_CHANNEL_ID = "calls"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val prefs = com.aivora.app.data.local.NotificationPrefs.load(context)
        val soundUri = prefs.msgSoundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        val want = soundUri?.toString() ?: ""
        val have = existing?.sound?.toString().orEmpty()
        // Channel sound is immutable — recreate when the user picks a new tone (Wave 47).
        if (existing != null && have != want) {
            nm.deleteNotificationChannel(CHANNEL_ID)
        }
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "New chat messages"
                    enableVibration(true)
                    if (soundUri != null) {
                        setSound(
                            soundUri,
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build(),
                        )
                    }
                },
            )
        }
        ensureCallsChannel(context)
    }

    /** Call after the user changes or resets the message notification tone. */
    fun recreateMessagesChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java)
                ?.deleteNotificationChannel(CHANNEL_ID)
        }
        ensureChannel(context)
    }

    fun ensureCallsChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val prefs = com.aivora.app.data.local.NotificationPrefs.load(context)
        val soundUri = prefs.callSoundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
        val existing = nm.getNotificationChannel(CALLS_CHANNEL_ID)
        val want = soundUri?.toString() ?: ""
        val have = existing?.sound?.toString().orEmpty()
        if (existing != null && have != want) {
            nm.deleteNotificationChannel(CALLS_CHANNEL_ID)
        }
        if (nm.getNotificationChannel(CALLS_CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CALLS_CHANNEL_ID, "Calls", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Incoming voice and video calls"
                    enableVibration(true)
                    if (soundUri != null) {
                        setSound(
                            soundUri,
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build(),
                        )
                    }
                },
            )
        }
    }

    /** Call after the user changes or resets the call ringtone (Wave 53). */
    fun recreateCallsChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java)
                ?.deleteNotificationChannel(CALLS_CHANNEL_ID)
        }
        ensureCallsChannel(context)
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun show(
        context: Context,
        title: String,
        body: String,
        conversationId: String?,
        isReaction: Boolean = false,
        isGroup: Boolean = false,
    ) {
        if (conversationId != null && InMemoryChatStore.isViewing(conversationId)) return
        if (!canPost(context)) return
        val prefs = com.aivora.app.data.local.NotificationPrefs.load(context)
        if (!prefs.enabled) return
        if (isReaction && !prefs.reactions) return
        ensureChannel(context)

        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!conversationId.isNullOrBlank()) {
                putExtra("conversation_id", conversationId)
            }
        }
        val pending = PendingIntent.getActivity(
            context,
            (conversationId ?: body).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val text = if (prefs.showPreview) body else "New message"
        val vibrateKey = if (isGroup) prefs.groupVibrate else prefs.msgVibrate
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_aivora)
            .setContentTitle(title.ifBlank { "Aivora" })
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(
                if (prefs.highPriority) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT,
            )
            .setColor(0xFF00BE3C.toInt())
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val customSound = prefs.msgSoundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
        when {
            !prefs.conversationTones && vibrateKey == com.aivora.app.data.local.NotificationPrefs.VIBRATE_OFF -> {
                builder.setSilent(true)
            }
            !prefs.conversationTones -> {
                builder.setDefaults(0)
                applyVibrate(builder, vibrateKey)
            }
            vibrateKey == com.aivora.app.data.local.NotificationPrefs.VIBRATE_OFF -> {
                applySound(builder, customSound)
            }
            vibrateKey == com.aivora.app.data.local.NotificationPrefs.VIBRATE_LONG -> {
                applySound(builder, customSound)
                builder.setVibrate(longArrayOf(0, 400, 200, 400))
            }
            else -> {
                applySound(builder, customSound)
                applyVibrate(builder, vibrateKey)
            }
        }

        nm.notify((conversationId ?: body).hashCode() and 0x7fffffff, builder.build())
    }

    private fun applySound(builder: NotificationCompat.Builder, custom: Uri?) {
        if (custom != null) {
            builder.setSound(custom)
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }
    }

    private fun applyVibrate(builder: NotificationCompat.Builder, key: String) {
        when (key) {
            com.aivora.app.data.local.NotificationPrefs.VIBRATE_OFF -> { /* silent vibrate */ }
            com.aivora.app.data.local.NotificationPrefs.VIBRATE_LONG ->
                builder.setVibrate(longArrayOf(0, 400, 200, 400))
            else -> builder.setVibrate(longArrayOf(0, 250, 100, 250))
        }
    }

    fun showCall(
        context: Context,
        title: String,
        body: String,
        conversationId: String?,
        callId: String,
    ) {
        if (!canPost(context)) return
        val prefs = com.aivora.app.data.local.NotificationPrefs.load(context)
        if (!prefs.enabled) return
        ensureCallsChannel(context)
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("incoming_call_id", callId)
            if (!conversationId.isNullOrBlank()) putExtra("conversation_id", conversationId)
        }
        val pending = PendingIntent.getActivity(
            context,
            callId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, CALLS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_aivora)
            .setContentTitle(title.ifBlank { "Incoming call" })
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
        val customSound = prefs.callSoundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
        when (prefs.callVibrate) {
            com.aivora.app.data.local.NotificationPrefs.VIBRATE_OFF ->
                applySound(builder, customSound)
            com.aivora.app.data.local.NotificationPrefs.VIBRATE_LONG -> {
                applySound(builder, customSound)
                builder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            }
            else -> {
                applySound(builder, customSound)
                applyVibrate(builder, prefs.callVibrate)
            }
        }
        nm.notify(callId.hashCode() and 0x7fffffff, builder.build())
    }

    /** Removes the incoming-call notification once that call is over. */
    fun cancelCall(context: Context, callId: String) {
        context.getSystemService(NotificationManager::class.java)
            ?.cancel(callId.hashCode() and 0x7fffffff)
    }
}
