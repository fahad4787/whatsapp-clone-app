package com.aivora.app.ui.feature.chat

import com.aivora.app.ui.icons.AppIcons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.model.DirectBody
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PeopleTapList
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.rememberPeopleSearch
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
    val api = remember { ChatRepository.api() }
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val search = rememberPeopleSearch(query)

    AivoraScaffold(
        title = "New chat",
        onBack = onBack,
        fab = {
            FloatingActionButton(
                onClick = { onNavigate(Routes.NewGroup) },
                containerColor = AivoraColors.Primary,
                contentColor = AivoraColors.OnPrimary,
            ) { Icon(AppIcons.Add, contentDescription = null) }
        },
    ) {
        InfoBanner("Find anyone by their full phone number. Recent contacts are listed below.")
        SearchField(query, { query = it }, "Search name or phone number")
        val err = error ?: search.error
        if (err != null) {
            Text(err, color = AivoraColors.Danger, fontSize = 13.sp)
        }
        PeopleTapList(
            people = search.people,
            onPersonClick = { user ->
                if (busy) return@PeopleTapList
                val peerId = user.id ?: return@PeopleTapList
                busy = true
                scope.launch {
                    try {
                        val conv = api.postConversationsDirect(DirectBody(userId = peerId))
                        InMemoryChatStore.upsertChat(conv.toLocalChat())
                        onNavigate(Routes.ChatId.replace("{id}", conv.id.orEmpty()))
                    } catch (e: Exception) {
                        error = com.aivora.app.data.api.ApiErrors.message(e, "Could not start chat")
                    } finally {
                        busy = false
                    }
                }
            },
            header = {
                PersonRow(
                    name = "New group",
                    subtitle = "Create a group with friends",
                    onClick = { onNavigate(Routes.NewGroup) },
                    leadingIcon = com.aivora.app.ui.icons.AppIcons.GroupAdd,
                )
            },
            footer = {
                if (search.people.isEmpty() && !search.loading && err == null) {
                    val digits = query.count { it.isDigit() }
                    Text(
                        when {
                            query.isBlank() -> "No contacts yet.\nEnter a phone number above to find someone."
                            digits in 1..6 -> "Type the full phone number to find someone."
                            else -> "No one found. Check the number and try again."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 32.dp),
                        color = AivoraColors.TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            },
        )
    }
}
