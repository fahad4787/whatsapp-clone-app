package com.aivora.app.ui.feature.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
)

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun fetchAndSend() {
        if (sending || locating) return
        locating = true
        scope.launch {
            val resolved = runCatching {
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
                    ?: "Current location"
                ResolvedLocation(loc.latitude, loc.longitude, name, line)
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
                        ),
                    ),
                )
            }.onSuccess {
                InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid))
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
        if (ok) fetchAndSend()
        else Toast.makeText(context, "Location permission needed", Toast.LENGTH_SHORT).show()
    }

    fun requestOrSend() {
        if (hasLocationPermission()) fetchAndSend()
        else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground),
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Share location", fontWeight = FontWeight.SemiBold)
                    Text(
                        "LOCATION · $peer",
                        style = MaterialTheme.typography.labelMedium,
                        color = AivoraColors.TextSecondary,
                    )
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                TextButton(onClick = { onBack?.invoke() }) {
                    Text("Cancel", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AivoraColors.LightBackground),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text("Your position", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE8F0E9), Color(0xFFD6E4F0), Color(0xFFF0F0F0)),
                        ),
                    ),
            ) {
                if (locating) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AivoraColors.Primary,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.Primary.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = AivoraColors.Primary)
                    }
                }
                Text(
                    preview?.let { p ->
                        p.address ?: p.name ?: "%.5f, %.5f".format(Locale.US, p.lat, p.lng)
                    } ?: "Tap below to use GPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AivoraColors.Ink,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                    .background(AivoraColors.SurfaceMuted),
            ) {
                SettingsRow(
                    SettingsItem(
                        if (locating || sending) "Getting location…" else "Send current location",
                        subtitle = preview?.address
                            ?: "Uses GPS on this device",
                        icon = Icons.Default.NearMe,
                        onClick = { if (!locating && !sending) requestOrSend() },
                    ),
                )
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                    .background(AivoraColors.SurfaceMuted),
            ) {
                SettingsRow(
                    SettingsItem(
                        "Share live location",
                        subtitle = "Let $peer see your location for a while",
                        icon = Icons.Default.ShareLocation,
                        onClick = {
                            Toast.makeText(context, "Live location coming in a later wave", Toast.LENGTH_SHORT).show()
                        },
                    ),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
