package com.aivora.app.ui.feature.channels

import com.aivora.app.ui.icons.AppIcons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ChannelCreateBody
import com.aivora.app.data.model.ChannelDto
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.AivoraScrollColumn
import com.aivora.app.ui.components.EmptyState
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun ChannelsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var query by remember { mutableStateOf("") }
    var channels by remember { mutableStateOf<List<ChannelDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }

    fun reload(q: String = query) {
        scope.launch {
            loading = channels.isEmpty()
            runCatching { api.getChannels(q = q.takeIf { it.isNotBlank() }) }
                .onSuccess {
                    channels = it.items
                    error = null
                }
                .onFailure { error = it.message ?: "Could not load channels" }
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    AivoraScaffold(
        title = "Channels",
        onBack = onBack,
        fab = {
            FloatingActionButton(
                onClick = {
                    if (creating) return@FloatingActionButton
                    creating = true
                    scope.launch {
                        runCatching {
                            api.postChannels(
                                ChannelCreateBody(
                                    name = "My channel",
                                    listed = true,
                                ),
                            )
                        }.onSuccess { ch ->
                            val slug = ch.slug ?: return@onSuccess
                            onNavigate(Routes.ChannelSlug.replace("{slug}", slug))
                        }.onFailure {
                            android.widget.Toast.makeText(
                                context,
                                it.message ?: "Create failed",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                        creating = false
                    }
                },
                containerColor = AivoraColors.Primary,
                contentColor = AivoraColors.OnPrimary,
            ) {
                Icon(AppIcons.Add, null)
            }
        },
    ) {
        InfoBanner("Find channels")
        SearchField(
            query,
            {
                query = it
                reload(it)
            },
            "Search channels",
        )
        when {
            loading -> com.aivora.app.ui.components.LoadingState()
            error != null && channels.isEmpty() -> EmptyState("Couldn’t load", error!!)
            channels.isEmpty() -> EmptyState("No channels match.", "Try a different search.")
            else -> AivoraScrollColumn(modifier = Modifier.fillMaxSize()) {
                channels.forEach { ch ->
                    val name = ch.name ?: ch.slug ?: "Channel"
                    val slug = ch.slug ?: return@forEach
                    val subtitle = buildString {
                        if (ch.verified) append("Verified · ")
                        append("${ch.followerCount} followers")
                        if (ch.following) append(" · Following")
                    }
                    key(ch.id ?: slug) {
                        PersonRow(
                            name,
                            subtitle,
                            onClick = {
                                onNavigate(Routes.ChannelSlug.replace("{slug}", slug))
                            },
                        )
                    }
                }
            }
        }
    }
}
