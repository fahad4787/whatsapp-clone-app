package com.aivora.app.ui.feature.status

import com.aivora.app.ui.icons.AppIcons

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraNoOverscroll
import com.aivora.app.ui.media.rememberViewportImageModel
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val STATUS_DURATION_MS = 5_000

@Composable
fun StatusViewerScreen(
    statusId: String,
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Pin author from the entry status — deleting that id must not wipe the queue.
    val entry = remember(statusId) { InMemoryStatusStore.get(statusId) }
    val authorId = remember(statusId) { entry?.authorId }
    val mineFeed = remember(statusId) { entry?.mine == true }
    val allStatuses by InMemoryStatusStore.statuses.collectAsState()
    val queue = remember(authorId, mineFeed, allStatuses) {
        if (authorId == null) emptyList()
        else InMemoryStatusStore.statusesForAuthor(authorId, mineFeed)
    }
    val startIndex = remember(statusId) {
        InMemoryStatusStore.statusesForAuthor(authorId.orEmpty(), mineFeed)
            .indexOfFirst { it.id == statusId }
            .coerceAtLeast(0)
    }
    var index by remember(statusId) { mutableIntStateOf(startIndex) }
    var exiting by remember { mutableStateOf(false) }

    val queueKey = remember(queue) { "${queue.size}:${queue.joinToString(",") { it.id }}" }
    LaunchedEffect(queueKey) {
        if (exiting) return@LaunchedEffect
        if (queue.isEmpty()) {
            exiting = true
            onBack?.invoke()
            return@LaunchedEffect
        }
        if (index > queue.lastIndex) index = queue.lastIndex
    }

    var paused by remember { mutableStateOf(false) }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var showViewers by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var viewers by remember { mutableStateOf<List<StatusViewerDto>>(emptyList()) }
    val progress = remember { Animatable(0f) }

    val current: LocalStatus? = queue.getOrNull(index)
    val isTextStatus = current != null && current.mediaUri.isNullOrBlank()
    val currentId = current?.id

    fun goNext() {
        if (index < queue.lastIndex) index += 1
        else {
            exiting = true
            onBack?.invoke()
        }
    }

    fun goPrev() {
        if (index > 0) index -= 1
        else {
            exiting = true
            onBack?.invoke()
        }
    }

    suspend fun deleteCurrent() {
        val item = current ?: return
        val deleteAt = index
        // Optimistic remove updates Updates tab immediately; advance or leave cleanly.
        val ok = StatusRepository.delete(context, api, item.id)
        if (!ok) {
            Toast.makeText(context, "Could not delete", Toast.LENGTH_SHORT).show()
            return
        }
        val remaining = InMemoryStatusStore.statusesForAuthor(item.authorId, mine = true)
        if (remaining.isEmpty()) {
            exiting = true
            onBack?.invoke()
        } else {
            index = deleteAt.coerceAtMost(remaining.lastIndex)
        }
    }

    fun openViewersSheet() {
        val item = current ?: return
        if (!item.mine || !useRemote) return
        paused = true
        scope.launch {
            runCatching { api.getStatusIdViewers(item.id).items }
                .onSuccess {
                    viewers = it
                    InMemoryStatusStore.setViewCount(item.id, it.size)
                    showViewers = true
                }
                .onFailure {
                    Toast.makeText(context, "Could not load viewers", Toast.LENGTH_SHORT).show()
                    paused = false
                }
        }
    }

    // Peer opens status → mark viewed immediately (owner gets WS view_count bump).
    LaunchedEffect(currentId) {
        val item = current ?: return@LaunchedEffect
        if (item.mine) return@LaunchedEffect
        if (useRemote) StatusRepository.markViewed(api, item.id)
        else InMemoryStatusStore.markViewed(item.id)
    }

    // Own status: one immediate refresh; poll only when WS down (WS is primary — PERF H3).
    LaunchedEffect(currentId, current?.mine) {
        val item = current ?: return@LaunchedEffect
        if (!item.mine || !useRemote) return@LaunchedEffect
        StatusRepository.refreshViewCount(api, item.id)
        while (true) {
            delay(15_000)
            if (com.aivora.app.data.realtime.RealtimeClient.connected.value) continue
            StatusRepository.refreshViewCount(api, item.id)
        }
    }

    LaunchedEffect(showViewers, currentId) {
        if (!showViewers || !useRemote) return@LaunchedEffect
        val id = currentId ?: return@LaunchedEffect
        // First open already fetched in openViewersSheet; slow refresh only.
        while (showViewers) {
            delay(20_000)
            if (!showViewers) break
            runCatching { api.getStatusIdViewers(id).items }
                .onSuccess {
                    viewers = it
                    InMemoryStatusStore.setViewCount(id, it.size)
                }
        }
    }

    // Auto-advance — restart only when the page id changes or pause lifts.
    // Capture currentId so a late completion cannot skip an extra page after a tap.
    LaunchedEffect(currentId, paused) {
        val page = currentId ?: return@LaunchedEffect
        if (paused) {
            progress.stop()
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = STATUS_DURATION_MS, easing = LinearEasing),
        )
        if (currentId == page) goNext()
    }

    val bg = remember(current?.background) {
        parseStatusColor(current?.background) ?: AivoraColors.Primary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTextStatus) bg else Color.Black),
    ) {
        if (exiting || current == null || queue.isEmpty()) {
            // Quiet exit after delete / end of reel — no "unavailable" flash.
        } else {
            if (!isTextStatus) {
                current.mediaUri?.let { uri ->
                    AsyncImage(
                        model = rememberViewportImageModel(uri),
                        contentDescription = "Status photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            } else {
                Text(
                    current.text,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(vertical = 96.dp),
                )
            }

            // Tap zones: left = previous, right = next. Hold pauses the timer.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentId, queue.size) {
                        detectTapGestures(
                            onPress = {
                                paused = true
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    paused = false
                                }
                            },
                            onTap = { offset ->
                                if (offset.x < size.width * 0.35f) goPrev() else goNext()
                            },
                        )
                    },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                // Isolated so Animatable ticks don't recompose the whole viewer.
                StatusProgressRow(
                    count = queue.size,
                    index = index,
                    progress = progress,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(
                            AppIcons.ArrowBack,
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
                    if (current.mine) {
                        Box {
                            IconButton(
                                onClick = {
                                    paused = true
                                    menuOpen = true
                                },
                            ) {
                                Icon(AppIcons.MoreVert, contentDescription = "More", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = {
                                    menuOpen = false
                                    paused = false
                                },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Viewers (${current.viewCount})") },
                                    leadingIcon = { Icon(AppIcons.Visibility, null) },
                                    onClick = {
                                        menuOpen = false
                                        openViewersSheet()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Status privacy") },
                                    leadingIcon = { Icon(AppIcons.Lock, null) },
                                    onClick = {
                                        menuOpen = false
                                        paused = false
                                        onNavigate(Routes.SettingsPrivacyStatus)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color(0xFFD32F2F)) },
                                    leadingIcon = {
                                        Icon(AppIcons.Delete, null, tint = Color(0xFFD32F2F))
                                    },
                                    onClick = {
                                        menuOpen = false
                                        showDeleteConfirm = true
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (!isTextStatus || current.mine) {
                var swipeAccum by remember(currentId) { mutableFloatStateOf(0f) }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .then(
                            if (current.mine) {
                                Modifier.pointerInput(currentId) {
                                    detectVerticalDragGestures(
                                        onDragEnd = { swipeAccum = 0f },
                                        onDragCancel = { swipeAccum = 0f },
                                        onVerticalDrag = { _, dragAmount ->
                                            // Negative = swipe up (WhatsApp viewers sheet).
                                            swipeAccum += dragAmount
                                            if (swipeAccum < -80f) {
                                                swipeAccum = 0f
                                                openViewersSheet()
                                            }
                                        },
                                    )
                                }
                            } else {
                                Modifier
                            },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (
                        !isTextStatus &&
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
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    if (current.mine) {
                        Icon(
                            AppIcons.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(28.dp),
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .clickable { openViewersSheet() }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                AppIcons.Visibility,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                if (current.viewCount == 1) "1 view" else "${current.viewCount} views",
                                color = Color.White.copy(alpha = 0.95f),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Text(
                            "Swipe up for viewers",
                            color = Color.White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }

    if (showViewers) {
        AivoraBottomSheet(
            onDismiss = {
                showViewers = false
                paused = false
            },
            title = if (viewers.isEmpty()) "No views yet" else "Viewed by ${viewers.size}",
        ) {
            if (viewers.isEmpty()) {
                Text(
                    "When contacts view your status, they'll show up here.",
                    color = AivoraColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                )
            } else {
                AivoraNoOverscroll {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        viewers.forEach { v ->
                            val name = v.user?.displayName?.takeIf { it.isNotBlank() }
                                ?: v.user?.phone
                                ?: "Contact"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Avatar(name = name, size = 44)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = AivoraColors.Ink,
                                    )
                                    Text(
                                        formatViewerTime(v.seenAt ?: v.createdAt),
                                        fontSize = 12.sp,
                                        color = AivoraColors.TextSecondary,
                                    )
                                }
                                if (!v.reaction.isNullOrBlank()) {
                                    Text(v.reaction, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                paused = false
            },
            title = { Text("Delete status?") },
            text = { Text("This status update will be deleted for everyone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            deleteCurrent()
                            paused = false
                        }
                    },
                ) { Text("Delete", color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        paused = false
                    },
                ) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun StatusProgressRow(
    count: Int,
    index: Int,
    progress: Animatable<Float, AnimationVector1D>,
) {
    // Reading progress.value here confines per-frame recomposition to the bars only.
    val fraction = progress.value
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(count) { i ->
            val filled = when {
                i < index -> 1f
                i == index -> fraction
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
                        .fillMaxWidth(filled.coerceIn(0f, 1f))
                        .background(Color.White),
                )
            }
        }
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

private fun formatViewerTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "Just now"
    return runCatching {
        val instant = Instant.parse(iso)
        val local = instant.atZone(ZoneId.systemDefault())
        val now = Instant.now()
        val secs = java.time.Duration.between(instant, now).seconds
        when {
            secs < 60 -> "Just now"
            secs < 3600 -> "${secs / 60} min ago"
            local.toLocalDate() == now.atZone(ZoneId.systemDefault()).toLocalDate() ->
                "Today, " + local.format(DateTimeFormatter.ofPattern("h:mm a"))
            else -> local.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
        }
    }.getOrDefault("Just now")
}
