package com.aivora.app.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.aivora.app.BuildConfig
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.LocalStatus
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.local.StatusCache
import com.aivora.app.data.model.StatusCreateBody
import com.aivora.app.data.model.StatusItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private val JSON = "application/json; charset=utf-8".toMediaType()

object StatusRepository {
    private const val TAG = "StatusRepository"

    fun hydrateFromDisk(context: Context) {
        // Skip Main/IO JSON parse when Updates feed is already warm.
        if (InMemoryStatusStore.statuses.value.isNotEmpty()) return
        InMemoryStatusStore.hydrateFromCache(StatusCache.load(context))
    }

    suspend fun refresh(context: Context, api: AivoraApi): Boolean {
        val myId = SessionStore.userId(context)
        return runCatching {
            val list = api.getStatus().items
            val local = InMemoryStatusStore.fromRows(list, myId)
            InMemoryStatusStore.applyRemoteFeed(local)
            InMemoryStatusStore.persist(context)
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
                createdAtMs = System.currentTimeMillis(),
            ),
        )
        InMemoryStatusStore.persist(context)
        return created
    }

    suspend fun markViewed(api: AivoraApi, statusId: String): Boolean {
        // Prefer raw OkHttp — Retrofit Map<String, Any?> bodies used to fail silently
        // with kotlinx.serialization, so views never reached the server.
        val okHttp = NetworkModule.okHttp()
        val remoteOk = withContext(Dispatchers.IO) {
            runCatching {
                val req = Request.Builder()
                    .url(BuildConfig.API_BASE_URL.trimEnd('/') + "/api/status/$statusId")
                    .post("{}".toRequestBody(JSON))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build()
                okHttp.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        Log.w(TAG, "markViewed failed code=${resp.code} id=$statusId")
                        return@use false
                    }
                    true
                }
            }.getOrElse {
                Log.w(TAG, "markViewed okhttp error id=$statusId", it)
                runCatching {
                    api.postStatusId(statusId, com.aivora.app.data.model.EmptyRequestBody())
                    true
                }.getOrElse { e ->
                    Log.w(TAG, "markViewed retrofit error id=$statusId", e)
                    false
                }
            }
        }
        if (remoteOk) InMemoryStatusStore.markViewed(statusId)
        return remoteOk
    }

    /** Pull latest view count for an owned status (viewers list length). */
    suspend fun refreshViewCount(api: AivoraApi, statusId: String): Int? {
        return runCatching {
            val count = api.getStatusIdViewers(statusId).items.size
            InMemoryStatusStore.setViewCount(statusId, count)
            count
        }.getOrNull()
    }

    /**
     * Optimistic local remove so Updates/list update instantly, then confirm with the API.
     * Restores the row if the server rejects the delete.
     */
    suspend fun delete(context: Context, api: AivoraApi, statusId: String): Boolean {
        val snapshot = InMemoryStatusStore.removeReturning(statusId)
        InMemoryStatusStore.persist(context)

        val okHttp = NetworkModule.okHttp { SessionStore.accessToken(context) }
        val remoteOk = withContext(Dispatchers.IO) {
            runCatching {
                val req = Request.Builder()
                    .url(BuildConfig.API_BASE_URL.trimEnd('/') + "/api/status/$statusId")
                    .delete()
                    .header("Accept", "application/json")
                    .build()
                okHttp.newCall(req).execute().use { resp ->
                    val good = resp.isSuccessful || resp.code == 404
                    if (!good) Log.w(TAG, "delete status failed code=${resp.code} id=$statusId")
                    good
                }
            }.getOrElse {
                Log.w(TAG, "delete status error id=$statusId", it)
                runCatching {
                    val res = api.deleteStatusId(statusId)
                    res.isSuccessful || res.code() == 204 || res.code() == 404
                }.getOrDefault(false)
            }
        }
        if (!remoteOk) {
            if (snapshot != null) {
                InMemoryStatusStore.put(snapshot)
                InMemoryStatusStore.persist(context)
            }
            return false
        }
        return true
    }
}
