package com.aivora.app.data.push

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
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
    private const val CALLS_CHANNEL_ID = "incoming_calls"
    private const val LEGACY_CALLS_CHANNEL_ID = "calls"
    /** Same call notification while the call screen is up and ringing by itself. */
    private const val CALLS_SILENT_CHANNEL_ID = "incoming_calls_silent"
    private const val CALL_RING_MS = 60_000L

    const val ACTION_DECLINE_CALL = "com.aivora.app.action.DECLINE_CALL"
    const val EXTRA_CALL_ID = "call_id"
    /** On MainActivity's intent: the user tapped Answer on the call notification. */
    const val EXTRA_ANSWER_CALL_ID = "answer_call_id"

    private data class CallNote(val title: String, val body: String, val conversationId: String?)
    private val callNotes = java.util.concurrent.ConcurrentHashMap<String, CallNote>()

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
        // The old "calls" channel played the short notification sound; calls now use the ringtone.
        nm.deleteNotificationChannel(LEGACY_CALLS_CHANNEL_ID)
        val soundUri = prefs.callSoundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val existing = nm.getNotificationChannel(CALLS_CHANNEL_ID)
        val want = soundUri?.toString().orEmpty()
        val have = existing?.sound?.toString().orEmpty()
        if (existing != null && have != want) {
            nm.deleteNotificationChannel(CALLS_CHANNEL_ID)
        }
        if (nm.getNotificationChannel(CALLS_CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CALLS_CHANNEL_ID, "Calls", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Incoming voice and video calls"
                    enableVibration(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(
                        soundUri,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                },
            )
        }
    }

    private fun ensureSilentCallsChannel(context: Context): String {
        if (Build.VERSION.SDK_INT < 26) return CALLS_SILENT_CHANNEL_ID
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm != null && nm.getNotificationChannel(CALLS_SILENT_CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CALLS_SILENT_CHANNEL_ID,
                    "Call on screen",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Incoming call while the call screen is already showing"
                    setSound(null, null)
                    enableVibration(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                },
            )
        }
        return CALLS_SILENT_CHANNEL_ID
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

    /**
     * The incoming-call notification, like WhatsApp's: on a locked phone it wakes the screen
     * and opens the call screen; on a phone in use it stays up as a call banner with Answer
     * and Decline, and the ringtone repeats until the call is answered, declined or over.
     */
    fun showCall(
        context: Context,
        title: String,
        body: String,
        conversationId: String?,
        callId: String,
        silent: Boolean = false,
    ) {
        if (!canPost(context)) return
        val prefs = com.aivora.app.data.local.NotificationPrefs.load(context)
        if (!prefs.enabled) return
        ensureCallsChannel(context)
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        callNotes[callId] = CallNote(title, body, conversationId)
        val open = callActivityIntent(context, callId, conversationId, answer = false)
        val answer = callActivityIntent(context, callId, conversationId, answer = true)
        val decline = PendingIntent.getBroadcast(
            context,
            callId.hashCode(),
            Intent(context, CallActionReceiver::class.java)
                .setAction(ACTION_DECLINE_CALL)
                .putExtra(EXTRA_CALL_ID, callId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // On Android 8+ the sound belongs to the channel, so a quiet update must change channel.
        val channel = if (silent) ensureSilentCallsChannel(context) else CALLS_CHANNEL_ID
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_aivora)
            .setContentTitle(title.ifBlank { "Incoming call" })
            .setContentText(body)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // The server stops ringing after 60 s; don't leave a dead call on screen.
            .setTimeoutAfter(CALL_RING_MS)
            .addAction(0, "Decline", decline)
            .addAction(0, "Answer", answer)
        if (silent) {
            // The call screen is showing and ringing itself; keep the notification quiet.
            builder.setSilent(true)
        } else {
            builder.setFullScreenIntent(open, true)
            val customSound = prefs.callSoundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
            builder.setSound(customSound ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            when (prefs.callVibrate) {
                com.aivora.app.data.local.NotificationPrefs.VIBRATE_OFF -> Unit
                com.aivora.app.data.local.NotificationPrefs.VIBRATE_LONG ->
                    builder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
                else -> applyVibrate(builder, prefs.callVibrate)
            }
        }
        val notification = builder.build()
        // Repeat the ringtone until the notification is cancelled, like a phone call.
        if (!silent) notification.flags = notification.flags or Notification.FLAG_INSISTENT
        nm.notify(callNotificationId(callId), notification)
    }

    /** The call screen is up and rings itself: stop the notification's ringtone. */
    fun silenceCall(context: Context, callId: String) {
        val note = callNotes[callId] ?: return
        // An update keeps the ringing record's looping sound going; only a cancel stops it.
        context.getSystemService(NotificationManager::class.java)?.cancel(callNotificationId(callId))
        showCall(context, note.title, note.body, note.conversationId, callId, silent = true)
    }

    /** Removes the incoming-call notification once that call is answered, declined or over. */
    fun cancelCall(context: Context, callId: String) {
        callNotes.remove(callId)
        context.getSystemService(NotificationManager::class.java)
            ?.cancel(callNotificationId(callId))
    }

    private fun callNotificationId(callId: String) = callId.hashCode() and 0x7fffffff

    private fun callActivityIntent(
        context: Context,
        callId: String,
        conversationId: String?,
        answer: Boolean,
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("incoming_call_id", callId)
            if (answer) putExtra(EXTRA_ANSWER_CALL_ID, callId)
            if (!conversationId.isNullOrBlank()) putExtra("conversation_id", conversationId)
        }
        // Extras don't make two PendingIntents different, so open and Answer need their own codes.
        return PendingIntent.getActivity(
            context,
            callId.hashCode() * 31 + if (answer) 1 else 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
