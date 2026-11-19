package com.aivora.app.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aivora.app.data.api.AivoraApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Wave 51 — multi-chat text transcripts as one zip. */
object ChatTranscripts {
    data class ChatRef(val id: String, val title: String)

    suspend fun downloadAndShareZip(
        context: Context,
        api: AivoraApi,
        chats: List<ChatRef>,
    ): Boolean = withContext(Dispatchers.IO) {
        if (chats.isEmpty()) return@withContext false
        runCatching {
            val dir = File(context.cacheDir, "transcripts").apply { mkdirs() }
            val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val zipFile = File(dir, "Aivora Transcripts $day.zip")
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                val used = mutableSetOf<String>()
                for (chat in chats) {
                    val body = api.getConversationsIdExport(id = chat.id, media = 0)
                    val bytes = body.bytes()
                    body.close()
                    require(bytes.isNotEmpty()) { "empty export for ${chat.title}" }
                    var folder = chat.title
                        .replace(Regex("[^A-Za-z0-9._\\-()+ ]+"), "_")
                        .trim()
                        .take(60)
                        .ifBlank { "chat" }
                    if (folder in used) {
                        var n = 2
                        while ("${folder}_$n" in used) n += 1
                        folder = "${folder}_$n"
                    }
                    used.add(folder)
                    zos.putNextEntry(ZipEntry("chats/$folder/_chat.txt"))
                    zos.write(bytes)
                    zos.closeEntry()
                }
                val whenIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                val note =
                    """{"format":"aivora-transcripts-v1","chat_count":${chats.size},"exported_at":"$whenIso","note":"Text-only transcripts. Sign in restores live chats from the server."}""" + "\n"
                zos.putNextEntry(ZipEntry("manifest.json"))
                zos.write(note.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile,
            )
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newUri(context.contentResolver, zipFile.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(share, "Share transcripts"))
            }
            true
        }.getOrElse {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Could not generate transcripts: ${it.message ?: "error"}",
                    Toast.LENGTH_LONG,
                ).show()
            }
            false
        }
    }
}
