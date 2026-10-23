package com.aivora.app.data.local

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Disk snapshot of the home chat list for instant cold-start paint. */
object ChatListCache {
    private const val PREFS = "aivora_chat_cache"
    private const val KEY_LIST = "conversations_v1"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Serializable
    private data class CachedChat(
        val id: String,
        val title: String,
        val preview: String,
        val time: String,
        val unread: Int = 0,
        val isGroup: Boolean = false,
        val favorite: Boolean = false,
        val archived: Boolean = false,
        val pinned: Boolean = false,
        val mutedUntil: String? = null,
        val blockedByMe: Boolean = false,
        val blockedMe: Boolean = false,
        val peerUserId: String? = null,
        val myRole: String? = null,
        val description: String? = null,
        val avatarUrl: String? = null,
        val onlyAdminsCanSend: Boolean = false,
        val onlyAdminsCanEditInfo: Boolean = false,
    )

    fun load(context: Context): List<LocalChat> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LIST, null)
            ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<CachedChat>>(raw).map {
                LocalChat(
                    id = it.id,
                    title = it.title,
                    preview = it.preview,
                    time = it.time,
                    unread = it.unread,
                    isGroup = it.isGroup,
                    favorite = it.favorite,
                    archived = it.archived,
                    pinned = it.pinned,
                    mutedUntil = it.mutedUntil,
                    blockedByMe = it.blockedByMe,
                    blockedMe = it.blockedMe,
                    peerUserId = it.peerUserId,
                    myRole = it.myRole,
                    description = it.description,
                    avatarUrl = it.avatarUrl,
                    onlyAdminsCanSend = it.onlyAdminsCanSend,
                    onlyAdminsCanEditInfo = it.onlyAdminsCanEditInfo,
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, chats: List<LocalChat>) {
        val payload = chats.map {
            CachedChat(
                id = it.id,
                title = it.title,
                preview = it.preview,
                time = it.time,
                unread = it.unread,
                isGroup = it.isGroup,
                favorite = it.favorite,
                archived = it.archived,
                pinned = it.pinned,
                mutedUntil = it.mutedUntil,
                blockedByMe = it.blockedByMe,
                blockedMe = it.blockedMe,
                peerUserId = it.peerUserId,
                myRole = it.myRole,
                description = it.description,
                avatarUrl = it.avatarUrl,
                onlyAdminsCanSend = it.onlyAdminsCanSend,
                onlyAdminsCanEditInfo = it.onlyAdminsCanEditInfo,
            )
        }
        runCatching {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LIST, json.encodeToString(payload))
                .apply()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
