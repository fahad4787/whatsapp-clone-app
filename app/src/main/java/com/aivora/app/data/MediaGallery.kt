package com.aivora.app.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.aivora.app.data.local.ChatComposerPrefs
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Wave 57 — publish chat media into the system gallery when Media visibility is on.
 * Explicit Save from the viewer always publishes (user intent).
 */
object MediaGallery {
    private val http = OkHttpClient()

    fun visibilityEnabled(context: Context): Boolean =
        ChatComposerPrefs.load(context).mediaVisibility

    /**
     * After a successful send: copy image/video into Pictures/Aivora or Movies/Aivora
     * when Media visibility is enabled. Skips view-once and non-AV types.
     */
    suspend fun publishOutgoingIfVisible(
        context: Context,
        uri: Uri,
        mime: String,
        viewOnce: Boolean,
    ): Boolean {
        if (viewOnce || !visibilityEnabled(context)) return false
        if (!mime.startsWith("image/") && !mime.startsWith("video/")) return false
        return publishUri(context, uri, mime) != null
    }

    /** Download a remote media URL into the gallery (viewer Save / auto). */
    suspend fun publishUrl(
        context: Context,
        url: String,
        mimeHint: String? = null,
    ): Uri? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        runCatching {
            val req = Request.Builder().url(url).get().build()
            http.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@use null
                val body = res.body ?: return@use null
                val mime = mimeHint
                    ?: res.header("Content-Type")?.substringBefore(';')?.trim()
                    ?: guessMime(url)
                val bytes = body.bytes()
                writeBytes(context, bytes, mime)
            }
        }.getOrNull()
    }

    suspend fun publishUri(context: Context, uri: Uri, mime: String?): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@runCatching null
                val resolved = mime
                    ?: context.contentResolver.getType(uri)
                    ?: "image/jpeg"
                writeBytes(context, bytes, resolved)
            }.getOrNull()
        }

    private fun writeBytes(context: Context, bytes: ByteArray, mime: String): Uri? {
        val video = mime.startsWith("video/")
        val ext = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            mime.contains("gif") -> "gif"
            video && mime.contains("webm") -> "webm"
            video -> "mp4"
            else -> "jpg"
        }
        val name = "Aivora_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext"
        val resolver = context.contentResolver
        val collection = if (video) {
            if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        } else {
            if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            if (Build.VERSION.SDK_INT >= 29) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    if (video) {
                        Environment.DIRECTORY_MOVIES + "/Aivora"
                    } else {
                        Environment.DIRECTORY_PICTURES + "/Aivora"
                    },
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val out = resolver.insert(collection, values) ?: return null
        return try {
            resolver.openOutputStream(out)?.use { it.write(bytes) }
                ?: run {
                    resolver.delete(out, null, null)
                    return null
                }
            if (Build.VERSION.SDK_INT >= 29) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(out, values, null, null)
            }
            out
        } catch (_: Exception) {
            runCatching { resolver.delete(out, null, null) }
            null
        }
    }

    private fun guessMime(url: String): String {
        val path = url.substringBefore('?').lowercase()
        return when {
            path.endsWith(".png") -> "image/png"
            path.endsWith(".webp") -> "image/webp"
            path.endsWith(".gif") -> "image/gif"
            path.endsWith(".mp4") || path.endsWith(".mov") -> "video/mp4"
            else -> "image/jpeg"
        }
    }
}
