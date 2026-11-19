package com.aivora.app.ui.feature.channels

import com.aivora.app.ui.icons.AppIcons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ChannelDto
import com.aivora.app.data.model.ChannelPostDto
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.AivoraScrollColumn
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.LinkButton
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun ChannelSlugScreen(
    slug: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var channel by remember { mutableStateOf<ChannelDto?>(null) }
    var posts by remember { mutableStateOf<List<ChannelPostDto>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var composeOpen by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val title = channel?.name
        ?: slug.ifBlank { "channel" }.replace('-', ' ').replaceFirstChar { it.uppercase() }

    fun reload() {
        if (slug.isBlank() || slug == "new") return
        scope.launch {
            runCatching { api.getChannelsSlug(slug) }.onSuccess { channel = it }
            runCatching { api.getChannelsSlugPosts(slug) }.onSuccess { posts = it.items }
        }
    }

    LaunchedEffect(slug) { reload() }

    if (composeOpen) {
        AlertDialog(
            onDismissRequest = { if (!busy) composeOpen = false },
            title = { Text("New channel post") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write an update…") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (busy || draft.isBlank()) return@TextButton
                        busy = true
                        scope.launch {
                            runCatching {
                                api.postChannelsSlugPosts(slug, mapOf("text" to draft.trim()))
                            }.onSuccess {
                                draft = ""
                                composeOpen = false
                                reload()
                            }.onFailure {
                                android.widget.Toast.makeText(
                                    context,
                                    it.message ?: "Post failed",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            }
                            busy = false
                        }
                    },
                ) { Text(if (busy) "…" else "Post") }
            },
            dismissButton = {
                TextButton(onClick = { if (!busy) composeOpen = false }) { Text("Cancel") }
            },
        )
    }

    AivoraScaffold(
        title = title,
        onBack = onBack,
        actions = {
            IconButton(onClick = { }) { Icon(AppIcons.MoreVert, null) }
        },
        fab = {
            FloatingActionButton(
                onClick = { composeOpen = true },
                containerColor = AivoraColors.Primary,
                contentColor = AivoraColors.OnPrimary,
            ) {
                Icon(AppIcons.Edit, contentDescription = "New channel post")
            }
        },
    ) {
        InfoBanner(channel?.description ?: "Channel posts")
        AivoraScrollColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (posts.isEmpty()) {
                Text(
                    "No posts yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AivoraColors.TextSecondary,
                )
            }
            posts.forEach { p ->
                key(p.id ?: p.text) {
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(p.text.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${p.viewCount} views · ${p.reactions?.sumOf { it.count } ?: 0} reactions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LinkButton("👍 React") {
                            val id = p.id ?: return@LinkButton
                            scope.launch {
                                runCatching {
                                    api.postChannelsSlugPostsPostIdReaction(
                                        slug,
                                        id,
                                        mapOf("emoji" to "👍"),
                                    )
                                }.onSuccess { reload() }
                            }
                        }
                    }
                }
            }
            val following = channel?.following == true
            PrimaryButton(
                if (following) "Unfollow channel" else "Follow channel",
                onClick = {
                    if (busy) return@PrimaryButton
                    busy = true
                    scope.launch {
                        runCatching {
                            if (following) api.deleteChannelsSlugFollow(slug)
                            else api.postChannelsSlugFollow(slug)
                        }.onSuccess {
                            channel = it
                        }.onFailure {
                            android.widget.Toast.makeText(
                                context,
                                it.message ?: "Failed",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                        busy = false
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
            LinkButton("Report this channel") {
                scope.launch {
                    runCatching {
                        api.postChannelsSlugReport(slug, mapOf("reason" to "user_report"))
                    }.onSuccess {
                        android.widget.Toast.makeText(
                            context,
                            "Report sent",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }
}
