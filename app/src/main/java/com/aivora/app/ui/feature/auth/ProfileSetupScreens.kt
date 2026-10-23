package com.aivora.app.ui.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.MeUpdateBody
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PrimaryButton
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupScreen(
    onBack: (() -> Unit)? = null,
    onFinished: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var name by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Hey there! I am using Aivora.") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        runCatching { api.getMe() }
            .onSuccess { me ->
                if (!me.displayName.isNullOrBlank()) {
                    // Already set up — don't force the form again.
                    onFinished()
                    return@LaunchedEffect
                }
                about = me.about?.takeIf { it.isNotBlank() } ?: about
            }
        loading = false
    }

    AivoraScaffold(title = "Profile setup", onBack = onBack) {
        if (loading) {
            Text("Loading…", modifier = Modifier.padding(16.dp))
            return@AivoraScaffold
        }
        InfoBanner("Add your name and a profile photo so friends can recognize you.")
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(name.ifBlank { "?" }, size = 96)
            Spacer(modifier = Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; error = null },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            label = { Text("Your name") },
            placeholder = { Text("Add your name") },
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            enabled = !busy,
        )
        OutlinedTextField(
            value = about,
            onValueChange = { about = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            label = { Text("About") },
            placeholder = { Text("Hey there! I am using Aivora.") },
            singleLine = true,
            enabled = !busy,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = if (busy) "Saving…" else "Finish",
            onClick = {
                if (name.isBlank()) {
                    error = "Name is required"
                    return@PrimaryButton
                }
                busy = true
                error = null
                scope.launch {
                    try {
                        api.putMe(
                            MeUpdateBody(
                                displayName = name.trim(),
                                about = about.trim().ifBlank { null },
                            ),
                        )
                        onFinished()
                    } catch (e: Exception) {
                        error = e.message ?: "Could not save profile"
                        busy = false
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            enabled = !busy,
        )
    }
}
