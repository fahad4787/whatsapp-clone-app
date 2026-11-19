package com.aivora.app.data

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.local.BackupPrefs
import com.aivora.app.data.local.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Wave 29/48 — download account backup zip (share sheet or silent auto). */
object ChatBackup {
    fun onWifi(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Downloads backup zip. Returns file + byte size, or null if skipped/failed.
     * @param quiet no Toasts (for WorkManager).
     * @param durable store under filesDir/backups (auto) vs cacheDir (share).
     */
    suspend fun downloadZip(
        context: Context,
        api: AivoraApi,
        includeMedia: Boolean,
        quiet: Boolean,
        durable: Boolean,
    ): Pair<File, Long>? = withContext(Dispatchers.IO) {
        runCatching {
            if (SessionStore.accessToken(context).isNullOrBlank()) {
                return@runCatching null
            }
            if (BackupPrefs.wifiOnly(context) && !onWifi(context)) {
                if (!quiet) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Wi-Fi only is on — connect to Wi-Fi to back up",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
                return@runCatching null
            }

            val body = api.getMeBackup(media = if (includeMedia) 1 else 0)
            val bytes = body.bytes()
            body.close()
            require(bytes.isNotEmpty()) { "empty backup" }

            val dir = if (durable) {
                File(context.filesDir, "backups").apply { mkdirs() }
            } else {
                File(context.cacheDir, "backup").apply { mkdirs() }
            }
            val day = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
            val file = File(dir, "Aivora_Backup_$day.zip")
            file.writeBytes(bytes)
            BackupPrefs.recordSuccess(context, bytes.size.toLong())
            if (durable) pruneOldBackups(dir, keep = 5)
            file to bytes.size.toLong()
        }.getOrElse {
            if (!quiet) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Could not back up: ${it.message ?: "error"}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
            null
        }
    }

    suspend fun downloadAndShare(
        context: Context,
        api: AivoraApi,
        includeMedia: Boolean,
    ): Boolean {
        val result = downloadZip(
            context = context,
            api = api,
            includeMedia = includeMedia,
            quiet = false,
            durable = false,
        ) ?: return false
        val (file, _) = result
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newUri(context.contentResolver, file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        withContext(Dispatchers.Main) {
            context.startActivity(Intent.createChooser(share, "Save chat backup"))
        }
        return true
    }

    /** Silent auto-backup into app files (Wave 48). */
    suspend fun downloadAuto(context: Context, api: AivoraApi): Boolean {
        val includeMedia = BackupPrefs.includeVideos(context)
        return downloadZip(
            context = context,
            api = api,
            includeMedia = includeMedia,
            quiet = true,
            durable = true,
        ) != null
    }

    private fun pruneOldBackups(dir: File, keep: Int) {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".zip") }
            ?.sortedByDescending { it.lastModified() }
            ?: return
        files.drop(keep).forEach { runCatching { it.delete() } }
    }
}
