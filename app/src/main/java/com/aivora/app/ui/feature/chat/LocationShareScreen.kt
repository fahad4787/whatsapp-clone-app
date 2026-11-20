package com.aivora.app.ui.feature.chat

import com.aivora.app.ui.icons.AppIcons

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import coil3.asDrawable
import coil3.network.httpHeaders
import coil3.request.allowHardware
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import com.aivora.app.data.LiveLocationSession
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.LocationBody
import com.aivora.app.data.model.SendMessageBody
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsRow
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

private data class ResolvedLocation(
    val lat: Double,
    val lng: Double,
    val name: String?,
    val address: String?,
    val accuracy: Double? = null,
)

private enum class ShareMode { Once, Live, Preview }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationShareScreen(
    chatId: String = "1",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val chat = remember(chatId) { InMemoryChatStore.chat(chatId) }
    val peer = chat.title.substringBefore(" ").ifBlank { "contact" }
    var sending by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<ResolvedLocation?>(null) }
    var pendingMode by remember { mutableStateOf(ShareMode.Once) }
    var showDurationPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun resolveLocation(mode: ShareMode): ResolvedLocation {
        val cts = CancellationTokenSource()
        val loc = fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            ?: fused.lastLocation.await()
            ?: error("no_fix")
        val address = withContext(Dispatchers.IO) {
            runCatching {
                if (!Geocoder.isPresent()) return@runCatching null
                @Suppress("DEPRECATION")
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(loc.latitude, loc.longitude, 1)
                    ?.firstOrNull()
            }.getOrNull()
        }
        val line = address?.getAddressLine(0)
        val name = listOfNotNull(address?.featureName, address?.locality, address?.subLocality)
            .firstOrNull { !it.isNullOrBlank() && it != line }
            ?: if (mode == ShareMode.Live) "Live location" else "Current location"
        return ResolvedLocation(loc.latitude, loc.longitude, name, line, accuracy = loc.accuracy.toDouble())
    }

    fun loadPreview() {
        if (locating || sending) return
        locating = true
        scope.launch {
            runCatching { resolveLocation(ShareMode.Once) }
                .onSuccess { preview = it }
                .onFailure {
                    Toast.makeText(context, "Could not get GPS fix. Try again outdoors.", Toast.LENGTH_SHORT).show()
                }
            locating = false
        }
    }

    fun fetchAndSend(mode: ShareMode, durationSeconds: Int = 900) {
        if (sending || locating) return
        locating = true
        scope.launch {
            val resolved = runCatching {
                val cached = preview
                if (mode == ShareMode.Once && cached != null) cached else resolveLocation(mode)
            }.getOrElse {
                locating = false
                Toast.makeText(
                    context,
                    if (it.message == "no_fix") "Could not get GPS fix. Try again outdoors."
                    else "Could not read location",
                    Toast.LENGTH_SHORT,
                ).show()
                return@launch
            }
            preview = resolved
            locating = false

            val uid = SessionStore.userId(context)
            if (SessionStore.accessToken(context).isNullOrBlank() || uid == null) {
                InMemoryChatStore.sendText(
                    chatId,
                    "📍 ${resolved.name ?: "Location"} · ${resolved.address ?: "${resolved.lat}, ${resolved.lng}"}",
                )
                onBack?.invoke()
                return@launch
            }
            sending = true
            runCatching {
                NetworkModule.api().postConversationsIdMessages(
                    chatId,
                    SendMessageBody(
                        clientId = UUID.randomUUID().toString(),
                        location = LocationBody(
                            lat = resolved.lat,
                            lng = resolved.lng,
                            name = resolved.name,
                            address = resolved.address,
                            live = mode == ShareMode.Live,
                            accuracy = resolved.accuracy,
                            durationSeconds = if (mode == ShareMode.Live) durationSeconds else null,
                        ),
                    ),
                )
            }.onSuccess {
                InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid))
                val msgId = it.id
                if (mode == ShareMode.Live && !msgId.isNullOrBlank()) {
                    LiveLocationSession.start(context, msgId, chatId)
                }
                onBack?.invoke()
            }.onFailure {
                sending = false
                Toast.makeText(context, "Could not send location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) {
            if (pendingMode == ShareMode.Live) showDurationPicker = true
            else if (pendingMode == ShareMode.Preview) loadPreview()
            else fetchAndSend(ShareMode.Once)
        } else Toast.makeText(context, "Location permission needed", Toast.LENGTH_SHORT).show()
    }

    fun requestOrSend(mode: ShareMode) {
        pendingMode = mode
        if (hasLocationPermission()) {
            if (mode == ShareMode.Live) showDurationPicker = true
            else if (mode == ShareMode.Preview) loadPreview()
            else fetchAndSend(ShareMode.Once)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) { requestOrSend(ShareMode.Preview) }

    if (showDurationPicker) {
        var selected by remember { mutableStateOf(900) }
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (!locating && !sending) showDurationPicker = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(AivoraColors.SurfaceMuted)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.ShareLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Share live location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$peer will see where you are in real time. You can stop sharing at any time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AivoraColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                listOf(900 to "15 minutes", 3600 to "1 hour", 28800 to "8 hours").forEach { (secs, label) ->
                    val on = selected == secs
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (on) AivoraColors.Primary.copy(alpha = 0.16f) else Color.Transparent)
                            .clickable { selected = secs }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = on,
                            onClick = { selected = secs },
                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                selectedColor = AivoraColors.Primary,
                            ),
                        )
                        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 6.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { if (!locating && !sending) showDurationPicker = false },
                        modifier = Modifier.weight(1f),
                    ) { Text("Cancel", color = AivoraColors.Primary) }
                    androidx.compose.material3.Button(
                        onClick = {
                            showDurationPicker = false
                            fetchAndSend(ShareMode.Live, selected)
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = AivoraColors.Primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("Share") }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground),
    ) {
        TopAppBar(
            title = { Text("Send location", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(AppIcons.Close, contentDescription = "Close")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AivoraColors.LightBackground),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFE8EDE9)),
        ) {
            val p = preview
            if (p != null) {
                LocationMapImage(p.lat, p.lng, Modifier.fillMaxSize())
                Icon(
                    AppIcons.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFE64C53),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 22.dp)
                        .size(44.dp),
                )
            }
            if (locating && p == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AivoraColors.Primary,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = !locating && !sending) { requestOrSend(ShareMode.Preview) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.MyLocation, contentDescription = "My location", tint = Color(0xFF3C4043))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            LocationActionRow(
                icon = AppIcons.MyLocation,
                title = "Send your current location",
                subtitle = when {
                    sending -> "Sending…"
                    locating -> "Getting location…"
                    preview?.accuracy != null -> "Accurate to ${preview!!.accuracy!!.toInt()} meters"
                    else -> "Uses GPS on this device"
                },
                onClick = { if (!locating && !sending) requestOrSend(ShareMode.Once) },
            )
            LocationActionRow(
                icon = AppIcons.ShareLocation,
                title = "Share live location",
                subtitle = "Updates in real time as you move · 15m / 1h / 8h",
                onClick = { if (!locating && !sending) requestOrSend(ShareMode.Live) },
            )
            preview?.address?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AivoraColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun LocationActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(AivoraColors.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AivoraColors.TextSecondary)
        }
    }
}

