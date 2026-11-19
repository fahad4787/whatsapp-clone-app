package com.aivora.app.ui.feature.settings

import com.aivora.app.ui.icons.AppIcons

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.BuildConfig
import com.aivora.app.data.HelpArticles
import com.aivora.app.data.QrCodec
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.DeviceContact
import com.aivora.app.data.local.DeviceContacts
import com.aivora.app.data.local.SessionStore
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.AivoraScrollColumn
import com.aivora.app.ui.components.AivoraSheetAction
import com.aivora.app.ui.components.AivoraSheetActionGrid
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.components.SettingsRow
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HelpFeedbackScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
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
                    iconRes = AivoraIcons.Help,
                    title = "Help Center",
                    subtitle = "Answers to common questions",
                    onClick = { onNavigate(Routes.HelpCenter) },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
                HelpActionRow(
                    iconRes = AivoraIcons.Chat,
                    title = "Contact us",
                    subtitle = "Get help or send feedback",
                    onClick = {
                        val uid = SessionStore.userId(context)?.take(8).orEmpty()
                        val mail = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@aivora.app"))
                            putExtra(Intent.EXTRA_SUBJECT, "Aivora feedback")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Describe your issue or idea:\n\n\n---\nApp ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nUser $uid",
                            )
                        }
                        runCatching { context.startActivity(Intent.createChooser(mail, "Contact us")) }
                            .onFailure {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
                HelpActionRow(
                    icon = AppIcons.Info,
                    title = "App info",
                    subtitle = "About Aivora",
                    onClick = { onNavigate(Routes.HelpAppInfo) },
                )
            }
            Spacer(Modifier.height(12.dp))
            SettingsMutedGroup {
                SettingsRow(SettingsItem("Terms of service", onClick = { onNavigate(Routes.HelpTerms) }))
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
                SettingsRow(SettingsItem("Privacy policy", onClick = { onNavigate(Routes.HelpPrivacy) }))
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
                "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = AivoraColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun HelpAppInfoScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "App info", onBack = onBack) {
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("App name", trailing = "Aivora"),
                    SettingsItem("Version", trailing = BuildConfig.VERSION_NAME),
                    SettingsItem("Build", trailing = BuildConfig.VERSION_CODE.toString()),
                ),
                "Legal" to listOf(
                    SettingsItem("Terms of service", onClick = { onNavigate(Routes.HelpTerms) }),
                    SettingsItem("Privacy policy", onClick = { onNavigate(Routes.HelpPrivacy) }),
                ),
            ),
        )
        Text(
            "Aivora is a messaging app for chats, calls, and updates. Feedback: support@aivora.app",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AivoraColors.TextSecondary,
        )
    }
}

@Composable
fun HelpArticleScreen(
    articleId: String,
    onBack: (() -> Unit)? = null,
) {
    val article = remember(articleId) { HelpArticles.byId(articleId) }
    AivoraScaffold(title = article?.title ?: "Help", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            if (article == null) {
                Text("Article not found.", color = AivoraColors.Danger)
            } else {
                Text(
                    article.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AivoraColors.TextSecondary,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    article.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AivoraColors.Ink,
                )
            }
        }
    }
}

