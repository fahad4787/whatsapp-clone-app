package com.aivora.app.ui.feature.community

import com.aivora.app.ui.icons.AppIcons

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.CommunityDto
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun CommunityIdScreen(
    communityId: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var community by remember { mutableStateOf<CommunityDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(communityId) {
        if (communityId.isBlank()) return@LaunchedEffect
        runCatching { api.getCommunitiesId(communityId) }
            .onSuccess {
                community = it
                error = null
            }
            .onFailure { error = it.message ?: "Could not load community" }
    }

    val title = community?.name ?: "Community"
    AivoraScaffold(title = title, onBack = onBack) {
        InfoBanner(
            community?.description
                ?: "A community keeps related groups together with one announcement group.",
        )
        if (error != null) {
            Text(error!!, color = AivoraColors.Danger)
        }
        val announcement = community?.announcementId
        val groups = community?.groups.orEmpty()
        SettingsList(
            buildList {
                if (!announcement.isNullOrBlank()) {
                    add(
                        "Announcement" to listOf(
                            SettingsItem(
                                "Community announcements",
                                "Everyone in the community",
                                icon = AppIcons.Groups,
                                onClick = {
                                    onNavigate(Routes.ChatId.replace("{id}", announcement))
                                },
                            ),
                        ),
                    )
                }
                if (groups.isNotEmpty()) {
                    add(
                        "Groups" to groups.mapNotNull { g ->
                            val gid = g.id ?: return@mapNotNull null
                            SettingsItem(
                                g.title ?: "Group",
                                subtitle = buildString {
                                    append("${g.memberCount} members")
                                    if (g.joined) append(" · Joined")
                                },
                                icon = AppIcons.Groups,
                                onClick = {
                                    if (g.joined) {
                                        onNavigate(Routes.ChatId.replace("{id}", gid))
                                    } else {
                                        scope.launch {
                                            runCatching {
                                                api.postCommunitiesIdGroupsConversationIdJoin(
                                                    communityId,
                                                    gid,
                                                )
                                            }.onSuccess {
                                                community = it
                                                onNavigate(Routes.ChatId.replace("{id}", gid))
                                            }.onFailure {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    it.message ?: "Join failed",
                                                    android.widget.Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        }
                                    }
                                },
                            )
                        },
                    )
                }
            },
        )
    }
}
