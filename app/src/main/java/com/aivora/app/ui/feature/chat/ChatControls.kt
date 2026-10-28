package com.aivora.app.ui.feature.chat

import com.aivora.app.ui.icons.AppIcons

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.theme.AivoraColors
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wallpaper
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.LocalChat
import com.aivora.app.data.model.BlockBody
import com.aivora.app.data.model.ConversationSettingsBody
import com.aivora.app.ui.components.AivoraSheetAction
import com.aivora.app.ui.components.OverflowMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Shared mute / pin / archive / block helpers used by home long-press and in-chat menu. */
object ChatControls {

    fun mergeLocal(
        chatId: String,
        archived: Boolean? = null,
        pinned: Boolean? = null,
        favorite: Boolean? = null,
        wallpaper: String? = null,
        mutedUntil: String? = null,
        clearMute: Boolean = false,
        blockedByMe: Boolean? = null,
    ): LocalChat {
        val current = InMemoryChatStore.chat(chatId)
        val next = current.copy(
            archived = archived ?: current.archived,
            pinned = pinned ?: current.pinned,
            favorite = favorite ?: current.favorite,
            wallpaper = wallpaper ?: current.wallpaper,
            mutedUntil = when {
                clearMute -> null
                mutedUntil != null -> mutedUntil
                else -> current.mutedUntil
            },
            blockedByMe = blockedByMe ?: current.blockedByMe,
        )
        InMemoryChatStore.upsertChat(next)
        return next
    }

    suspend fun toggleMute(api: AivoraApi, useRemote: Boolean, chat: LocalChat): Boolean {
        val nextMuted = !chat.isMuted
        if (!useRemote) {
            if (nextMuted) {
                mergeLocal(
                    chat.id,
                    mutedUntil = java.time.Instant.now().plusSeconds(8 * 3600).toString(),
                )
            } else {
                mergeLocal(chat.id, clearMute = true)
            }
            return true
        }
        return runCatching {
            val res = api.putConversationsIdSettings(
                chat.id,
                ConversationSettingsBody(muted = nextMuted),
            )
            mergeLocal(
                chat.id,
                mutedUntil = res.mutedUntil,
                clearMute = res.mutedUntil.isNullOrBlank(),
            )
        }.isSuccess
    }

    suspend fun togglePin(api: AivoraApi, useRemote: Boolean, chat: LocalChat): Boolean {
        val nextPinned = !chat.pinned
        if (!useRemote) {
            mergeLocal(chat.id, pinned = nextPinned)
            return true
        }
        return runCatching {
            val res = api.putConversationsIdSettings(
                chat.id,
                ConversationSettingsBody(pinned = nextPinned),
            )
            mergeLocal(chat.id, pinned = res.pinned)
        }.isSuccess
    }

    suspend fun toggleFavorite(api: AivoraApi, useRemote: Boolean, chat: LocalChat): Boolean {
        val nextFavorite = !chat.favorite
        if (!useRemote) {
            mergeLocal(chat.id, favorite = nextFavorite)
            return true
        }
        return runCatching {
            val res = api.putConversationsIdSettings(
                chat.id,
                ConversationSettingsBody(favorite = nextFavorite),
            )
            mergeLocal(chat.id, favorite = res.favorite)
        }.isSuccess
    }

    suspend fun setWallpaper(api: AivoraApi, useRemote: Boolean, chat: LocalChat, key: String): Boolean {
        if (!useRemote) {
            mergeLocal(chat.id, wallpaper = key)
            return true
        }
        return runCatching {
            val res = api.putConversationsIdSettings(
                chat.id,
                ConversationSettingsBody(wallpaper = key),
            )
            mergeLocal(chat.id, wallpaper = res.wallpaper ?: key)
        }.isSuccess
    }

