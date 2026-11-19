package com.aivora.app.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aivora.app.data.api.AivoraApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Wave 32 — download account info JSON and open the share sheet. */
object AccountInfoExport {
    suspend fun downloadAndShare(context: Context, api: AivoraApi): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = api.getMeData()
                val bytes = body.bytes()
                body.close()
                require(bytes.isNotEmpty()) { "empty report" }

                val dir = File(context.cacheDir, "account-info").apply { mkdirs() }
                val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val file = File(dir, "Aivora Account Info $day.json")
                file.writeBytes(bytes)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newUri(context.contentResolver, file.name, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(share, "Account information"))
                }
                true
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Could not create report: ${it.message ?: "error"}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
                false
            }
        }
}
