package com.aivora.app.ui.feature.auth

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.aivora.app.data.local.InMemoryMeStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.MeUpdateBody
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraSheetAction
import com.aivora.app.ui.components.AivoraSheetActionList
import com.aivora.app.ui.icons.AppIcons
import com.aivora.app.ui.media.AvatarCropScreen
import com.aivora.app.ui.media.avatarPart
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.theme.AivoraColors
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
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var cropSource by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    val media = rememberMediaActions(context, imagesOnly = true) { uri ->
        photoError = null
        cropSource = uri
    }

    fun finish() {
        SessionStore.setNeedsProfile(context, false)
        onFinished()
    }

    LaunchedEffect(Unit) {
        loading = true
        runCatching { api.getMe() }
            .onSuccess { me ->
                if (!me.displayName.isNullOrBlank()) {
                    // Already set up — don't force the form again.
                    finish()
                    return@LaunchedEffect
                }
                about = me.about?.takeIf { it.isNotBlank() } ?: about
                avatarUrl = me.avatarUrl
            }
        loading = false
    }

    if (loading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.aivora.app.ui.theme.AivoraColors.LightBackground),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = com.aivora.app.ui.theme.AivoraColors.Primary,
                strokeWidth = 3.dp,
            )
        }
        return
    }

    cropSource?.let { source ->
        AvatarCropScreen(
            source = source,
            busy = uploading,
            error = photoError,
            onCancel = {
                cropSource = null
                photoError = null
            },
            onDone = { jpeg ->
                uploading = true
                photoError = null
                scope.launch {
                    try {
                        avatarUrl = api.postMeAvatar(avatarPart(jpeg)).avatarUrl
                        cropSource = null
                    } catch (e: Exception) {
                        photoError = "Couldn't upload the photo. Check your connection and try again."
                    } finally {
                        uploading = false
                    }
                }
            },
        )
        return
    }

    if (showPhotoSheet) {
        AivoraBottomSheet(onDismiss = { showPhotoSheet = false }, title = "Profile photo") {
            AivoraSheetActionList(
                actions = listOf(
                    AivoraSheetAction("Take photo", AppIcons.CameraAlt) {
                        showPhotoSheet = false
                        media.takePhoto()
                    },
                    AivoraSheetAction("Choose photo", AppIcons.PhotoLibrary) {
                        showPhotoSheet = false
                        media.pickGallery()
                    },
                ),
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    AivoraScaffold(title = "Profile setup", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            InfoBanner("Add your name and a profile photo so friends can recognize you.")
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SetupPhoto(name = name, avatarUrl = avatarUrl) { showPhotoSheet = true }
                TextButton(onClick = { showPhotoSheet = true }, enabled = !busy) {
                    Text(if (avatarUrl.isNullOrBlank()) "Add photo" else "Change photo")
                }
                Spacer(modifier = Modifier.height(8.dp))
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
                            val me = api.putMe(
                                MeUpdateBody(
                                    displayName = name.trim(),
                                    about = about.trim().ifBlank { null },
                                ),
                            )
                            // So Home shows the new name and photo straight away.
                            InMemoryMeStore.replace(
                                me.displayName,
                                me.about,
                                me.phoneDisplay ?: me.phone,
                                me.avatarUrl,
                            )
                            finish()
                        } catch (e: Exception) {
                            error = "Couldn't save your profile. Check your connection and try again."
                            busy = false
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                enabled = !busy,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** The tappable photo circle: a camera placeholder until a photo is chosen. */
@Composable
private fun SetupPhoto(name: String, avatarUrl: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(104.dp)
            .clip(CircleShape)
            .background(AivoraColors.PrimarySoft)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarUrl.isNullOrBlank()) {
            Icon(
                AppIcons.CameraAlt,
                contentDescription = "Add photo",
                tint = AivoraColors.Primary,
                modifier = Modifier.size(36.dp),
            )
        } else {
            Avatar(name.ifBlank { "?" }, size = 104, imageUrl = avatarUrl)
        }
    }
}