/** OSM tiles (2x2 around the point) cropped so [lat],[lng] is exactly centered. */
@Composable
private fun LocationMapImage(lat: Double, lng: Double, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bmp by remember(lat, lng) { mutableStateOf<android.graphics.Bitmap?>(null) }
    androidx.compose.runtime.LaunchedEffect(lat, lng) {
        bmp = withContext(Dispatchers.IO) {
            runCatching {
                val z = 16
                val n = 1 shl z
                val xf = (lng + 180.0) / 360.0 * n
                val latRad = Math.toRadians(lat)
                val yf = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n
                val tx = Math.floor(xf).toInt().coerceIn(0, n - 1)
                val ty = Math.floor(yf).toInt().coerceIn(0, n - 1)
                val fx = (xf - tx).toFloat()
                val fy = (yf - ty).toFloat()
                val x0 = if (fx < 0.5f) tx - 1 else tx
                val y0 = if (fy < 0.5f) ty - 1 else ty
                val tile = 256
                val out = android.graphics.Bitmap.createBitmap(tile * 2, tile * 2, android.graphics.Bitmap.Config.ARGB_8888)
                val c = android.graphics.Canvas(out)
                c.drawColor(0xFFE8EDE9.toInt())
                var ok = false
                for (dx in 0..1) for (dy in 0..1) {
                    val req = coil3.request.ImageRequest.Builder(context)
                        .data("https://tile.openstreetmap.org/$z/${(x0 + dx).coerceIn(0, n - 1)}/${(y0 + dy).coerceIn(0, n - 1)}.png")
                        .httpHeaders(
                            coil3.network.NetworkHeaders.Builder()
                                .add("User-Agent", "AivoraApp/1.0 (Android)").build(),
                        )
                        .size(coil3.size.Size(tile, tile))
                        .allowHardware(false)
                        .build()
                    val r = coil3.SingletonImageLoader.get(context).execute(req)
                    if (r is coil3.request.SuccessResult) {
                        val dr = r.image.asDrawable(context.resources)
                        dr.setBounds(dx * tile, dy * tile, (dx + 1) * tile, (dy + 1) * tile)
                        dr.draw(c)
                        ok = true
                    }
                }
                if (!ok) return@runCatching null
                val px = (((tx - x0) + fx) * tile).toInt()
                val py = (((ty - y0) + fy) * tile).toInt()
                android.graphics.Bitmap.createBitmap(
                    out,
                    (px - 128).coerceIn(0, tile),
                    (py - 128).coerceIn(0, tile),
                    256, 256,
                )
            }.getOrNull()
        }
    }
    bmp?.let {
        androidx.compose.foundation.Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Map",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = modifier,
        )
    }
}