    suspend fun toggleArchive(api: AivoraApi, useRemote: Boolean, chat: LocalChat): Boolean {
        val nextArchived = !chat.archived
        if (!useRemote) {
            mergeLocal(chat.id, archived = nextArchived)
            return true
        }
        return runCatching {
            val res = api.putConversationsIdSettings(
                chat.id,
                ConversationSettingsBody(archived = nextArchived),
            )
            mergeLocal(chat.id, archived = res.archived)
        }.isSuccess
    }

    suspend fun block(api: AivoraApi, useRemote: Boolean, chat: LocalChat): Boolean {
        val peerId = chat.peerUserId ?: return false
        if (!useRemote) {
            mergeLocal(chat.id, blockedByMe = true)
            return true
        }
        return runCatching {
            api.postBlocks(BlockBody(userId = peerId))
            mergeLocal(chat.id, blockedByMe = true)
        }.isSuccess
    }

    suspend fun unblock(api: AivoraApi, useRemote: Boolean, chat: LocalChat): Boolean {
        val peerId = chat.peerUserId ?: return false
        if (!useRemote) {
            mergeLocal(chat.id, blockedByMe = false)
            return true
        }
        return runCatching {
            api.deleteBlocksId(peerId)
            mergeLocal(chat.id, blockedByMe = false)
        }.isSuccess
    }

