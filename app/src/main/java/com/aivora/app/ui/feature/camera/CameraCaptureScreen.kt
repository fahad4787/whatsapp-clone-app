package com.aivora.app.ui.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.PendingMedia
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.sendImageMessage
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.navigation.Routes
import kotlinx.coroutines.launch

/**
 * Transparent launcher for the system camera — no in-app dummy preview.
 *
 * @param target `"status"` or `"chat:{id}"`
 */
@Composable
fun CameraCaptureScreen(
    target: String = "status",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var launched by remember { mutableStateOf(false) }
    val media = rememberMediaActions(
        context = context,
        onCancel = { onBack?.invoke() },
    ) { uri ->
        when {
            target.startsWith("chat:") -> {
                val chatId = target.removePrefix("chat:")
                val token = SessionStore.accessToken(context)
                if (!token.isNullOrBlank()) {
                    scope.launch {
                        try {
                            val sent = api.sendImageMessage(context, chatId, uri)
                            val uid = SessionStore.userId(context)
                            if (uid != null) {
                                InMemoryChatStore.upsertMessage(
                                    chatId,
                                    sent.toLocalMessage(uid).copy(mediaUri = uri.toString()),
                                )
                            }
                        } catch (_: Exception) {
                            InMemoryChatStore.sendMedia(chatId, uri.toString())
                        } finally {
                            onBack?.invoke()
                        }
                    }
                } else {
                    InMemoryChatStore.sendMedia(chatId, uri.toString())
                    onBack?.invoke()
                }
            }
            else -> {
                PendingMedia.set(uri.toString())
                onNavigate(Routes.NewStatus)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!launched) {
            launched = true
            media.takePhoto()
        }
    }

    // Blank while the system camera is opening / returning.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    )
}
