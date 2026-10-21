package com.aivora.app.ui.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.PendingMedia
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

private data class GalleryTile(
    val id: String,
    val label: String,
    val isCamera: Boolean = false,
    val duration: String? = null,
    val hue: Color,
)

@Composable
fun GalleryPickerScreen(
    target: String = "status",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf<String?>(null) }
    val media = rememberMediaActions(context) { uri ->
        val value = uri.toString()
        when {
            target.startsWith("chat:") -> {
                InMemoryChatStore.sendMedia(target.removePrefix("chat:"), value)
                onBack?.invoke()
            }
            else -> {
                PendingMedia.set(value)
                onNavigate(Routes.NewStatus)
            }
        }
    }

    val today = listOf(
        GalleryTile("cam", "Camera", isCamera = true, hue = Color(0xFF3A3A3A)),
        GalleryTile("1", "Latte", hue = Color(0xFF8B6914)),
        GalleryTile("2", "Mountains", duration = "0:12", hue = Color(0xFF4A6FA5)),
        GalleryTile("3", "Flowers", hue = Color(0xFFC45C8A)),
        GalleryTile("4", "Desk", hue = Color(0xFF6B5B4A)),
        GalleryTile("5", "Beach", duration = "0:08", hue = Color(0xFF2A9D8F)),
    )
    val yesterday = listOf(
        GalleryTile("6", "Shore", hue = Color(0xFF457B9D)),
        GalleryTile("7", "Peaks", hue = Color(0xFF6D597A)),
        GalleryTile("8", "Bloom", hue = Color(0xFFE07A5F)),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onBack?.invoke() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Photos & videos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Recents", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            TextButton(onClick = { onBack?.invoke() }) {
                Text("Cancel", color = AivoraColors.TextSecondary)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item(span = { GridItemSpan(3) }) {
                Text(
                    "Today",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            items(today, key = { it.id }) { tile ->
                GalleryCell(
                    tile = tile,
                    selected = selected == tile.id,
                    onClick = {
                        if (tile.isCamera) {
                            onNavigate(Routes.camera(target))
                        } else {
                            selected = if (selected == tile.id) null else tile.id
                        }
                    },
                )
            }
            item(span = { GridItemSpan(3) }) {
                Text(
                    "Yesterday",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            items(yesterday, key = { it.id }) { tile ->
                GalleryCell(
                    tile = tile,
                    selected = selected == tile.id,
                    onClick = { selected = if (selected == tile.id) null else tile.id },
                )
            }
        }

        if (selected != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AivoraColors.LightSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF8B6914)),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1 photo selected", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Add a caption on the next step",
                            style = MaterialTheme.typography.bodySmall,
                            color = AivoraColors.TextSecondary,
                        )
                    }
                    IconButton(onClick = { selected = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "Next",
                    onClick = media.pickGallery,
                )
            }
        }
    }
}

@Composable
private fun GalleryCell(
    tile: GalleryTile,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .then(
                if (selected) Modifier.border(3.dp, AivoraColors.Primary, RoundedCornerShape(AivoraDimens.RadiusMd))
                else Modifier,
            )
            .background(tile.hue)
            .clickable(onClick = onClick),
    ) {
        if (tile.isCamera) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Camera", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) AivoraColors.Primary else Color.Transparent)
                    .border(1.5.dp, if (selected) AivoraColors.Primary else Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            if (tile.duration != null) {
                Text(
                    tile.duration,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                )
            }
        }
    }
}
