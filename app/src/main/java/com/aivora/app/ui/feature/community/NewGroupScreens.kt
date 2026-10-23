package com.aivora.app.ui.feature.community

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.model.GroupCreateBody
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.PeopleSelectList
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.rememberPeopleSearch
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun NewGroupScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ChatRepository.api() }
    var query by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val search = rememberPeopleSearch(query)

    AivoraScaffold(title = "New group", onBack = onBack) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            label = { Text("Group name") },
        )
        SearchField(query, { query = it }, "Search contacts")
        Text(
            if (selected.isEmpty()) {
                "Select one or more contacts for this group."
            } else {
                "${selected.size} selected"
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        val err = error ?: search.error
        if (err != null) {
            Text(
                err,
                color = AivoraColors.Danger,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PeopleSelectList(
            people = search.people,
            selectedIds = selected,
            onToggle = { id ->
                selected = if (id in selected) selected - id else selected + id
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimaryButton(
            text = if (busy) "Creating…" else "Create group",
            enabled = !busy && selected.isNotEmpty() && title.isNotBlank(),
            onClick = {
                busy = true
                error = null
                scope.launch {
                    try {
                        val conv = api.postConversationsGroup(
                            GroupCreateBody(
                                title = title.trim(),
                                memberIds = selected.toList(),
                            ),
                        )
                        InMemoryChatStore.upsertChat(conv.toLocalChat())
                        onNavigate(Routes.ChatId.replace("{id}", conv.id.orEmpty()))
                    } catch (e: Exception) {
                        error = e.message ?: "Could not create group"
                    } finally {
                        busy = false
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
