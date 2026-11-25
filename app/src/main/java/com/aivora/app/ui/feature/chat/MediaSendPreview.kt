package com.aivora.app.ui.feature.chat

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil3.compose.AsyncImage
import com.aivora.app.ui.icons.AppIcons
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.components.blockTouchesBehind
import kotlin.math.max

/**
 * WhatsApp-style photo compose as an in-activity overlay (not a Dialog) so
 * OEM nav bars (e.g. Vivo) correctly inset the caption + send row.
 */
@Composable
fun MediaSendPreview(
    uri: Uri,
    initialViewOnce: Boolean = false,
    onDismiss: () -> Unit,
    onSend: (caption: String, viewOnce: Boolean) -> Unit,
) {
    var caption by remember { mutableStateOf("") }
    var viewOnce by remember(initialViewOnce) { mutableStateOf(initialViewOnce) }

    BackHandler(onBack = onDismiss)

    val view = LocalView.current
    val density = LocalDensity.current
    val composeNavPx = WindowInsets.navigationBars.getBottom(density)
    val activityNavPx = ViewCompat.getRootWindowInsets(view)
        ?.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        ?.bottom
        ?: 0
    // Floor 56dp — Vivo FunTouch often under-reports gesture inset in overlays.
    val bottomPad = with(density) {
        max(composeNavPx, activityNavPx).toDp()
    }.coerceAtLeast(56.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .blockTouchesBehind(),
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                    ),
                )
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(AppIcons.Close, contentDescription = "Close", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                if (viewOnce) "View once" else "Photo",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f)),
                    ),
                )
                .windowInsetsPadding(WindowInsets.ime)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = bottomPad,
                ),
        ) {
            if (viewOnce) {
                Text(
                    "View once photo · opens once",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Caption field with view-once (AppIcons.Visibility) on the right
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = caption,
                        onValueChange = { if (it.length <= 1024) caption = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                        cursorBrush = SolidColor(AivoraColors.Primary),
                        decorationBox = { inner ->
                            Box {
                                if (caption.isEmpty()) {
                                    Text(
                                        "Add a caption…",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 15.sp,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                    IconButton(
                        onClick = { viewOnce = !viewOnce },
                        modifier = Modifier.size(44.dp),
                    ) {
                        ViewOnceEyeIcon(active = viewOnce, size = 22.dp)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary)
                        .clickable {
                            onSend(caption.trim(), viewOnce)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcons.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

/** View-once eye from the shared [AppIcons] library. */
@Composable
fun ViewOnceEyeIcon(
    active: Boolean,
    size: Dp = 22.dp,
    opened: Boolean = false,
    tint: Color? = null,
) {
    Icon(
        imageVector = if (opened) AppIcons.VisibilityOff else AppIcons.Visibility,
        contentDescription = if (opened) "Opened" else "View once",
        tint = tint ?: when {
            opened -> Color.White.copy(alpha = 0.85f)
            active -> AivoraColors.Primary
            else -> Color.White
        },
        modifier = Modifier.size(size),
    )
}
