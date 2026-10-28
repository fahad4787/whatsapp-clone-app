package com.aivora.app.ui.feature.chat

import com.aivora.app.ui.icons.AppIcons

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.LocalMediaKind
import com.aivora.app.data.local.LocalMessage
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.AivoraNoOverscroll
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.media.rememberSizedImageModel
import com.aivora.app.ui.theme.AivoraColors

private val tabs = listOf("Media", "Docs", "Links")

@Composable
fun ChatMediaScreen(
    chatId: String,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    var tab by remember { mutableIntStateOf(0) }
    fun seedFromChat(tabIndex: Int): List<LocalMessage> {
        val msgs = com.aivora.app.data.local.InMemoryChatStore.messages(chatId).value
        return when (tabIndex) {
            0 -> msgs.filter {
                it.mediaKind == LocalMediaKind.Image ||
                    it.mediaKind == LocalMediaKind.Video ||
                    it.mediaKind == LocalMediaKind.Audio
            }.asReversed()
            1 -> msgs.filter { it.mediaKind == LocalMediaKind.File }.asReversed()
            else -> msgs.filter { it.linkPreview != null }.asReversed()
        }
    }
    val initialItems = remember(chatId, tab) {
        com.aivora.app.data.local.InMemoryMediaGalleryStore.get(chatId, tab) ?: seedFromChat(tab)
    }
    var items by remember(chatId, tab) { mutableStateOf(initialItems) }
    var loading by remember(chatId, tab) {
        mutableStateOf(initialItems.isEmpty() && useRemote)
    }

    LaunchedEffect(chatId, tab, useRemote) {
        val warm = com.aivora.app.data.local.InMemoryMediaGalleryStore.get(chatId, tab)
            ?: seedFromChat(tab).takeIf { it.isNotEmpty() }
        if (warm != null) {
            items = warm
            loading = false
        } else if (useRemote) {
            loading = true
        }
        if (!useRemote) {
            items = warm.orEmpty()
            loading = false
            return@LaunchedEffect
        }
        val uid = SessionStore.userId(context).orEmpty()
        val fresh = runCatching {
            when (tab) {
                0 -> api.getConversationsIdMedia(chatId, type = "all", limit = 80)
                    .items
                    .map { it.toLocalMessage(uid) }
                    .filter {
                        it.mediaKind == LocalMediaKind.Image ||
                            it.mediaKind == LocalMediaKind.Video ||
                            it.mediaKind == LocalMediaKind.Audio
                    }
                1 -> api.getConversationsIdMedia(chatId, type = "docs", limit = 80)
                    .items
                    .map { it.toLocalMessage(uid) }
                else -> api.getConversationsIdMessages(chatId, limit = 100)
                    .items
                    .map { it.toLocalMessage(uid) }
                    .filter { it.linkPreview != null }
            }
        }.getOrDefault(warm.orEmpty())
        com.aivora.app.data.local.InMemoryMediaGalleryStore.put(chatId, tab, fresh)
        items = fresh
        loading = false
    }

    AivoraScaffold(title = "Media, links, and docs", onBack = onBack) {
        ScrollableTabRow(
            selectedTabIndex = tab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            tabs.forEachIndexed { i, label ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = { Text(label) },
                )
            }
        }
        when {
            loading -> Text(
                "Loading…",
                modifier = Modifier.padding(24.dp),
                color = AivoraColors.TextSecondary,
            )
            items.isEmpty() -> Text(
                "Nothing here yet",
                modifier = Modifier.padding(24.dp),
                color = AivoraColors.TextSecondary,
            )
            tab == 1 || tab == 2 -> {
                Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    items.forEach { msg ->
                        val title = when (tab) {
                            1 -> msg.fileName ?: msg.body
                            else -> msg.linkPreview?.title ?: msg.linkPreview?.url ?: msg.body
                        }
                        val subtitle = when (tab) {
                            1 -> msg.body
                            else -> msg.linkPreview?.site ?: msg.linkPreview?.url.orEmpty()
                        }
                        RowDoc(
                            title = title,
                            subtitle = subtitle,
                            isDoc = tab == 1,
                            onClick = {
                                val url = if (tab == 1) msg.mediaUri else msg.linkPreview?.url
                                if (!url.isNullOrBlank()) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
            else -> {
                val cellDp = (LocalConfiguration.current.screenWidthDp.dp - 24.dp) / 3
                AivoraNoOverscroll {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items, key = { it.id }, contentType = { "media" }) { msg ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AivoraColors.PrimarySoft)
                                .clickable {
                                    msg.mediaUri?.let { url ->
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                                            )
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            when (msg.mediaKind) {
                                LocalMediaKind.Image, LocalMediaKind.Video -> {
                                    AsyncImage(
                                        model = rememberSizedImageModel(
                                            data = msg.mediaUri,
                                            width = cellDp,
                                            height = cellDp,
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                    if (msg.mediaKind == LocalMediaKind.Video) {
                                        Icon(
                                            AppIcons.PlayArrow,
                                            contentDescription = "Video",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                                LocalMediaKind.Audio -> Text("♪", color = AivoraColors.Primary)
                                else -> Icon(
                                    AppIcons.Description,
                                    contentDescription = null,
                                    tint = AivoraColors.Primary,
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun RowDoc(
    title: String,
    subtitle: String,
    isDoc: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle.isNotBlank() && subtitle != title) {
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = AivoraColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isDoc) {
            // keep layout stable
        }
    }
}
