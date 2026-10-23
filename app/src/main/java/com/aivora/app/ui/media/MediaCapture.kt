package com.aivora.app.ui.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

data class MediaActions(
    val takePhoto: () -> Unit,
    val pickGallery: () -> Unit,
    val pickDocument: () -> Unit,
)

fun Context.createCaptureUri(): Uri {
    val dir = File(cacheDir, "capture").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
}

fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

fun Context.hasMicPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

fun Context.hasReadMediaPermission(): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun rememberMediaActions(
    context: Context,
    onCancel: (() -> Unit)? = null,
    onDocument: ((Uri) -> Unit)? = null,
    onImage: (Uri) -> Unit,
): MediaActions {
    var pendingCapture by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { ok ->
        val uri = pendingCapture
        pendingCapture = null
        if (ok && uri != null) {
            onImage(uri)
        } else {
            onCancel?.invoke()
        }
    }

    val pickVisual = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onImage(uri)
        else onCancel?.invoke()
    }

    val pickDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onDocument?.invoke(uri)
        } else {
            onCancel?.invoke()
        }
    }

    val requestCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = context.createCaptureUri()
            pendingCapture = uri
            takePicture.launch(uri)
        } else {
            onCancel?.invoke()
        }
    }

    return MediaActions(
        takePhoto = {
            if (context.hasCameraPermission()) {
                val uri = context.createCaptureUri()
                pendingCapture = uri
                takePicture.launch(uri)
            } else {
                requestCamera.launch(Manifest.permission.CAMERA)
            }
        },
        pickGallery = {
            pickVisual.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo,
                ),
            )
        },
        pickDocument = {
            pickDoc.launch(
                arrayOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "text/plain",
                    "application/zip",
                    "*/*",
                ),
            )
        },
    )
}
