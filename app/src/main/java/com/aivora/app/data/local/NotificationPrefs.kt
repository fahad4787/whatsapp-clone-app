package com.aivora.app.data.local

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Message / call notification toggles (Wave 36 + tones 47/53). */
object NotificationPrefs {
    private const val PREFS = "aivora_notif_prefs"

    private const val KEY_ENABLED = "enabled"
    private const val KEY_TONES = "conversation_tones"
    private const val KEY_HIGH = "high_priority"
    private const val KEY_REACTIONS = "reaction_notifications"
    private const val KEY_PREVIEW = "show_preview"
    private const val KEY_MSG_VIBRATE = "msg_vibrate"
    private const val KEY_GROUP_VIBRATE = "group_vibrate"
    private const val KEY_CALL_VIBRATE = "call_vibrate"
    private const val KEY_MSG_SOUND = "msg_sound_uri"
    private const val KEY_CALL_SOUND = "call_sound_uri"

    const val VIBRATE_DEFAULT = "default"
    const val VIBRATE_OFF = "off"
    const val VIBRATE_LONG = "long"

    data class Snapshot(
        val enabled: Boolean = true,
        val conversationTones: Boolean = true,
        val highPriority: Boolean = true,
        val reactions: Boolean = true,
        val showPreview: Boolean = true,
        val msgVibrate: String = VIBRATE_DEFAULT,
        val groupVibrate: String = VIBRATE_DEFAULT,
        val callVibrate: String = VIBRATE_DEFAULT,
        /** Null / blank = system default notification sound. */
        val msgSoundUri: String? = null,
        /** Null / blank = system default ringtone. */
        val callSoundUri: String? = null,
    )

    private val _snap = MutableStateFlow(Snapshot())
    val flow: StateFlow<Snapshot> = _snap.asStateFlow()

    fun hydrate(context: Context) {
        _snap.value = load(context)
    }

    fun load(context: Context): Snapshot {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Snapshot(
            enabled = p.getBoolean(KEY_ENABLED, true),
            conversationTones = p.getBoolean(KEY_TONES, true),
            highPriority = p.getBoolean(KEY_HIGH, true),
            reactions = p.getBoolean(KEY_REACTIONS, true),
            showPreview = p.getBoolean(KEY_PREVIEW, true),
            msgVibrate = p.getString(KEY_MSG_VIBRATE, VIBRATE_DEFAULT) ?: VIBRATE_DEFAULT,
            groupVibrate = p.getString(KEY_GROUP_VIBRATE, VIBRATE_DEFAULT) ?: VIBRATE_DEFAULT,
            callVibrate = p.getString(KEY_CALL_VIBRATE, VIBRATE_DEFAULT) ?: VIBRATE_DEFAULT,
            msgSoundUri = p.getString(KEY_MSG_SOUND, null)?.takeIf { it.isNotBlank() },
            callSoundUri = p.getString(KEY_CALL_SOUND, null)?.takeIf { it.isNotBlank() },
        )
    }

    private fun edit(context: Context, block: (Snapshot) -> Snapshot) {
        val next = block(_snap.value)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, next.enabled)
            .putBoolean(KEY_TONES, next.conversationTones)
            .putBoolean(KEY_HIGH, next.highPriority)
            .putBoolean(KEY_REACTIONS, next.reactions)
            .putBoolean(KEY_PREVIEW, next.showPreview)
            .putString(KEY_MSG_VIBRATE, next.msgVibrate)
            .putString(KEY_GROUP_VIBRATE, next.groupVibrate)
            .putString(KEY_CALL_VIBRATE, next.callVibrate)
            .putString(KEY_MSG_SOUND, next.msgSoundUri)
            .putString(KEY_CALL_SOUND, next.callSoundUri)
            .apply()
        _snap.value = next
    }

    fun setEnabled(context: Context, value: Boolean) =
        edit(context) { it.copy(enabled = value) }

    fun setConversationTones(context: Context, value: Boolean) =
        edit(context) { it.copy(conversationTones = value) }

    fun setHighPriority(context: Context, value: Boolean) =
        edit(context) { it.copy(highPriority = value) }

    fun setReactions(context: Context, value: Boolean) =
        edit(context) { it.copy(reactions = value) }

    fun setShowPreview(context: Context, value: Boolean) =
        edit(context) { it.copy(showPreview = value) }

    fun setMsgVibrate(context: Context, value: String) =
        edit(context) { it.copy(msgVibrate = value) }

    fun setGroupVibrate(context: Context, value: String) =
        edit(context) { it.copy(groupVibrate = value) }

    fun setCallVibrate(context: Context, value: String) =
        edit(context) { it.copy(callVibrate = value) }

    fun setMsgSoundUri(context: Context, uri: String?) =
        edit(context) { it.copy(msgSoundUri = uri?.takeIf { u -> u.isNotBlank() }) }

    fun setCallSoundUri(context: Context, uri: String?) =
        edit(context) { it.copy(callSoundUri = uri?.takeIf { u -> u.isNotBlank() }) }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        _snap.value = Snapshot()
    }

    fun vibrateLabel(key: String): String = when (key) {
        VIBRATE_OFF -> "Off"
        VIBRATE_LONG -> "Long"
        else -> "Default"
    }

    fun soundLabel(context: Context, uriStr: String?): String {
        if (uriStr.isNullOrBlank()) return "Default"
        return runCatching {
            val ring = RingtoneManager.getRingtone(context, Uri.parse(uriStr))
            ring?.getTitle(context)?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: "Custom"
    }
}
