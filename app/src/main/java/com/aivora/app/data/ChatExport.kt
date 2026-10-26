package com.aivora.app.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aivora.app.data.api.AivoraApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Download a conversation export from the API and open the system share sheet. */
object ChatExport {
    suspend fun downloadAndShare(
        context: Context,
        api: AivoraApi,
        conversationId: String,
        chatTitle: String,
        includeMedia: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val body = api.getConversationsIdExport(
                id = conversationId,
                media = if (includeMedia) 1 else 0,
            )
            val bytes = body.bytes()
            body.close()
            require(bytes.isNotEmpty()) { "empty export" }

            val dir = File(context.cacheDir, "export").apply { mkdirs() }
            val safe = chatTitle.replace(Regex("[^A-Za-z0-9._\\- ]+"), "_").trim().take(40)
                .ifBlank { "chat" }
            val ext = if (includeMedia) "zip" else "txt"
            val file = File(dir, "Aivora Chat - $safe.$ext")
            file.writeBytes(bytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val share = Intent(Intent.ACTION_SEND).apply {
                type = if (includeMedia) "application/zip" else "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newUri(context.contentResolver, file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(share, "Export chat"))
            }
            true
        }.getOrElse {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Could not export: ${it.message ?: "error"}",
                    Toast.LENGTH_LONG,
                ).show()
            }
            false
        }
    }
}
