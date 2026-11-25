package com.aivora.app.ui.feature.profile

import androidx.activity.compose.BackHandler
import com.aivora.app.ui.icons.AppIcons

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.MeUpdateBody
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraSheetAction
import com.aivora.app.ui.components.AivoraSheetActionList
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.media.AvatarCropScreen
import com.aivora.app.ui.media.avatarPart
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.launch

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

    val cachedMe = remember { com.aivora.app.data.local.InMemoryMeStore.me }
    var name by remember { mutableStateOf(cachedMe?.displayName.orEmpty()) }
    var about by remember { mutableStateOf(cachedMe?.about.orEmpty()) }
    var phone by remember {
        mutableStateOf(cachedMe?.phone?.ifBlank { null } ?: SessionStore.phone(context).orEmpty())
    }
    var links by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf(cachedMe?.avatarUrl) }
    var editingField by remember { mutableStateOf<String?>(null) }
    var showEditPhoto by remember { mutableStateOf(false) }
    var cropSource by remember { mutableStateOf<Uri?>(null) }
    // Spinner only when we have nothing cached to paint.
    var loading by remember {
        mutableStateOf(cachedMe == null && name.isBlank() && phone.isBlank())
    }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val media = rememberMediaActions(context, imagesOnly = true) { uri ->
        showEditPhoto = false
        error = null
        cropSource = uri
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
        com.aivora.app.data.local.InMemoryMeStore.replace(displayName, aboutText, phoneText, avatar)
    }

    LaunchedEffect(Unit) {
        val warm = com.aivora.app.data.local.InMemoryMeStore.me
        if (warm != null) {
            applyUser(warm.displayName, warm.about, warm.phone, warm.avatarUrl)
            loading = false
        } else if (name.isNotBlank() || phone.isNotBlank()) {
            loading = false
        } else {
            loading = true
        }
        error = null
        try {
            val me = api.getMe()
            applyUser(me.displayName, me.about, me.phoneDisplay ?: me.phone, me.avatarUrl)
        } catch (e: Exception) {
            if (name.isBlank()) error = e.message ?: "Could not load profile"
        } finally {
            loading = false
        }
    }

    cropSource?.let { source ->
        AvatarCropScreen(
            source = source,
            busy = saving,
            error = error,
            onCancel = {
                cropSource = null
                error = null
            },
            onDone = { jpeg ->
                saving = true
                error = null
                scope.launch {
                    try {
                        val me = api.postMeAvatar(avatarPart(jpeg))
                        applyUser(me.displayName, me.about, me.phoneDisplay ?: me.phone, me.avatarUrl)
                        cropSource = null
                    } catch (e: Exception) {
                        error = "Couldn't upload the photo. Check your connection and try again."
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
                    AivoraSheetAction("Take photo", AppIcons.CameraAlt, onClick = media.takePhoto),
                    AivoraSheetAction("Choose photo", AppIcons.PhotoLibrary, onClick = media.pickGallery),
                    AivoraSheetAction("Delete photo", AppIcons.Delete, danger = true) {
                        showEditPhoto = false
                        saving = true
                        scope.launch {
                            try {
                                val me = api.deleteMeAvatar()
                                applyUser(me.displayName, me.about, me.phoneDisplay ?: me.phone, me.avatarUrl)
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
internal fun ProfileAvatar(name: String, avatarUrl: String?, size: Int) {
    Avatar(name, size = size, imageUrl = avatarUrl)
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
