package com.aivora.app.ui.feature.chat

import androidx.compose.runtime.DisposableEffect
import android.content.pm.ActivityInfo
import android.content.ContextWrapper
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aivora.app.data.MediaGallery
import com.aivora.app.ui.components.blockTouchesBehind
import com.aivora.app.ui.feature.calls.CallSystemBars
import com.aivora.app.ui.icons.AppIcons
import com.aivora.app.ui.media.rememberBubbleImageModel
import kotlinx.coroutines.launch

/**
 * The app is portrait-only; photos may follow the phone (respecting the system rotation lock).
 * The manifest handles these config changes, so rotating does not rebuild the activity.
 */
@Composable
private fun RotateWhileVisible() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val activity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>()
            .firstOrNull()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}

/** Caption shown under a photo — skips placeholder bubble labels. */
fun mediaCaptionOf(body: String): String? {
    val placeholders = setOf(
        "Photo",
        "Video",
        "View once photo",
        "Opened",
        "Document",
        "Audio",
    )
    return body.trim().takeIf { it.isNotEmpty() && it !in placeholders }
}

/**
 * Full-screen in-app photo viewer (normal + view-once). Same chrome for both.
 */
@Composable
fun ChatImageViewer(
    url: String,
    caption: String? = null,
    title: String = "Photo",
    subtitle: String? = null,
    /** When true, hide Save and skip auto-gallery publish. */
    viewOnce: Boolean = false,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    BackHandler(onBack = onDismiss)
    RotateWhileVisible()
    CallSystemBars()

    // The keyboard from the chat underneath would otherwise cover the photo.
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(url) {
        focusManager.clearFocus(force = true)
        keyboard?.hide()
    }

    // Media visibility On → copy into gallery when the photo is opened (Wave 57).
    LaunchedEffect(url, viewOnce) {
        if (viewOnce || url.isBlank()) return@LaunchedEffect
        if (!MediaGallery.visibilityEnabled(context)) return@LaunchedEffect
        MediaGallery.publishUrl(context, url)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .blockTouchesBehind(),
    ) {
        AsyncImage(
            model = rememberBubbleImageModel(url, width = 720.dp, height = 1280.dp),
            contentDescription = title,
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
                // Sideways, the navigation bar and camera cutout sit at the screen's edges.
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                )
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(AppIcons.Close, contentDescription = "Close", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                    )
                }
            }
            if (!viewOnce) {
                IconButton(
                    enabled = !saving && url.isNotBlank(),
                    onClick = {
                        if (saving) return@IconButton
                        saving = true
                        scope.launch {
                            val ok = MediaGallery.publishUrl(context, url) != null
                            saving = false
                            Toast.makeText(
                                context,
                                if (ok) "Saved to gallery" else "Could not save",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                ) {
                    Icon(
                        AppIcons.PhotoLibrary,
                        contentDescription = "Save to gallery",
                        tint = Color.White,
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        if (!caption.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                        ),
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Text(
                    caption,
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
