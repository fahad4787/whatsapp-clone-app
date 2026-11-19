package com.aivora.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipInputStream

/** Wave 50 — inspect Aivora backup zips and share per-chat transcripts. */
object BackupZipBrowser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Serializable
    data class Manifest(
        val format: String? = null,
        @SerialName("exported_at") val exportedAt: String? = null,
        @SerialName("include_media") val includeMedia: Boolean? = null,
        @SerialName("chat_count") val chatCount: Int? = null,
        val note: String? = null,
    )

    data class ChatEntry(
        val folder: String,
        val zipPath: String,
    )

    data class Inspection(
        val manifest: Manifest?,
        val chats: List<ChatEntry>,
        val error: String? = null,
    )

    fun localAutoBackups(context: Context): List<File> {
        val dir = File(context.filesDir, "backups")
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles { f -> f.isFile && f.name.endsWith(".zip", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
    }

    suspend fun inspectUri(context: Context, uri: Uri): Inspection = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                inspectStream(input)
            } ?: Inspection(null, emptyList(), "Could not open file")
        }.getOrElse {
            Inspection(null, emptyList(), it.message ?: "Invalid backup")
        }
    }

    suspend fun inspectFile(file: File): Inspection = withContext(Dispatchers.IO) {
        runCatching {
            file.inputStream().use { inspectStream(it) }
        }.getOrElse {
            Inspection(null, emptyList(), it.message ?: "Invalid backup")
        }
    }

    private fun inspectStream(input: java.io.InputStream): Inspection {
        var manifest: Manifest? = null
        val chats = mutableListOf<ChatEntry>()
        ZipInputStream(input.buffered()).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                val name = entry.name.trimStart('/')
                when {
                    name.equals("manifest.json", ignoreCase = true) && !entry.isDirectory -> {
                        val text = zis.readBytes().toString(Charsets.UTF_8)
                        manifest = runCatching { json.decodeFromString(Manifest.serializer(), text) }
                            .getOrNull()
                    }
                    name.matches(Regex("""chats/[^/]+/_chat\.txt""", RegexOption.IGNORE_CASE)) &&
                        !entry.isDirectory -> {
                        val folder = name.removePrefix("chats/").substringBefore('/')
                        chats.add(ChatEntry(folder = folder, zipPath = name))
                    }
                }
                zis.closeEntry()
            }
        }
        if (manifest == null && chats.isEmpty()) {
            return Inspection(null, emptyList(), "Not an Aivora backup zip")
        }
        return Inspection(
            manifest = manifest,
            chats = chats.sortedBy { it.folder.lowercase() },
        )
    }

    suspend fun shareChatTxt(
        context: Context,
        source: Uri?,
        sourceFile: File?,
        chat: ChatEntry,
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = when {
                sourceFile != null -> extractEntry(sourceFile.inputStream(), chat.zipPath)
                source != null -> context.contentResolver.openInputStream(source)?.use {
                    extractEntry(it, chat.zipPath)
                }
                else -> null
            } ?: return@runCatching false

            val dir = File(context.cacheDir, "backup_open").apply { mkdirs() }
            val safe = chat.folder.replace(Regex("[^A-Za-z0-9._\\- ]+"), "_").trim().take(40)
                .ifBlank { "chat" }
            val out = File(dir, "Aivora Chat - $safe.txt")
            out.writeBytes(bytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                out,
            )
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newUri(context.contentResolver, out.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(share, "Share transcript"))
            }
            true
        }.getOrDefault(false)
    }

    private fun extractEntry(input: java.io.InputStream, path: String): ByteArray? {
        ZipInputStream(input.buffered()).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                val name = entry.name.trimStart('/')
                if (name.equals(path, ignoreCase = true) && !entry.isDirectory) {
                    return zis.readBytes()
                }
                zis.closeEntry()
            }
        }
        return null
    }
}
