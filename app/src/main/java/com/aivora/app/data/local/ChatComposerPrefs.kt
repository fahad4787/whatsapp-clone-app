package com.aivora.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Chats settings toggles (Wave 37). */
object ChatComposerPrefs {
    private const val PREFS = "aivora_chat_composer"
    private const val KEY_ENTER_SEND = "enter_is_send"
    private const val KEY_MEDIA_VIS = "media_visibility"
    private const val KEY_KEEP_ARCHIVED = "keep_chats_archived"

    data class Snapshot(
        val enterIsSend: Boolean = false,
        val mediaVisibility: Boolean = true,
        val keepArchived: Boolean = true,
    )

    private val _snap = MutableStateFlow(Snapshot())
    val flow: StateFlow<Snapshot> = _snap.asStateFlow()

    fun hydrate(context: Context) {
        _snap.value = load(context)
    }

    fun load(context: Context): Snapshot {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Snapshot(
            enterIsSend = p.getBoolean(KEY_ENTER_SEND, false),
            mediaVisibility = p.getBoolean(KEY_MEDIA_VIS, true),
            keepArchived = p.getBoolean(KEY_KEEP_ARCHIVED, true),
        )
    }

    private fun edit(context: Context, block: (Snapshot) -> Snapshot) {
        val next = block(_snap.value)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENTER_SEND, next.enterIsSend)
            .putBoolean(KEY_MEDIA_VIS, next.mediaVisibility)
            .putBoolean(KEY_KEEP_ARCHIVED, next.keepArchived)
            .apply()
        _snap.value = next
    }

    fun setEnterIsSend(context: Context, value: Boolean) =
        edit(context) { it.copy(enterIsSend = value) }

    fun setMediaVisibility(context: Context, value: Boolean) =
        edit(context) { it.copy(mediaVisibility = value) }

    fun setKeepArchived(context: Context, value: Boolean) =
        edit(context) { it.copy(keepArchived = value) }
}
