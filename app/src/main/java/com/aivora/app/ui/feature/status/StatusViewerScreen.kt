package com.aivora.app.ui.feature.status

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.aivora.app.data.local.LocalStatus
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.StatusViewerDto
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

private const val STATUS_DURATION_MS = 5_000

@Composable
fun StatusViewerScreen(
    statusId: String,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val seed = remember(statusId) { InMemoryStatusStore.get(statusId) }
    val queue = remember(statusId, seed?.authorId, seed?.mine) {
        if (seed == null) emptyList()
        else InMemoryStatusStore.statusesForAuthor(seed.authorId, seed.mine)
    }
    val startIndex = remember(statusId, queue) {
        queue.indexOfFirst { it.id == statusId }.coerceAtLeast(0)
    }
    var index by remember(statusId) { mutableIntStateOf(startIndex) }
    var paused by remember { mutableStateOf(false) }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var showViewers by remember { mutableStateOf(false) }
    var viewers by remember { mutableStateOf<List<StatusViewerDto>>(emptyList()) }
    val progress = remember { Animatable(0f) }

    val current: LocalStatus? = queue.getOrNull(index)

    fun goNext() {
        if (index < queue.lastIndex) index += 1
        else onBack?.invoke()
    }

    fun goPrev() {
        if (index > 0) index -= 1
        else onBack?.invoke()
    }

    LaunchedEffect(current?.id) {
        val item = current ?: return@LaunchedEffect
        if (!item.mine && useRemote) {
            StatusRepository.markViewed(api, item.id)
        } else if (!item.mine) {
            InMemoryStatusStore.markViewed(item.id)
        }
    }

    LaunchedEffect(current?.id, paused) {
        if (current == null) return@LaunchedEffect
        if (paused) {
            progress.stop()
            return@LaunchedEffect
        }
        val from = progress.value
        if (from >= 0.999f) progress.snapTo(0f)
        val start = if (from in 0.01f..0.99f) from else 0f
        if (start == 0f) progress.snapTo(0f)
        val remaining = ((1f - start) * STATUS_DURATION_MS).toInt().coerceAtLeast(50)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = remaining, easing = LinearEasing),
        )
        goNext()
    }

    val bg = remember(current?.background) {
        parseStatusColor(current?.background) ?: AivoraColors.Primary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (current?.mediaUri == null) bg else Color.Black),
    ) {
        if (current == null || queue.isEmpty()) {
            Text(
                "Status unavailable",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            current.mediaUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Status photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(index) {
                        detectTapGestures(
                            onPress = {
                                paused = true
                                tryAwaitRelease()
                                paused = false
                            },
                            onTap = { offset ->
                                if (offset.x < size.width * 0.35f) goPrev() else goNext()
                            },
                        )
                    },
            ) {
                Spacer(Modifier.weight(0.35f).fillMaxHeight())
                Spacer(Modifier.weight(0.65f).fillMaxHeight())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    queue.forEachIndexed { i, _ ->
                        val fraction = when {
                            i < index -> 1f
                            i == index -> progress.value
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.35f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                    .background(Color.White),
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            current.authorName,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            current.timeLabel,
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    if (current.mine && useRemote) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val ok = StatusRepository.delete(context, api, current.id)
                                    if (!ok) {
                                        Toast.makeText(context, "Could not delete", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    val remaining = InMemoryStatusStore.statusesForAuthor(
                                        current.authorId,
                                        mine = true,
                                    )
                                    if (remaining.isEmpty()) onBack?.invoke()
                                    else index = index.coerceAtMost(remaining.lastIndex)
                                }
                            },
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (current.mediaUri == null) {
                    Text(
                        current.text,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (
                    current.text.isNotBlank() &&
                    current.text != "Photo" &&
                    current.text != "Video"
                ) {
                    Text(
                        current.text,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (current.mine) {
                    Text(
                        "${current.viewCount} views · tap to see",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.pointerInput(current.id) {
                            detectTapGestures {
                                if (!useRemote) return@detectTapGestures
                                scope.launch {
                                    runCatching { api.getStatusIdViewers(current.id).items }
                                        .onSuccess {
                                            viewers = it
                                            showViewers = true
                                        }
                                        .onFailure {
                                            Toast.makeText(
                                                context,
                                                "Could not load viewers",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                }
                            }
                        },
                    )
                }
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
