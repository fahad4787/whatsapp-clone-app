package com.aivora.app.ui.feature.status

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aivora.app.data.StatusRepository
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.StatusViewerDto
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun StatusViewerScreen(
    statusId: String,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val status = remember(statusId) { InMemoryStatusStore.get(statusId) }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var showViewers by remember { mutableStateOf(false) }
    var viewers by remember { mutableStateOf<List<StatusViewerDto>>(emptyList()) }

    LaunchedEffect(statusId) {
        if (status != null && !status.mine && useRemote) {
            StatusRepository.markViewed(api, statusId)
        } else if (status != null && !status.mine) {
            InMemoryStatusStore.markViewed(statusId)
        }
    }

    val bg = remember(status?.background) {
        parseStatusColor(status?.background) ?: AivoraColors.Primary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (status?.mediaUri == null) bg else Color.Black)
            .clickable { onBack?.invoke() },
    ) {
        if (status == null) {
            Text(
                "Status unavailable",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            status.mediaUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Status photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (status.mediaUri == null) {
                    Text(
                        status.text,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else if (status.text.isNotBlank() && status.text != "Photo") {
                    Text(
                        status.text,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    buildString {
                        append(status.authorName)
                        append(" · ")
                        append(status.timeLabel)
                        if (status.mine) append(" · ${status.viewCount} views")
                    },
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(enabled = status.mine && useRemote) {
                        scope.launch {
                            runCatching { api.getStatusIdViewers(statusId).items }
                                .onSuccess {
                                    viewers = it
                                    showViewers = true
                                }
                                .onFailure {
                                    Toast.makeText(context, "Could not load viewers", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                )
            }
        }
        IconButton(
            onClick = { onBack?.invoke() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        if (status?.mine == true && useRemote) {
            IconButton(
                onClick = {
                    scope.launch {
                        val ok = StatusRepository.delete(context, api, statusId)
                        if (ok) onBack?.invoke()
                        else Toast.makeText(context, "Could not delete", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }
    }

    if (showViewers) {
        AlertDialog(
            onDismissRequest = { showViewers = false },
            title = { Text("Viewed by") },
            text = {
                Column {
                    if (viewers.isEmpty()) {
                        Text("No viewers yet")
                    } else {
                        viewers.forEach { v ->
                            val name = v.user?.displayName?.takeIf { it.isNotBlank() }
                                ?: v.user?.phone
                                ?: "Contact"
                            Text(
                                if (v.reaction.isNullOrBlank()) name else "$name · ${v.reaction}",
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViewers = false }) { Text("OK") }
            },
        )
    }
}

private fun parseStatusColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    val cleaned = hex.removePrefix("#")
    return runCatching {
        val value = cleaned.toLong(16)
        if (cleaned.length <= 6) Color(0xFF000000 or value) else Color(value)
    }.getOrNull()
}
