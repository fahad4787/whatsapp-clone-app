package com.aivora.app.ui.feature.calls

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.EmptyRequestBody
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

/**
 * Joins a Wave 19 call link via `POST /api/call-links/:code`, then LiveKit.
 */
@Composable
fun JoinCallLinkScreen(
    code: String,
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val api = remember { ChatRepository.api() }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(code, useRemote) {
        if (!useRemote) {
            error = "Sign in to join this call"
            return@LaunchedEffect
        }
        if (code.isBlank()) {
            error = "Invalid link"
            return@LaunchedEffect
        }
        runCatching { api.postCallLinksCode(code, EmptyRequestBody()) }
            .onSuccess { join ->
                val token = join.token
                val url = join.livekitUrl
                val callId = join.call?.id
                if (token.isNullOrBlank() || url.isNullOrBlank() || callId.isNullOrBlank()) {
                    error = "Could not join call"
                    return@onSuccess
                }
                val media = join.media?.lowercase() ?: if (join.audio) "audio" else "video"
                val peer = join.title
                    ?: join.host?.displayName
                    ?: join.host?.phone
                    ?: "Call link"
                ActiveCallSession.setActive(
                    callId = callId,
                    token = token,
                    livekitUrl = url,
                    media = media,
                    peerName = peer,
                    asCaller = false,
                )
                onNavigate(
                    if (media == "video") Routes.videoCall(peer) else Routes.audioCall(peer),
                )
            }
            .onFailure {
                error = when {
                    it.message?.contains("410") == true -> "This link has expired"
                    it.message?.contains("404") == true -> "Link not found"
                    else -> it.message ?: "Could not join"
                }
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
    }

    AivoraScaffold(title = "Joining call", onBack = onBack) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (error != null) {
                Text(
                    error!!,
                    color = AivoraColors.TextSecondary,
                    modifier = Modifier.padding(24.dp),
                )
            } else {
                CircularProgressIndicator(color = AivoraColors.Primary)
            }
        }
    }
}
