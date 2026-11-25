package com.aivora.app.data.local

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object StatusCache {
    private const val PREFS = "aivora_status_cache"
    private const val KEY = "statuses_v1"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Serializable
    private data class CachedStatus(
        val id: String,
        val authorId: String,
        val authorName: String,
        val text: String,
        val mediaUri: String? = null,
        val background: String? = null,
        val timeLabel: String = "Just now",
        val mine: Boolean = false,
        val viewed: Boolean = false,
        val viewCount: Int = 0,
        val createdAtMs: Long = 0L,
        val authorAvatarUrl: String? = null,
    )

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun load(context: Context): List<LocalStatus> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<CachedStatus>>(raw).map {
                LocalStatus(
                    id = it.id,
                    authorId = it.authorId,
                    authorName = it.authorName,
                    text = it.text,
                    mediaUri = it.mediaUri,
                    background = it.background,
                    timeLabel = it.timeLabel,
                    mine = it.mine,
                    viewed = it.viewed,
                    viewCount = it.viewCount,
                    createdAtMs = it.createdAtMs,
                    authorAvatarUrl = it.authorAvatarUrl,
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, statuses: List<LocalStatus>) {
        val payload = statuses.map {
            CachedStatus(
                id = it.id,
                authorId = it.authorId,
                authorName = it.authorName,
                text = it.text,
                mediaUri = it.mediaUri,
                background = it.background,
                timeLabel = it.timeLabel,
                mine = it.mine,
                viewed = it.viewed,
                viewCount = it.viewCount,
                createdAtMs = it.createdAtMs,
                authorAvatarUrl = it.authorAvatarUrl,
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, json.encodeToString(payload))
            .apply()
    }
}
