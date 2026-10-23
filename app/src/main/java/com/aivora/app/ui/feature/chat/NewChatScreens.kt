package com.aivora.app.ui.feature.chat

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.DirectBody
import com.aivora.app.data.model.UserDto
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var query by remember { mutableStateOf("") }
    var people by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val q = query.trim()
        try {
            people = api.getPeopleSearch(q.ifBlank { null }, limit = 100).items
            error = null
        } catch (e: Exception) {
            error = e.message ?: "Could not load people"
        }
    }

    AivoraScaffold(
        title = "New chat",
        onBack = onBack,
        fab = {
            FloatingActionButton(
                onClick = { onNavigate(Routes.NewGroup) },
                containerColor = AivoraColors.Primary,
                contentColor = AivoraColors.OnPrimary,
            ) { Icon(Icons.Default.Add, contentDescription = null) }
        },
    ) {
        InfoBanner("All registered users — tap to chat, or filter with search")
        SearchField(query, { query = it }, "Search name or phone")
        if (error != null) {
            Text(error!!, color = AivoraColors.Danger, fontSize = 13.sp)
        }
        LazyColumn {
            item {
                PersonRow(
                    name = "New group",
                    subtitle = "Create a group with friends",
                    onClick = { onNavigate(Routes.NewGroup) },
                )
            }
            items(people, key = { it.id.orEmpty() }) { user ->
                val label = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.phoneDisplay
                    ?: user.phone
                    ?: "User"
                PersonRow(
                    name = label,
                    subtitle = user.phoneDisplay ?: user.phone ?: "",
                    onClick = {
                        if (busy) return@PersonRow
                        val peerId = user.id ?: return@PersonRow
                        busy = true
                        scope.launch {
                            try {
                                val conv = api.postConversationsDirect(DirectBody(userId = peerId))
                                InMemoryChatStore.upsertChat(conv.toLocalChat())
                                onNavigate(Routes.ChatId.replace("{id}", conv.id.orEmpty()))
                            } catch (e: Exception) {
                                error = e.message ?: "Could not start chat"
                            } finally {
                                busy = false
                            }
                        }
                    },
                )
            }
        }
    }
}
