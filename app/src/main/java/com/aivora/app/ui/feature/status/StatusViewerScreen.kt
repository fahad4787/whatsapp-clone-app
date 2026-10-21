package com.aivora.app.ui.feature.status

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.ui.theme.AivoraColors

@Composable
fun StatusViewerScreen(
    statusId: String,
    onBack: (() -> Unit)? = null,
) {
    val status = remember(statusId) { InMemoryStatusStore.get(statusId) }

    LaunchedEffect(statusId) {
        if (status != null && !status.mine) {
            InMemoryStatusStore.markViewed(statusId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onBack?.invoke() },
    ) {
        if (status == null) {
            Text(
                "Status unavailable",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            status.mediaUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Status photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (status.mediaUri == null) {
                    Text(
                        status.text,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AivoraColors.Primary.copy(alpha = 0.85f))
                            .padding(24.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else if (status.text.isNotBlank() && status.text != "Photo") {
                    Text(
                        status.text,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    "${status.authorName} · ${status.timeLabel}",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        IconButton(
            onClick = { onBack?.invoke() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}
