package com.aivora.app.ui.feature.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aivora.app.data.QrCodec
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.QrPrefs
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.DirectBody
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.LinkButton
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsQrMyScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                SessionStore.accessToken(context)
            },
        )
    }
    var name by remember { mutableStateOf(SessionStore.phone(context).orEmpty()) }
    var userId by remember { mutableStateOf(SessionStore.userId(context).orEmpty()) }
    var showPhoto by remember { mutableStateOf(QrPrefs.showNamePhoto(context)) }
    var qrBmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(Unit) {
        runCatching { api.getMe() }
            .onSuccess { me ->
                userId = me.id.orEmpty().ifBlank { userId }
                if (userId.isNotBlank()) SessionStore.saveUserId(context, userId)
                name = me.displayName?.takeIf { it.isNotBlank() }
                    ?: me.phoneDisplay
                    ?: me.phone
                    ?: name
            }
        val id = userId
        if (id.isNotBlank()) {
            qrBmp = withContext(Dispatchers.Default) {
                QrCodec.encodeBitmap(QrCodec.profilePayload(id))
            }
        }
    }

    AivoraScaffold(title = "My QR code", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            if (showPhoto) {
                Text(
                    name.ifBlank { "Aivora" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
            }
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AivoraColors.LightSurface),
                contentAlignment = Alignment.Center,
            ) {
                val bmp = qrBmp
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "My QR code",
                        modifier = Modifier.size(240.dp),
                    )
                } else {
                    CircularProgressIndicator(color = AivoraColors.Primary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Scan this code in Aivora to start a chat with you.",
                style = MaterialTheme.typography.bodySmall,
                color = AivoraColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = "Share link",
                onClick = {
                    val id = userId
                    if (id.isBlank()) {
                        Toast.makeText(context, "Sign in first", Toast.LENGTH_SHORT).show()
                        return@PrimaryButton
                    }
                    val link = QrCodec.HttpsPrefix + id
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, link)
                    }
                    context.startActivity(Intent.createChooser(send, "Share QR link"))
                },
            )
            LinkButton(
                text = if (showPhoto) "Hide name on QR page" else "Show name on QR page",
                onClick = {
                    showPhoto = !showPhoto
                    QrPrefs.setShowNamePhoto(context, showPhoto)
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SettingsQrScanScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                SessionStore.accessToken(context)
            },
        )
    }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var camGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val handled = remember { AtomicBoolean(false) }

    fun openUser(userId: String) {
        if (!handled.compareAndSet(false, true)) return
        if (userId == SessionStore.userId(context)) {
            handled.set(false)
            error = "That’s your own QR code"
            return
        }
        busy = true
        error = null
        scope.launch {
            runCatching {
                api.getUsersId(userId)
                val conv = api.postConversationsDirect(DirectBody(userId = userId))
                InMemoryChatStore.upsertChat(conv.toLocalChat())
                conv.id.orEmpty()
            }.onSuccess { chatId ->
                busy = false
                if (chatId.isBlank()) {
                    handled.set(false)
                    error = "Could not open chat"
                } else {
                    onNavigate(Routes.ChatId.replace("{id}", chatId))
                }
            }.onFailure {
                busy = false
                handled.set(false)
                error = com.aivora.app.data.api.ApiErrors.message(it, "Could not open chat")
            }
        }
    }

    fun approveLink(linkCode: String) {
        if (!handled.compareAndSet(false, true)) return
        busy = true
        error = null
        scope.launch {
            runCatching {
                api.postAuthLinkApprove(
                    com.aivora.app.data.model.DeviceLinkCodeBody(code = linkCode),
                )
            }.onSuccess {
                busy = false
                Toast.makeText(context, "Device linked", Toast.LENGTH_SHORT).show()
                onNavigate(Routes.SettingsDevices)
            }.onFailure {
                busy = false
                handled.set(false)
                error = com.aivora.app.data.api.ApiErrors.message(it, "Could not link device")
            }
        }
    }

    fun onPayload(raw: String?) {
        QrCodec.linkCodeFromPayload(raw)?.let {
            approveLink(it)
            return
        }
        val id = QrCodec.userIdFromPayload(raw) ?: run {
            // Ignore non-Aivora frames from live camera; gallery still surfaces an error.
            return
        }
        openUser(id)
    }

    val askCam = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { ok -> camGranted = ok }

    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val payload = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream) ?: return@use null
                    QrCodec.decodeBitmap(bmp)
                }
            }
            if (QrCodec.linkCodeFromPayload(payload) == null &&
                QrCodec.userIdFromPayload(payload) == null
            ) {
                error = "No Aivora QR found in that image"
            } else {
                onPayload(payload)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!camGranted) askCam.launch(Manifest.permission.CAMERA)
    }

    AivoraScaffold(title = "Scan QR code", onBack = onBack) {
        InfoBanner("Scan a profile QR to chat, or a device-link QR to approve a companion.")
        if (error != null) {
            Text(
                error!!,
                color = AivoraColors.Danger,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AivoraColors.SurfaceMuted),
            contentAlignment = Alignment.Center,
        ) {
            when {
                busy -> CircularProgressIndicator(color = AivoraColors.Primary)
                !camGranted -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text(
                            "Camera permission is needed for live scan.",
                            color = AivoraColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton(
                            text = "Allow camera",
                            onClick = { askCam.launch(Manifest.permission.CAMERA) },
                        )
                    }
                }
                else -> {
                    val lastAttempt = remember { AtomicLong(0L) }
                    val previewView = remember {
                        PreviewView(context).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    }
                    DisposableEffect(lifecycleOwner, previewView) {
                        val analysisExecutor = Executors.newSingleThreadExecutor()
                        val future = ProcessCameraProvider.getInstance(context)
                        val mainExec = ContextCompat.getMainExecutor(context)
                        val listener = Runnable {
                            val provider = runCatching { future.get() }.getOrNull() ?: return@Runnable
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val analysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            analysis.setAnalyzer(analysisExecutor) { image ->
                                try {
                                    if (handled.get()) return@setAnalyzer
                                    val now = System.currentTimeMillis()
                                    if (now - lastAttempt.get() < 350L) return@setAnalyzer
                                    lastAttempt.set(now)
                                    val plane = image.planes.firstOrNull() ?: return@setAnalyzer
                                    val buf = plane.buffer
                                    val rowStride = plane.rowStride
                                    val w = image.width
                                    val h = image.height
                                    if (w <= 0 || h <= 0) return@setAnalyzer
                                    val y = ByteArray(w * h)
                                    if (rowStride == w) {
                                        buf.rewind()
                                        buf.get(y, 0, (w * h).coerceAtMost(buf.remaining()))
                                    } else {
                                        var offset = 0
                                        for (row in 0 until h) {
                                            buf.position(row * rowStride)
                                            buf.get(y, offset, w)
                                            offset += w
                                        }
                                    }
                                    val text = QrCodec.decodeLuma(y, w, h)
                                    if (text != null) {
                                        previewView.post { onPayload(text) }
                                    }
                                } finally {
                                    image.close()
                                }
                            }
                            runCatching {
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    analysis,
                                )
                            }
                        }
                        future.addListener(listener, mainExec)
                        onDispose {
                            runCatching {
                                if (future.isDone) future.get().unbindAll()
                            }
                            analysisExecutor.shutdown()
                        }
                    }
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        PrimaryButton(
            text = "Choose image",
            onClick = { pick.launch("image/*") },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun OpenUserLinkScreen(
    userId: String,
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                SessionStore.accessToken(context)
            },
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (SessionStore.accessToken(context).isNullOrBlank()) {
            error = "Sign in to open this profile"
            return@LaunchedEffect
        }
        if (userId.isBlank()) {
            error = "Invalid link"
            return@LaunchedEffect
        }
        if (userId == SessionStore.userId(context)) {
            error = "That’s your own QR code"
            return@LaunchedEffect
        }
        runCatching {
            api.getUsersId(userId)
            val conv = api.postConversationsDirect(DirectBody(userId = userId))
            InMemoryChatStore.upsertChat(conv.toLocalChat())
            conv.id.orEmpty()
        }.onSuccess { chatId ->
            if (chatId.isBlank()) {
                error = "Could not open chat"
            } else {
                onNavigate(Routes.ChatId.replace("{id}", chatId))
            }
        }.onFailure {
            error = com.aivora.app.data.api.ApiErrors.message(it, "Could not open chat")
        }
    }

    AivoraScaffold(title = "Open chat", onBack = onBack) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            if (error != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = AivoraColors.Danger, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    LinkButton(text = "Back", onClick = { onBack?.invoke() })
                }
            } else {
                CircularProgressIndicator(color = AivoraColors.Primary)
            }
        }
    }
}
