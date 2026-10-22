package com.aivora.app.ui.feature.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.GroupCreateBody
import com.aivora.app.data.model.UserDto
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SearchField
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
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var query by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var people by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        try {
            people = api.getPeopleSearch(query.ifBlank { null }).items
        } catch (e: Exception) {
            error = e.message ?: "Could not load people"
        }
    }

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
            "Choose the people you want in this group.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        if (error != null) {
            Text(
                error!!,
                color = AivoraColors.Danger,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f), RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(people, key = { it.id.orEmpty() }) { user ->
                    val id = user.id.orEmpty()
                    val name = user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.phoneDisplay
                        ?: user.phone
                        ?: "User"
                    val about = user.about ?: user.phone.orEmpty()
                    val isOn = id in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (isOn) selected - id else selected + id
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(name, size = 40)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(about, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ThemeSelectCircle(selected = isOn)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 66.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.35f),
                    )
                }
            }
        }
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

@Composable
private fun ThemeSelectCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(AivoraColors.Primary)
                } else {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = AivoraColors.OnPrimary, modifier = Modifier.size(14.dp))
        }
    }
}
