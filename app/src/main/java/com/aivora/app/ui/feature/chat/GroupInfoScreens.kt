package com.aivora.app.ui.feature.chat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ConversationDto
import com.aivora.app.data.model.GroupInfoBody
import com.aivora.app.data.model.GroupMemberRoleBody
import com.aivora.app.data.model.GroupMembersBody
import com.aivora.app.data.model.GroupPrivacyBody
import com.aivora.app.data.model.MemberDto
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.AivoraSheetAction
import com.aivora.app.ui.components.AivoraSheetActionList
import com.aivora.app.ui.components.PeopleSelectList
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.SettingsCard
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.rememberPeopleSearch
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun GroupInfoScreen(
    chatId: String,
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ChatRepository.api() }
    val myId = remember { SessionStore.userId(context) }
    var conv by remember { mutableStateOf<ConversationDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var editOpen by remember { mutableStateOf(false) }
    var addOpen by remember { mutableStateOf(false) }
    var memberTarget by remember { mutableStateOf<MemberDto?>(null) }
    var confirmLeave by remember { mutableStateOf(false) }
    var confirmDissolve by remember { mutableStateOf(false) }

    fun applyConv(next: ConversationDto) {
        conv = next
        InMemoryChatStore.upsertChat(next.toLocalChat())
    }

    val pickAvatar = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: error("Could not read image")
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val part = MultipartBody.Part.createFormData(
                    "file",
                    "group.jpg",
                    bytes.toRequestBody(mime.toMediaTypeOrNull()),
                )
                api.postConversationsIdAvatar(chatId, part)
            }.onSuccess { applyConv(it) }
                .onFailure {
                    Toast.makeText(context, "Could not update photo", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(chatId) {
        runCatching { api.getConversationsId(chatId) }
            .onSuccess { applyConv(it) }
            .onFailure { error = "Could not load group" }
    }

    val title = conv?.title?.takeIf { it.isNotBlank() }
        ?: InMemoryChatStore.chat(chatId).title
    val description = conv?.description.orEmpty()
    val members = conv?.members.orEmpty()
    val isAdmin = conv?.myRole.equals("admin", ignoreCase = true)
    val canEditInfo = isAdmin || !(conv?.onlyAdminsCanEditInfo ?: false)

    AivoraScaffold(title = "Group info", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Avatar(title, size = 96)
                Spacer(Modifier.height(12.dp))
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${members.size} members",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = AivoraColors.Danger)
                }
            }

            SettingsCard(
                items = buildList {
                    add(
                        SettingsItem(
                            title = "Media, links, and docs",
                            icon = Icons.Default.Photo,
                            onClick = { onNavigate(Routes.chatMedia(chatId)) },
                        ),
                    )
                    if (canEditInfo) {
                        add(
                            SettingsItem(
                                title = "Edit group info",
                                icon = Icons.Default.Edit,
                                onClick = { editOpen = true },
                            ),
                        )
                        add(
                            SettingsItem(
                                title = "Change group photo",
                                icon = Icons.Default.CameraAlt,
                                onClick = { pickAvatar.launch("image/*") },
                            ),
                        )
                    }
                    if (isAdmin) {
                        add(
                            SettingsItem(
                                title = "Add participants",
                                icon = Icons.Default.PersonAdd,
                                onClick = { addOpen = true },
                            ),
                        )
                    }
                },
            )

            if (isAdmin && conv != null) {
                Text(
                    "Group permissions",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                PrivacyToggle(
                    title = "Only admins can send messages",
                    checked = conv!!.onlyAdminsCanSend,
                    onCheckedChange = { checked ->
                        scope.launch {
                            runCatching {
                                api.putConversationsIdPrivacy(
                                    chatId,
                                    GroupPrivacyBody(onlyAdminsCanSend = checked),
                                )
                            }.onSuccess { applyConv(it) }
                                .onFailure {
                                    Toast.makeText(context, "Could not update", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                )
                PrivacyToggle(
                    title = "Only admins can edit group info",
                    checked = conv!!.onlyAdminsCanEditInfo,
                    onCheckedChange = { checked ->
                        scope.launch {
                            runCatching {
                                api.putConversationsIdPrivacy(
                                    chatId,
                                    GroupPrivacyBody(onlyAdminsCanEditInfo = checked),
                                )
                            }.onSuccess { applyConv(it) }
                                .onFailure {
                                    Toast.makeText(context, "Could not update", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                )
            }

            Text(
                "Members",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            members.forEach { member ->
                val name = member.user?.displayName?.takeIf { it.isNotBlank() }
                    ?: member.user?.phoneDisplay
                    ?: member.user?.phone
                    ?: "Member"
                val role = member.role.orEmpty()
                val uid = member.user?.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isAdmin && uid != null && uid != myId) {
                            memberTarget = member
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(name, size = 44)
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(name, fontWeight = FontWeight.Medium)
                        if (role.equals("admin", ignoreCase = true)) {
                            Text("Admin", color = AivoraColors.Primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (uid == myId) {
                        Text("You", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(color = AivoraColors.ChatDivider)
            }

            SettingsCard(
                items = buildList {
                    add(
                        SettingsItem(
                            title = "Exit group",
                            icon = Icons.Default.ExitToApp,
                            danger = true,
                            onClick = { confirmLeave = true },
                        ),
                    )
                    if (isAdmin) {
                        add(
                            SettingsItem(
                                title = "Delete group",
                                icon = Icons.Default.Delete,
                                danger = true,
                                onClick = { confirmDissolve = true },
                            ),
                        )
                    }
                },
            )
        }
    }

    if (editOpen && conv != null) {
        var titleEdit by remember(conv!!.id) { mutableStateOf(conv!!.title.orEmpty()) }
        var descEdit by remember(conv!!.id) { mutableStateOf(conv!!.description.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editOpen = false },
            title = { Text("Edit group info") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleEdit,
                        onValueChange = { titleEdit = it },
                        label = { Text("Group name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = descEdit,
                        onValueChange = { descEdit = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            runCatching {
                                api.putConversationsIdInfo(
                                    chatId,
                                    GroupInfoBody(
                                        title = titleEdit.trim().ifBlank { null },
                                        description = descEdit.trim(),
                                    ),
                                )
                            }.onSuccess {
                                applyConv(it)
                                editOpen = false
                            }.onFailure {
                                Toast.makeText(context, "Could not save", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editOpen = false }) { Text("Cancel") }
            },
        )
    }

    if (addOpen) {
        AddParticipantsSheet(
            excludeIds = members.mapNotNull { it.user?.id }.toSet(),
            onDismiss = { addOpen = false },
            onAdd = { ids ->
                scope.launch {
                    runCatching {
                        api.postConversationsIdMembers(chatId, GroupMembersBody(ids))
                    }.onSuccess {
                        applyConv(it)
                        addOpen = false
                        Toast.makeText(context, "Participants added", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "Could not add", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }

    memberTarget?.let { target ->
        val name = target.user?.displayName?.takeIf { !it.isNullOrBlank() }
            ?: target.user?.phone
            ?: "Member"
        val uid = target.user?.id ?: return@let
        val isTargetAdmin = target.role.equals("admin", ignoreCase = true)
        AivoraBottomSheet(onDismiss = { memberTarget = null }, title = name) {
            AivoraSheetActionList(
                actions = buildList {
                    add(
                        AivoraSheetAction(
                            if (isTargetAdmin) "Dismiss as admin" else "Make group admin",
                            Icons.Default.Edit,
                        ) {
                            memberTarget = null
                            scope.launch {
                                runCatching {
                                    api.putConversationsIdMembersUserId(
                                        chatId,
                                        uid,
                                        GroupMemberRoleBody(if (isTargetAdmin) "member" else "admin"),
                                    )
                                }.onSuccess { applyConv(it) }
                                    .onFailure {
                                        Toast.makeText(context, "Could not update role", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                    )
                    add(
                        AivoraSheetAction("Remove from group", Icons.Default.Delete, danger = true) {
                            memberTarget = null
                            scope.launch {
                                runCatching {
                                    api.deleteConversationsIdMembersUserId(chatId, uid)
                                }.onSuccess { applyConv(it) }
                                    .onFailure {
                                        Toast.makeText(context, "Could not remove", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                    )
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Exit group?") },
            text = { Text("You will no longer receive messages from this group.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLeave = false
                        scope.launch {
                            runCatching { api.postConversationsIdLeave(chatId) }
                                .onSuccess {
                                    InMemoryChatStore.removeChat(chatId, context)
                                    onNavigate(Routes.Home)
                                }
                                .onFailure {
                                    Toast.makeText(context, "Could not leave", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                ) { Text("Exit", color = AivoraColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLeave = false }) { Text("Cancel") }
            },
        )
    }

    if (confirmDissolve) {
        AlertDialog(
            onDismissRequest = { confirmDissolve = false },
            title = { Text("Delete group?") },
            text = { Text("This removes the group for everyone. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDissolve = false
                        scope.launch {
                            runCatching { api.deleteConversationsId(chatId) }
                                .onSuccess {
                                    InMemoryChatStore.removeChat(chatId, context)
                                    onNavigate(Routes.Home)
                                }
                                .onFailure {
                                    Toast.makeText(context, "Could not delete", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                ) { Text("Delete", color = AivoraColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDissolve = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PrivacyToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AivoraColors.Primary,
                checkedThumbColor = Color.White,
            ),
        )
    }
}

@Composable
private fun AddParticipantsSheet(
    excludeIds: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (List<String>) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val search = rememberPeopleSearch(query, excludeIds)

    AivoraBottomSheet(onDismiss = onDismiss, title = "Add participants") {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SearchField(value = query, onValueChange = { query = it }, placeholder = "Search people")
            Spacer(Modifier.height(8.dp))
            PeopleSelectList(
                people = search.people,
                selectedIds = selected,
                onToggle = { id ->
                    selected = if (id in selected) selected - id else selected + id
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (selected.isEmpty()) "Add" else "Add (${selected.size})",
                enabled = selected.isNotEmpty(),
                onClick = { onAdd(selected.toList()) },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
