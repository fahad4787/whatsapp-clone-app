package com.aivora.app.ui.media

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.decodeUprightBitmap
import com.aivora.app.ui.feature.calls.CallSystemBars
import com.aivora.app.ui.icons.AppIcons
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

private const val PREVIEW_MAX_EDGE = 2048
private const val OUTPUT_MAX_EDGE = 640
private const val MAX_ZOOM = 4f

/** Multipart body for the profile and group photo endpoints. */
fun avatarPart(jpeg: ByteArray): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        "file",
        "avatar.jpg",
        jpeg.toRequestBody("image/jpeg".toMediaType()),
    )

/**
 * "Move and scale": pinch and drag the photo inside the circle, then confirm.
 * [onDone] receives the visible square as a small upright JPEG.
 */
@Composable
fun AvatarCropScreen(
    source: Uri,
    busy: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onDone: (jpeg: ByteArray) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photo by remember(source) { mutableStateOf<Bitmap?>(null) }
    var unreadable by remember(source) { mutableStateOf(false) }
    var zoom by remember(source) { mutableFloatStateOf(1f) }
    var offset by remember(source) { mutableStateOf(Offset.Zero) }
    var encoding by remember { mutableStateOf(false) }
    val viewport = 280.dp
    val viewportPx = with(LocalDensity.current) { viewport.toPx() }
    val working = busy || encoding

    BackHandler { if (!working) onCancel() }
    CallSystemBars()

    LaunchedEffect(source) {
        val decoded = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(source)
                    ?.use { it.readBytes() }
                    ?.let { decodeUprightBitmap(it, PREVIEW_MAX_EDGE) }
            }.getOrNull()
        }
        photo = decoded
        unreadable = decoded == null
    }

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
            IconButton(onClick = onCancel, enabled = !working) {
                Icon(AppIcons.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Text("Move and scale", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            IconButton(
                enabled = photo != null && !working,
                onClick = {
                    val image = photo ?: return@IconButton
                    encoding = true
                    scope.launch {
                        val jpeg = withContext(Dispatchers.Default) {
                            cropSquareJpeg(image, zoom, offset, viewportPx)
                        }
                        encoding = false
                        onDone(jpeg)
                    }
                },
            ) {
                if (working) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = AivoraColors.Primary,
                    )
                } else {
                    Icon(AppIcons.Check, contentDescription = "Done", tint = AivoraColors.Primary)
                }
            }
        }

        // Gestures cover the whole area, so a pinch doesn't have to start inside the circle.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(photo) {
                    val image = photo ?: return@pointerInput
                    detectTransformGestures { _, pan, zoomChange, _ ->
                        val next = (zoom * zoomChange).coerceIn(1f, MAX_ZOOM)
                        val moved = offset * (next / zoom) + pan
                        zoom = next
                        offset = clampOffset(moved, image, next, viewportPx)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val image = photo
            if (image != null) {
                val picture = remember(image) { image.asImageBitmap() }
                Canvas(
                    modifier = Modifier
                        .size(viewport)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                ) {
                    val scale = size.width / min(image.width, image.height) * zoom
                    withTransform({
                        translate(
                            (size.width - image.width * scale) / 2f + offset.x,
                            (size.height - image.height * scale) / 2f + offset.y,
                        )
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        drawImage(picture)
                    }
                }
            } else if (!unreadable) {
                CircularProgressIndicator(color = AivoraColors.Primary)
            }
        }

        Text(
            text = when {
                error != null -> error
                unreadable -> "Could not open this photo"
                else -> "Pinch to zoom, drag to move"
            },
            color = if (error != null || unreadable) AivoraColors.Danger else Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        )
    }
}

/** Keeps the circle fully covered by the photo. */
private fun clampOffset(offset: Offset, image: Bitmap, zoom: Float, viewport: Float): Offset {
    val scale = viewport / min(image.width, image.height) * zoom
    val maxX = ((image.width * scale - viewport) / 2f).coerceAtLeast(0f)
    val maxY = ((image.height * scale - viewport) / 2f).coerceAtLeast(0f)
    return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
}

private fun cropSquareJpeg(image: Bitmap, zoom: Float, offset: Offset, viewport: Float): ByteArray {
    val shortEdge = min(image.width, image.height)
    val side = (shortEdge / zoom).roundToInt().coerceIn(1, shortEdge)
    val scale = viewport / shortEdge * zoom
    val left = (image.width / 2f - offset.x / scale - side / 2f).roundToInt()
        .coerceIn(0, image.width - side)
    val top = (image.height / 2f - offset.y / scale - side / 2f).roundToInt()
        .coerceIn(0, image.height - side)
    val edge = min(side, OUTPUT_MAX_EDGE)
    val output = Bitmap.createBitmap(edge, edge, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(output).apply {
        // JPEG has no transparency; without this, see-through areas turn black.
        drawColor(android.graphics.Color.WHITE)
        drawBitmap(
            image,
            Rect(left, top, left + side, top + side),
            Rect(0, 0, edge, edge),
            Paint(Paint.FILTER_BITMAP_FLAG),
        )
    }
    val bytes = ByteArrayOutputStream()
    output.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
    output.recycle()
    return bytes.toByteArray()
}
