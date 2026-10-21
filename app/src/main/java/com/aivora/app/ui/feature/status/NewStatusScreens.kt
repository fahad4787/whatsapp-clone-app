package com.aivora.app.ui.feature.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.PendingMedia
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.navigation.Routes

@Composable
fun NewStatusScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var mediaUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        PendingMedia.consume()?.let { mediaUri = it }
    }

    val media = rememberMediaActions(context) { uri ->
        mediaUri = uri.toString()
    }

    AivoraScaffold(title = "New status", onBack = onBack) {
        InfoBanner("Local only — nothing is uploaded")
        mediaUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Status photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(max = 220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(160.dp),
            placeholder = { Text("Type a status") },
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Photo status", null, Icons.Default.Photo, onClick = media.pickGallery),
                    SettingsItem("Camera", null, Icons.Default.CameraAlt, onClick = media.takePhoto),
                    SettingsItem(
                        "Status privacy",
                        "Who can see your status",
                        Icons.Default.Visibility,
                        onClick = { onNavigate(Routes.SettingsPrivacyStatus) },
                    ),
                    SettingsItem(
                        "Post status",
                        null,
                        Icons.Default.Send,
                        onClick = {
                            if (text.isBlank() && mediaUri == null) return@SettingsItem
                            InMemoryStatusStore.post(text, mediaUri)
                            onNavigate(Routes.Home)
                        },
                    ),
                ),
            ),
        )
    }
}
