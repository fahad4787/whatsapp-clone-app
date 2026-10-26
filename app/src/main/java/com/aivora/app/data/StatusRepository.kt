package com.aivora.app.data

import android.content.Context
import android.net.Uri
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.LocalStatus
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.local.StatusCache
import com.aivora.app.data.model.StatusCreateBody
import com.aivora.app.data.model.StatusItemDto

object StatusRepository {
    fun hydrateFromDisk(context: Context) {
        InMemoryStatusStore.hydrateFromCache(StatusCache.load(context))
    }

    suspend fun refresh(context: Context, api: AivoraApi): Boolean {
        val myId = SessionStore.userId(context)
        return runCatching {
            val list = api.getStatus().items
            val local = InMemoryStatusStore.fromRows(list, myId)
            InMemoryStatusStore.replaceAll(local)
            StatusCache.save(context, local)
            true
        }.getOrDefault(false)
    }

    suspend fun post(
        context: Context,
        api: AivoraApi,
        text: String,
        background: String?,
        mediaUri: String?,
    ): StatusItemDto? {
        val attachment = mediaUri?.let { uriStr ->
            runCatching {
                api.uploadAttachment(
                    context = context,
                    uri = Uri.parse(uriStr),
                    defaultName = "status.jpg",
                    defaultMime = "image/jpeg",
                )
            }.getOrNull()
        }
        val body = StatusCreateBody(
            kind = when {
                attachment?.mime?.startsWith("video/") == true -> "video"
                attachment != null -> "image"
                else -> "text"
            },
            text = text.trim().takeIf { it.isNotBlank() },
            background = background,
            attachment = attachment,
        )
        val created = runCatching { api.postStatus(body) }.getOrNull() ?: return null
        val id = created.id ?: return created
        InMemoryStatusStore.prependMine(
            LocalStatus(
                id = id,
                authorId = SessionStore.userId(context) ?: "me",
                authorName = "My status",
                text = created.text.orEmpty().ifBlank {
                    if (attachment != null) "Photo" else ""
                },
                mediaUri = created.attachment?.url ?: attachment?.url,
                background = created.background ?: background,
                timeLabel = "Just now",
                mine = true,
                viewed = true,
                viewCount = created.viewCount,
            ),
        )
        StatusCache.save(context, InMemoryStatusStore.statuses.value)
        return created
    }

    suspend fun markViewed(api: AivoraApi, statusId: String): Boolean {
        return runCatching {
            api.postStatusId(statusId)
            InMemoryStatusStore.markViewed(statusId)
            true
        }.getOrDefault(false)
    }

    suspend fun delete(context: Context, api: AivoraApi, statusId: String): Boolean {
        return runCatching {
            api.deleteStatusId(statusId)
            InMemoryStatusStore.remove(statusId)
            StatusCache.save(context, InMemoryStatusStore.statuses.value)
            true
        }.getOrDefault(false)
    }
}
