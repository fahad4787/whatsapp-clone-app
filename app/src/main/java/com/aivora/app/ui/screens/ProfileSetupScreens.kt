package com.aivora.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PrimaryButton

@Composable
fun ProfileSetupScreen(
    onBack: (() -> Unit)? = null,
    onFinished: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Hey there! I am using Aivora.") }
    var error by remember { mutableStateOf<String?>(null) }

    AivoraScaffold(title = "Profile setup", onBack = onBack) {
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
        )
        OutlinedTextField(
            value = about,
            onValueChange = { about = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            label = { Text("About") },
            placeholder = { Text("Hey there! I am using Aivora.") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = "Finish",
            onClick = {
                if (name.isBlank()) {
                    error = "Name is required"
                } else {
                    onFinished()
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