@Composable
fun HelpCenterScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val topics = remember {
        HelpArticles.all.map { it.id to (it.title to it.subtitle) }
    }
    AivoraScaffold(title = "Help Center", onBack = onBack) {
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search help articles",
        )
        SettingsList(
            listOf(
                "Popular topics" to topics
                    .filter { (_, pair) ->
                        query.isBlank() ||
                            pair.first.contains(query, ignoreCase = true) ||
                            pair.second.contains(query, ignoreCase = true)
                    }
                    .map { (id, pair) ->
                        SettingsItem(
                            pair.first,
                            subtitle = pair.second,
                            onClick = { onNavigate(Routes.helpArticle(id)) },
                        )
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
    var showShare by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userId = remember { SessionStore.userId(context).orEmpty() }
    val inviteLink = remember(userId) {
        if (userId.isNotBlank()) QrCodec.HttpsPrefix + userId
        else "https://whatsapp-clone-backend-hep1.onrender.com/"
    }
    val inviteBody =
        "Join me on Aivora! Let’s stay connected with messages, voice and video calls.\n$inviteLink"
    var deviceContacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var contactsDenied by remember { mutableStateOf(false) }

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            deviceContacts = DeviceContacts.load(context)
            contactsDenied = false
        } else {
            contactsDenied = true
        }
    }

    LaunchedEffect(Unit) {
        if (DeviceContacts.hasPermission(context)) {
            deviceContacts = withContext(Dispatchers.IO) { DeviceContacts.load(context) }
        } else {
            permission.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }

    val contacts = deviceContacts.filter {
        query.isBlank() ||
            it.name.contains(query, ignoreCase = true) ||
            it.phone.contains(query, ignoreCase = true)
    }

    if (showShare) {
        AivoraBottomSheet(
            onDismiss = { showShare = false },
            title = "Share invitation",
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AivoraColors.SoftMint)
                    .padding(16.dp),
            ) {
                Text("Aivora", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AivoraColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Join me on Aivora! Let’s stay connected with messages, voice and video calls.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AivoraColors.InkMuted,
                )
                Spacer(Modifier.height(8.dp))
                Text(inviteLink, color = AivoraColors.Primary, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Suggested contacts",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                contacts.take(4).forEach { c ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showShare = false
                            sendSmsInvite(context, c.phone, inviteBody)
                        },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(AivoraColors.SoftMint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(AppIcons.Person, null, tint = AivoraColors.Primary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            c.name.substringBefore(' ').ifBlank { c.name }.take(10),
                            fontSize = 12.sp,
                            color = AivoraColors.InkMuted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            AivoraSheetActionGrid(
                columns = 3,
                actions = listOf(
                    AivoraSheetAction("Messages", AppIcons.Chat) {
                        showShare = false
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, inviteBody)
                                },
                                "Share invitation",
                            ),
                        )
                    },
                    AivoraSheetAction("Mail", AppIcons.Email) {
                        showShare = false
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "message/rfc822"
                                    putExtra(Intent.EXTRA_SUBJECT, "Join me on Aivora")
                                    putExtra(Intent.EXTRA_TEXT, inviteBody)
                                },
                                "Share invitation",
                            ),
                        )
                    },
                    AivoraSheetAction("More", AppIcons.MoreHoriz) {
                        showShare = false
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, inviteBody)
                                },
                                "Share invitation",
                            ),
                        )
                    },
                ),
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AivoraColors.SurfaceMuted)
                    .clickable {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Aivora invite", inviteLink))
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                        showShare = false
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AppIcons.ContentCopy, null, tint = AivoraColors.Primary)
                Spacer(Modifier.width(12.dp))
                Text("Copy link", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                Text("›", color = AivoraColors.TextSecondary, fontSize = 20.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
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
                    icon = AppIcons.Share,
                    title = "Share invitation link",
                    subtitle = null,
                    onClick = { showShare = true },
                )
            }
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search name or phone number",
            )
            Text(
                "From your contacts",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            AivoraScrollColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AivoraDimens.CardHorizontal),
            ) {
                when {
                    contactsDenied && contacts.isEmpty() -> {
                        Text(
                            "Allow contacts access to invite people from your address book, or share the link above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AivoraColors.TextSecondary,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    contacts.isEmpty() -> {
                        Text(
                            if (query.isNotBlank()) "No contacts match “$query”."
                            else "No phone contacts found yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AivoraColors.TextSecondary,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                                .background(AivoraColors.SurfaceMuted),
                        ) {
                            contacts.forEachIndexed { index, c ->
                                InviteContactRow(
                                    name = c.name,
                                    phone = c.phone,
                                    onInvite = { sendSmsInvite(context, c.phone, inviteBody) },
                                )
                                if (index < contacts.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 66.dp),
                                        color = AivoraColors.Divider,
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    "Invitations open your messaging app. Standard SMS charges may apply. Your invite link opens a chat with you in Aivora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AivoraColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                )
            }
        }
    }
}

private fun sendSmsInvite(context: Context, phone: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:${Uri.encode(phone)}")
        putExtra("sms_body", body)
    }
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun LegalDocumentScreen(
    title: String,
    kind: String,
    onBack: (() -> Unit)? = null,
) {
    var body by remember { mutableStateOf("Loading…") }
    val api = remember { NetworkModule.api() }
    LaunchedEffect(kind) {
        body = withContext(Dispatchers.IO) {
            runCatching {
                val docs = api.getLegalDocs()
                when (kind) {
                    "terms" -> docs.termsOfService
                    else -> docs.privacyPolicy
                }.ifBlank { "No document available." }
            }.getOrElse { "Could not load document. Check your connection." }
        }
    }
    AivoraScaffold(title = title, onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.Ink,
            )
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
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    @androidx.annotation.DrawableRes iconRes: Int? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            iconRes != null -> Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = AivoraColors.Ink,
                modifier = Modifier.size(24.dp),
            )
            icon != null -> Icon(icon, contentDescription = null, tint = AivoraColors.Ink, modifier = Modifier.size(24.dp))
        }
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
            Icon(AppIcons.Person, contentDescription = null, tint = AivoraColors.Primary)
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