    /**
     * Sheet actions for chat controls.
     * [includeClear] / [includeSearch] only for in-chat; home long-press omits them.
     */
    fun sheetActions(
        chat: LocalChat,
        scope: CoroutineScope,
        context: Context,
        api: AivoraApi,
        useRemote: Boolean,
        includeSearch: Boolean = false,
        includeClear: Boolean = false,
        includeReport: Boolean = true,
        onDismiss: () -> Unit,
        onSearch: (() -> Unit)? = null,
        onClear: (() -> Unit)? = null,
        onReport: (() -> Unit)? = null,
        onViewInfo: (() -> Unit)? = null,
        onWallpaper: (() -> Unit)? = null,
        onScheduleCall: (() -> Unit)? = null,
        onExport: (() -> Unit)? = null,
        onAfterArchive: ((archived: Boolean) -> Unit)? = null,
    ): List<AivoraSheetAction> = buildList {
        onViewInfo?.let { view ->
            add(
                AivoraSheetAction(
                    if (chat.isGroup) "Group info" else "Contact info",
                    AppIcons.Person,
                ) {
                    onDismiss()
                    view()
                },
            )
        }
        if (onScheduleCall != null && !chat.isGroup) {
            add(
                AivoraSheetAction("Schedule call", AppIcons.Event) {
                    onDismiss()
                    onScheduleCall()
                },
            )
        }
        if (onExport != null) {
            add(
                AivoraSheetAction("Export chat", AppIcons.Upload) {
                    onDismiss()
                    onExport()
                },
            )
        }
        if (includeSearch && onSearch != null) {
            add(
                AivoraSheetAction("Search", AppIcons.Search) {
                    onDismiss()
                    onSearch()
                },
            )
        }
        add(
            AivoraSheetAction(
                if (chat.isMuted) "Unmute" else "Mute notifications",
                if (chat.isMuted) AppIcons.NotificationsActive else AppIcons.NotificationsOff,
            ) {
                onDismiss()
                scope.launch {
                    val ok = toggleMute(api, useRemote, chat)
                    if (!ok) {
                        Toast.makeText(context, "Could not update mute", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        add(
            AivoraSheetAction(
                if (chat.pinned) "Unpin" else "Pin",
                AppIcons.PushPin,
            ) {
                onDismiss()
                scope.launch {
                    val ok = togglePin(api, useRemote, chat)
                    if (!ok) {
                        Toast.makeText(context, "Could not pin (max 3)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        add(
            AivoraSheetAction(
                if (chat.favorite) "Remove from favourites" else "Add to favourites",
                if (chat.favorite) AppIcons.Star else AppIcons.StarBorder,
            ) {
                onDismiss()
                scope.launch {
                    val ok = toggleFavorite(api, useRemote, chat)
                    if (!ok) {
                        Toast.makeText(context, "Could not update favourites", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        if (onWallpaper != null) {
            add(
                AivoraSheetAction("Chat wallpaper", AppIcons.Wallpaper) {
                    onDismiss()
                    onWallpaper()
                },
            )
        }
        add(
            AivoraSheetAction(
                if (chat.archived) "Unarchive" else "Archive",
                if (chat.archived) AppIcons.Unarchive else AppIcons.Archive,
            ) {
                onDismiss()
                val willArchive = !chat.archived
                scope.launch {
                    val ok = toggleArchive(api, useRemote, chat)
                    if (!ok) {
                        Toast.makeText(context, "Could not archive", Toast.LENGTH_SHORT).show()
                    } else {
                        onAfterArchive?.invoke(willArchive)
                    }
                }
            },
        )
        if (includeReport && onReport != null) {
            add(
                AivoraSheetAction("Report", AppIcons.Flag, danger = true) {
                    onDismiss()
                    onReport()
                },
            )
        }
        if (!chat.isGroup) {
            if (chat.blockedByMe) {
                add(
                    AivoraSheetAction("Unblock", AppIcons.Block) {
                        onDismiss()
                        scope.launch {
                            val ok = unblock(api, useRemote, chat)
                            Toast.makeText(
                                context,
                                if (ok) "Contact unblocked" else "Could not unblock",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            } else {
                add(
                    AivoraSheetAction("Block", AppIcons.Block, danger = true) {
                        onDismiss()
                        scope.launch {
                            val ok = block(api, useRemote, chat)
                            Toast.makeText(
                                context,
                                if (ok) "Contact blocked" else "Could not block",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
        if (includeClear && onClear != null) {
            add(
                AivoraSheetAction("Clear chat", AppIcons.Delete, danger = true) {
                    onDismiss()
                    onClear()
                },
            )
        }
    }

    /**
     * Chat header ⋮ — common actions only (Figma header has call/video; details page
     * owns schedule / export / pin / favourite / archive / block / report).
     */
    fun headerOverflowMenuItems(
        chat: LocalChat,
        scope: CoroutineScope,
        context: Context,
        api: AivoraApi,
        useRemote: Boolean,
        onDismiss: () -> Unit,
        onSearch: () -> Unit,
        onClear: () -> Unit,
        onViewInfo: () -> Unit,
        onMedia: () -> Unit,
        onWallpaper: () -> Unit,
    ): List<OverflowMenuItem> = buildList {
        add(
            OverflowMenuItem(
                if (chat.isGroup) "Group info" else "Contact info",
                AppIcons.Person,
            ) {
                onDismiss()
                onViewInfo()
            },
        )
        add(
            OverflowMenuItem("Search", AppIcons.Search) {
                onDismiss()
                onSearch()
            },
        )
        add(
            OverflowMenuItem(
                if (chat.isMuted) "Unmute" else "Mute",
                if (chat.isMuted) AppIcons.NotificationsActive else AppIcons.NotificationsOff,
            ) {
                onDismiss()
                scope.launch {
                    val ok = toggleMute(api, useRemote, chat)
                    if (!ok) {
                        Toast.makeText(context, "Could not update mute", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        add(
            OverflowMenuItem("Media", AppIcons.Photo) {
                onDismiss()
                onMedia()
            },
        )
        add(
            OverflowMenuItem("Wallpaper", AppIcons.Wallpaper) {
                onDismiss()
                onWallpaper()
            },
        )
        add(
            OverflowMenuItem("Clear chat", AppIcons.Delete, danger = true) {
                onDismiss()
                onClear()
            },
        )
    }

    /** Same actions as [sheetActions], shaped for the chat ⋮ overflow menu. */
    fun overflowMenuItems(
        chat: LocalChat,
        scope: CoroutineScope,
        context: Context,
        api: AivoraApi,
        useRemote: Boolean,
        includeSearch: Boolean = true,
        includeClear: Boolean = true,
        includeReport: Boolean = true,
        includeMedia: Boolean = false,
        onDismiss: () -> Unit,
        onSearch: (() -> Unit)? = null,
        onClear: (() -> Unit)? = null,
        onReport: (() -> Unit)? = null,
        onViewInfo: (() -> Unit)? = null,
        onMedia: (() -> Unit)? = null,
        onWallpaper: (() -> Unit)? = null,
        onScheduleCall: (() -> Unit)? = null,
        onExport: (() -> Unit)? = null,
        onBlockRequest: (() -> Unit)? = null,
        onAfterArchive: ((archived: Boolean) -> Unit)? = null,
    ): List<OverflowMenuItem> {
        val sheets = sheetActions(
            chat = chat,
            scope = scope,
            context = context,
            api = api,
            useRemote = useRemote,
            includeSearch = includeSearch,
            includeClear = includeClear,
            includeReport = includeReport,
            onDismiss = onDismiss,
            onSearch = onSearch,
            onClear = onClear,
            onReport = onReport,
            onViewInfo = onViewInfo,
            onWallpaper = onWallpaper,
            onScheduleCall = onScheduleCall,
            onExport = onExport,
            onAfterArchive = onAfterArchive,
        ).toMutableList()
        if (includeMedia && onMedia != null) {
            val insertAt = sheets.indexOfFirst { it.label == "Search" }.let { if (it < 0) 1 else it + 1 }
            sheets.add(
                insertAt.coerceAtMost(sheets.size),
                AivoraSheetAction("Media", AppIcons.Photo) {
                    onDismiss()
                    onMedia()
                },
            )
        }
        // Prefer confirm dialog for block when provided.
        if (onBlockRequest != null && !chat.blockedByMe && !chat.isGroup) {
            val idx = sheets.indexOfFirst { it.label == "Block" }
            if (idx >= 0) {
                sheets[idx] = AivoraSheetAction("Block", AppIcons.Block, danger = true) {
                    onDismiss()
                    onBlockRequest()
                }
            }
        }
        return sheets.map { OverflowMenuItem(it.label, it.icon, it.danger, it.onClick) }
    }
}

private val WallpaperChoices = listOf(
    "default" to "Default",
    "mint" to "Mint",
    "dusk" to "Dusk",
    "sand" to "Sand",
)

/** Chat canvas colour for a wallpaper key; null/"default" follows the device theme. */
fun chatWallpaperColor(key: String?, dark: Boolean): Color = when (key) {
    "mint" -> AivoraColors.SoftMint
    "dusk" -> Color(0xFFE4EAF2)
    "sand" -> Color(0xFFF3EBDD)
    else -> if (dark) AivoraColors.ChatWallpaperDark else AivoraColors.ChatWallpaperLight
}

@Composable
fun ChatWallpaperDialog(
    selected: String?,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val ink = if (dark) Color.White else AivoraColors.Ink
    val current = selected?.takeIf { k -> WallpaperChoices.any { it.first == k } } ?: "default"
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = if (dark) AivoraColors.DarkSurface else AivoraColors.LightSurface,
        title = { Text("Chat wallpaper", fontWeight = FontWeight.Bold, color = ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WallpaperChoices.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPick(key) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(chatWallpaperColor(key, dark))
                                .border(
                                    width = if (key == current) 2.dp else 1.dp,
                                    color = if (key == current) AivoraColors.Primary else AivoraColors.ChatDivider,
                                    shape = RoundedCornerShape(10.dp),
                                ),
                        )
                        Text(
                            label,
                            color = ink,
                            fontSize = 16.sp,
                            fontWeight = if (key == current) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(start = 14.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AivoraColors.TextSecondary)
            }
        },
    )
}
