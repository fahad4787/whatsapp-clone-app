package com.aivora.app.ui.feature.locked

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ChatLockPinBody
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.EmptyState
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun LockedChatsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    val allChats by InMemoryChatStore.chats.collectAsState()
    val locked = remember(allChats) { allChats.filter { it.locked } }
    var unlocked by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    AivoraScaffold(title = "Locked chats", onBack = onBack) {
        if (!unlocked) {
            InfoBanner("Enter your secret code to open locked chats.")
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.length <= 8 && it.all(Char::isDigit)) pin = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = { Text("Secret code") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
            )
            if (error != null) {
                Text(
                    error!!,
                    color = AivoraColors.Danger,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            PrimaryButton(
                text = if (busy) "…" else "Unlock",
                onClick = {
                    if (!busy && pin.length >= 4) {
                        busy = true
                        error = null
                        scope.launch {
                            runCatching {
                                api.postMeChatLockUnlock(ChatLockPinBody(pin = pin))
                            }.onSuccess {
                                unlocked = true
                                pin = ""
                            }.onFailure {
                                error = "Wrong code"
                            }
                            busy = false
                        }
                    }
                },
                modifier = Modifier.padding(16.dp),
            )
            PrimaryButton(
                "Open chat lock settings",
                onClick = { onNavigate(Routes.SettingsPrivacyChatLock) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        } else if (locked.isEmpty()) {
            EmptyState("No locked chats", "Lock a chat from the chat menu.")
            PrimaryButton(
                "Open chat lock settings",
                onClick = { onNavigate(Routes.SettingsPrivacyChatLock) },
                modifier = Modifier.padding(16.dp),
            )
        } else {
            InfoBanner("Unlocked for this session.")
            locked.forEach { chat ->
                PersonRow(
                    chat.title,
                    chat.listPreview,
                    onClick = { onNavigate(Routes.ChatId.replace("{id}", chat.id)) },
                )
            }
        }
    }
}
