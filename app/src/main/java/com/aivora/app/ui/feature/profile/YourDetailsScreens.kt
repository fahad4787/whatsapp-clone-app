package com.aivora.app.ui.feature.profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.MeUpdateBody
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraSheetAction
import com.aivora.app.ui.components.AivoraSheetActionList
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun YourDetailsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }

    var name by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var links by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var editingField by remember { mutableStateOf<String?>(null) }
    var showEditPhoto by remember { mutableStateOf(false) }
    var showCrop by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val media = rememberMediaActions(context) { uri ->
        photoUri = uri.toString()
        showEditPhoto = false
        showCrop = true
    }

    fun applyUser(
        displayName: String?,
        aboutText: String?,
        phoneText: String?,
        avatar: String?,
    ) {
        name = displayName.orEmpty()
        about = aboutText.orEmpty()
        phone = phoneText.orEmpty()
        avatarUrl = avatar
    }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            val me = api.getMe()
            applyUser(me.displayName, me.about, me.phoneDisplay ?: me.phone, me.avatarUrl)
        } catch (e: Exception) {
            error = e.message ?: "Could not load profile"
        } finally {
            loading = false
        }
    }

    suspend fun uploadAvatar(uriString: String) {
        val uri = Uri.parse(uriString)
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } ?: throw IllegalStateException("Could not read image")
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", body)
        val me = api.postMeAvatar(part)
        applyUser(me.displayName, me.about, me.phoneDisplay ?: me.phone, me.avatarUrl)
    }

    if (showCrop) {
        EditPhotoCropScreen(
            name = name.ifBlank { "A" },
            previewUri = photoUri,
            busy = saving,
            onCancel = { showCrop = false },
            onDone = {
                val uri = photoUri ?: return@EditPhotoCropScreen
                saving = true
                error = null
                scope.launch {
                    try {
                        uploadAvatar(uri)
                        showCrop = false
                    } catch (e: Exception) {
                        error = e.message ?: "Upload failed"
                    } finally {
                        saving = false
                    }
                }
            },
        )
        return
    }

    if (showEditPhoto) {
        AivoraBottomSheet(
            onDismiss = { showEditPhoto = false },
            title = "Edit profile picture",
            leading = {
                ProfileAvatar(name = name, avatarUrl = avatarUrl, size = 40)
            },
        ) {
            AivoraSheetActionList(
                actions = listOf(
                    AivoraSheetAction("Take photo", Icons.Default.CameraAlt, onClick = media.takePhoto),
                    AivoraSheetAction("Choose photo", Icons.Default.PhotoLibrary, onClick = media.pickGallery),
                    AivoraSheetAction("Delete photo", Icons.Default.Delete, danger = true) {
                        showEditPhoto = false
                        saving = true
                        scope.launch {
                            try {
                                val me = api.deleteMeAvatar()
                                applyUser(me.displayName, me.about, me.phoneDisplay ?: me.phone, me.avatarUrl)
                                photoUri = null
                            } catch (e: Exception) {
                                error = e.message ?: "Could not delete photo"
                            } finally {
                                saving = false
                            }
                        }
                    },
                ),
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    editingField?.let { field ->
        EditFieldDialog(
            field = field,
            initial = when (field) {
                "Name" -> name
                "About" -> about
                else -> links
            },
            onDismiss = { editingField = null },
            onSave = { value ->
                when (field) {
                    "Name", "About" -> {
                        saving = true
                        error = null
                        scope.launch {
                            try {
                                val me = api.putMe(
                                    MeUpdateBody(
                                        displayName = if (field == "Name") value else null,
                                        about = if (field == "About") value else null,
                                    ),
                                )
                                applyUser(me.displayName, me.about, me.phoneDisplay ?: me.phone, me.avatarUrl)
                                editingField = null
                            } catch (e: Exception) {
                                error = e.message ?: "Could not save"
                            } finally {
                                saving = false
                            }
                        }
                    }
                    else -> {
                        links = value
                        editingField = null
                    }
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AivoraDimens.Space2, vertical = AivoraDimens.Space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onBack?.invoke() }) {
                Icon(
                    painter = painterResource(AivoraIcons.Back),
                    contentDescription = "Back",
                    tint = AivoraColors.Ink,
                )
            }
            Text(
                "Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AivoraColors.InkMuted,
            )
            Spacer(Modifier.weight(1f))
            if (saving || loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp).padding(end = 12.dp),
                    strokeWidth = 2.dp,
                    color = AivoraColors.Primary,
                )
            }
        }

        if (error != null) {
            Text(
                error!!,
                color = AivoraColors.Danger,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            ProfileAvatar(name = name.ifBlank { "?" }, avatarUrl = avatarUrl, size = 132)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(AivoraDimens.RadiusFull))
                    .background(AivoraColors.PrimarySoft)
                    .clickable(enabled = !saving) { showEditPhoto = true }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(AivoraIcons.Camera),
                    contentDescription = null,
                    tint = AivoraColors.Primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Edit photo",
                    color = AivoraColors.Primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AivoraDimens.ScreenHorizontal)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "PROFILE DETAILS",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.7.sp,
                        color = AivoraColors.TextSecondary,
                    )
                    Spacer(Modifier.width(12.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = AivoraColors.Divider)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                        .background(AivoraColors.SurfaceMuted),
                ) {
                    ProfileDetailRow("Name", name.ifBlank { "Add name" }, accent = false) {
                        editingField = "Name"
                    }
                    HorizontalDivider(color = AivoraColors.Divider, modifier = Modifier.padding(start = 16.dp))
                    ProfileDetailRow("About", about.ifBlank { "Add about" }, accent = true) {
                        editingField = "About"
                    }
                    HorizontalDivider(color = AivoraColors.Divider, modifier = Modifier.padding(start = 16.dp))
                    ProfileDetailRow("Phone number", phone.ifBlank { "—" }, accent = false) { /* read-only */ }
                    HorizontalDivider(color = AivoraColors.Divider, modifier = Modifier.padding(start = 16.dp))
                    ProfileDetailRow(
                        "Links",
                        links.ifBlank { "Add links" },
                        accent = true,
                    ) { editingField = "Links" }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(name: String, avatarUrl: String?, size: Int) {
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(AivoraColors.PrimarySoft),
        )
    } else {
        Avatar(name, size = size)
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    accent: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AivoraColors.InkMuted, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = if (accent) AivoraColors.Primary else AivoraColors.TextSecondary,
            fontSize = 15.sp,
            maxLines = 1,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(AivoraIcons.ChevronRight),
            contentDescription = null,
            tint = AivoraColors.IconSecondary,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun EditPhotoCropScreen(
    name: String,
    previewUri: String?,
    busy: Boolean,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onCancel, enabled = !busy) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Text("Move and scale", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            IconButton(onClick = onDone, enabled = !busy) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = AivoraColors.Primary)
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Done", tint = AivoraColors.Primary)
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (!previewUri.isNullOrBlank()) {
                    AsyncImage(
                        model = previewUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                } else {
                    Avatar(
                        name,
                        size = 280,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EditFieldDialog(
    field: String,
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember(field, initial) { mutableStateOf(initial) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
        ) {
            Text(field, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AivoraColors.Ink)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = field != "About",
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(onClick = { onSave(value.trim()) }) { Text("Save") }
            }
        }
    }
}
