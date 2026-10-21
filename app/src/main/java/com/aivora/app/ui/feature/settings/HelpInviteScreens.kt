package com.aivora.app.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.components.SettingsRow
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

@Composable
fun HelpFeedbackScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Help and feedback", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Text(
                "How can we help?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                "Find answers or get in touch with the Aivora team.",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(16.dp))
            SettingsMutedGroup {
                HelpActionRow(
                    icon = Icons.Default.HelpOutline,
                    title = "Help Center",
                    subtitle = "Answers to common questions",
                    onClick = { onNavigate(Routes.HelpCenter) },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
                HelpActionRow(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    title = "Contact us",
                    subtitle = "Get help or send feedback",
                    onClick = { },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
                HelpActionRow(
                    icon = Icons.Default.Info,
                    title = "App info",
                    subtitle = "About Aivora",
                    onClick = { },
                )
            }
            Spacer(Modifier.height(12.dp))
            SettingsMutedGroup {
                SettingsRow(SettingsItem("Terms of service", onClick = { }))
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
                SettingsRow(SettingsItem("Privacy policy", onClick = { }))
            }
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = AivoraDimens.CardHorizontal)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                    .background(AivoraColors.BubbleMine)
                    .padding(16.dp),
            ) {
                Text(
                    "Your feedback matters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AivoraColors.Primary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tell us what’s working and what we can improve. We’re here to make Aivora better for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AivoraColors.Ink,
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "Aivora",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = AivoraColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun HelpCenterScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val topics = listOf(
        "Get started" to "Set up your account and start chatting",
        "Chats and media" to "Messages, photos, documents, and more",
        "Calls and video" to "Voice, video, and scheduled calls",
        "Privacy and security" to "Controls that protect your account",
        "Notifications" to "Manage alerts and sounds",
        "Account and backup" to "Number, passkeys, and chat backup",
    )
    AivoraScaffold(title = "Help Center", onBack = onBack) {
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search help articles",
        )
        SettingsList(
            listOf(
                "Popular topics" to topics
                    .filter {
                        query.isBlank() ||
                            it.first.contains(query, ignoreCase = true) ||
                            it.second.contains(query, ignoreCase = true)
                    }
                    .map { (title, subtitle) ->
                        SettingsItem(title, subtitle = subtitle, onClick = { })
                    },
            ),
        )
    }
}

@Composable
fun InviteFriendScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val contacts = listOf(
        "Amrit Singh" to "+91 98765 43210",
        "Navjot Singh" to "+91 98765 43211",
        "Gurpreet Kaur" to "+91 98765 43212",
        "Harleen Kaur" to "+91 98765 43213",
        "Jaspreet Singh" to "+91 98765 43214",
        "Manpreet Kaur" to "+91 98765 43215",
    ).filter {
        query.isBlank() ||
            it.first.contains(query, ignoreCase = true) ||
            it.second.contains(query, ignoreCase = true)
    }

    AivoraScaffold(title = "Invite a friend", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Bring your friends to Aivora. Share an invitation or send one to a contact.",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            SettingsMutedGroup {
                HelpActionRow(
                    icon = Icons.Default.Share,
                    title = "Share invitation link",
                    subtitle = null,
                    onClick = { },
                )
            }
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search name or phone number",
            )
            Text(
                "Contacts not on Aivora",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AivoraDimens.CardHorizontal),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                            .background(AivoraColors.SurfaceMuted),
                    ) {
                        contacts.forEachIndexed { index, (name, phone) ->
                            InviteContactRow(name = name, phone = phone, onInvite = { })
                            if (index < contacts.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 66.dp),
                                    color = AivoraColors.Divider,
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Invitations are sent through your phone’s messaging app. Standard SMS charges may apply.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AivoraColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMutedGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = AivoraDimens.CardHorizontal)
            .fillMaxWidth()
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .background(AivoraColors.SurfaceMuted),
    ) {
        content()
    }
}

@Composable
private fun HelpActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AivoraColors.Ink, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AivoraColors.TextSecondary,
                )
            }
        }
        Icon(
            painter = painterResource(AivoraIcons.ChevronRight),
            contentDescription = null,
            tint = AivoraColors.IconSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun InviteContactRow(
    name: String,
    phone: String,
    onInvite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AivoraColors.BubbleMine),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = AivoraColors.Primary)
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(phone, style = MaterialTheme.typography.bodyMedium, color = AivoraColors.TextSecondary)
        }
        Text(
            "Invite",
            color = AivoraColors.Primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AivoraColors.BubbleMine)
                .clickable(onClick = onInvite)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
