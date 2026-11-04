package com.aivora.app.ui.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aivora.app.data.local.PendingChatMedia
import com.aivora.app.data.local.PendingMedia
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.navigation.Routes

/**
 * Transparent launcher for the system camera — no in-app dummy preview.
 *
 * @param target `"status"` or `"chat:{id}"`
 * Chat photos hand off to [PendingChatMedia] so ChatScreen shows caption / view-once preview.
 */
@Composable
fun CameraCaptureScreen(
    target: String = "status",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var launched by remember { mutableStateOf(false) }
    val media = rememberMediaActions(
        context = context,
        onCancel = { onBack?.invoke() },
    ) { uri ->
        when {
            target.startsWith("chat:") -> {
                val chatId = target.removePrefix("chat:")
                PendingChatMedia.set(chatId, uri.toString())
                onBack?.invoke()
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
