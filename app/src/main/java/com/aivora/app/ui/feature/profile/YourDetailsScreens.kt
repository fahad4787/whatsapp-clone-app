package com.aivora.app.ui.feature.profile

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
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

@Composable
fun YourDetailsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var name by remember { mutableStateOf("Leslie") }
    var about by remember { mutableStateOf("Ending the day with...") }
    var phone by remember { mutableStateOf("+92 323 4226146") }
    var links by remember { mutableStateOf("") }
    var editingField by remember { mutableStateOf<String?>(null) }
    var showEditPhoto by remember { mutableStateOf(false) }
    var showCrop by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val media = rememberMediaActions(context) { uri ->
        photoUri = uri.toString()
        showEditPhoto = false
        showCrop = true
    }

    if (showCrop) {
        EditPhotoCropScreen(
            name = name,
            onCancel = { showCrop = false },
            onDone = { showCrop = false },
        )
        return
    }

    if (showEditPhoto) {
        EditPhotoSheet(
            onDismiss = { showEditPhoto = false },
            onCamera = media.takePhoto,
            onGallery = media.pickGallery,
            onRemove = {
                photoUri = null
                showEditPhoto = false
            },
        )
    }

    editingField?.let { field ->
        EditFieldDialog(
            field = field,
            initial = when (field) {
                "Name" -> name
                "About" -> about
                "Phone number" -> phone
                else -> links
            },
            onDismiss = { editingField = null },
            onSave = { value ->
                when (field) {
                    "Name" -> name = value
                    "About" -> about = value
                    "Phone number" -> phone = value
                    else -> links = value
                }
                editingField = null
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
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            Avatar(name, size = 132)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(AivoraDimens.RadiusFull))
                    .background(AivoraColors.PrimarySoft)
                    .clickable { showEditPhoto = true }
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
                    ProfileDetailRow("Name", name, accent = false) { editingField = "Name" }
                    HorizontalDivider(color = AivoraColors.Divider, modifier = Modifier.padding(start = 16.dp))
                    ProfileDetailRow("About", about, accent = true) { editingField = "About" }
                    HorizontalDivider(color = AivoraColors.Divider, modifier = Modifier.padding(start = 16.dp))
                    ProfileDetailRow("Phone number", phone, accent = false) { editingField = "Phone number" }
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
private fun EditPhotoSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {}
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
        ) {
            Text("Edit profile picture", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AivoraColors.Ink)
            Spacer(Modifier.height(16.dp))
            EditPhotoOption(Icons.Default.CameraAlt, "Camera", onCamera)
            EditPhotoOption(Icons.Default.PhotoLibrary, "Gallery", onGallery)
            EditPhotoOption(Icons.Default.Close, "Remove photo", onRemove, danger = true)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Cancel", color = AivoraColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun EditPhotoOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    danger: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (danger) AivoraColors.Danger else AivoraColors.Primary,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            fontSize = 16.sp,
            color = if (danger) AivoraColors.Danger else AivoraColors.Ink,
        )
    }
}

@Composable
private fun EditPhotoCropScreen(
    name: String,
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
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Text("Move and scale", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            IconButton(onClick = onDone) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = AivoraColors.Primary)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { scale = (scale - 0.1f).coerceAtLeast(0.8f) }) {
                Text("−", color = Color.White, fontSize = 22.sp)
            }
            Text("Pinch to zoom (mock)", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            TextButton(onClick = { scale = (scale + 0.1f).coerceAtMost(1.6f) }) {
                Text("+", color = Color.White, fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun EditFieldDialog(
    field: String,
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember(field) { mutableStateOf(initial) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {}
                .padding(20.dp),
        ) {
            Text(field, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AivoraColors.Ink)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = field != "About",
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = AivoraColors.TextSecondary)
                }
                TextButton(onClick = { onSave(value.trim()) }) {
                    Text("Save", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
