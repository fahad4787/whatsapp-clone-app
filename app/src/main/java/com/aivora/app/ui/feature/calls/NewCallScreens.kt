package com.aivora.app.ui.feature.calls

import com.aivora.app.ui.icons.AppIcons

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.CallLog
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.CallStartBody
import com.aivora.app.data.model.UserDto
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraNoOverscroll
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.displayLabel
import com.aivora.app.ui.components.rememberPeopleSearch
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun NewCallScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { NetworkModule.api() }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()

    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<UserDto?>(null) }
    var calling by remember { mutableStateOf(false) }
    val search = rememberPeopleSearch(query)

    fun startCall(media: String) {
        val peer = selected ?: return
        val peerId = peer.id ?: return
        val name = peer.displayLabel()
        if (!useRemote) {
            onNavigate(if (media == "video") Routes.videoCall(name) else Routes.audioCall(name))
            return
        }
        if (calling) return
        scope.launch {
            calling = true
            Toast.makeText(context, "Calling…", Toast.LENGTH_SHORT).show()
            CallLog.i("new-call start media=$media peer=$peerId")
            val result = runCatching {
                api.postCalls(
                    CallStartBody(
                        media = media,
                        userIds = listOf(peerId),
                    ),
                )
            }.onFailure {
                CallLog.e("new-call failed", it)
                Toast.makeText(context, "Call failed: ${it.message ?: "error"}", Toast.LENGTH_LONG).show()
            }.getOrNull()
            calling = false
            if (result == null) return@launch
            val token = result.token
            val url = result.livekitUrl
            val callId = result.call?.id
            if (token.isNullOrBlank() || url.isNullOrBlank() || callId.isNullOrBlank()) {
                Toast.makeText(context, "Call failed: bad response", Toast.LENGTH_LONG).show()
                return@launch
            }
            ActiveCallSession.setActive(
                callId = callId,
                token = token,
                livekitUrl = url,
                media = media,
                peerName = name,
                asCaller = true,
            )
            onNavigate(if (media == "video") Routes.videoCall(name) else Routes.audioCall(name))
        }
    }

    AivoraScaffold(
        title = "New call",
        onBack = onBack,
        titleContent = {
            Column {
                Text("New call", fontWeight = FontWeight.Bold)
                Text(
                    "Pick someone to call",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        SearchField(query, { query = it }, "Search people")
        if (search.error != null) {
            Text(
                search.error!!,
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 13.sp,
            )
        }
        AivoraNoOverscroll {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    items = search.people.filter { !it.id.isNullOrBlank() },
                    key = { it.id!! },
                    contentType = { "person" },
                ) { person ->
                    val id = person.id!!
                    val name = person.displayLabel()
                    val about = person.about?.ifBlank { null }
                        ?: person.phoneDisplay
                        ?: person.phone
                        ?: ""
                    val on = selected?.id == id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = if (on) null else person }
                            .background(
                                if (on) AivoraColors.Primary.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surface,
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(name, size = 44)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            if (about.isNotBlank()) {
                                Text(
                                    about,
                                    fontSize = 13.sp,
                                    color = AivoraColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BottomCallPill(
                icon = AppIcons.Call,
                label = if (calling) "Calling…" else "Voice call",
                modifier = Modifier.weight(1f),
                enabled = selected != null && !calling,
                onClick = { startCall("audio") },
            )
            BottomCallPill(
                icon = AppIcons.Videocam,
                label = "Video call",
                modifier = Modifier.weight(1f),
                enabled = selected != null && !calling,
                onClick = { startCall("video") },
            )
        }
    }
}

@Composable
private fun BottomCallPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (enabled) AivoraColors.Primary.copy(alpha = 0.14f)
                else AivoraColors.SurfaceMuted,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            null,
            tint = if (enabled) MaterialTheme.colorScheme.onBackground else AivoraColors.TextTertiary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) AivoraColors.Ink else AivoraColors.TextTertiary,
        )
    }
}
