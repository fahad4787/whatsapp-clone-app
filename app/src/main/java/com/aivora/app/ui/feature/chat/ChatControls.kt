package com.aivora.app.ui.feature.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
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
        mutedUntil: String? = null,
        clearMute: Boolean = false,
        blockedByMe: Boolean? = null,
    ): LocalChat {
        val current = InMemoryChatStore.chat(chatId)
        val next = current.copy(
            archived = archived ?: current.archived,
            pinned = pinned ?: current.pinned,
            favorite = pinned ?: current.favorite,
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
        onAfterArchive: ((archived: Boolean) -> Unit)? = null,
    ): List<AivoraSheetAction> = buildList {
        onViewInfo?.let { view ->
            add(
                AivoraSheetAction(
                    if (chat.isGroup) "Group info" else "View contact",
                    Icons.Default.Person,
                ) {
                    onDismiss()
                    view()
                },
            )
        }
        if (includeSearch && onSearch != null) {
            add(
                AivoraSheetAction("Search", Icons.Default.Search) {
                    onDismiss()
                    onSearch()
                },
            )
        }
        add(
            AivoraSheetAction(
                if (chat.isMuted) "Unmute" else "Mute notifications",
                if (chat.isMuted) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
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
                Icons.Default.PushPin,
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
                if (chat.archived) "Unarchive" else "Archive",
                if (chat.archived) Icons.Default.Unarchive else Icons.Default.Archive,
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
                AivoraSheetAction("Report", Icons.Default.Flag, danger = true) {
                    onDismiss()
                    onReport()
                },
            )
        }
        if (!chat.isGroup) {
            if (chat.blockedByMe) {
                add(
                    AivoraSheetAction("Unblock", Icons.Default.Block) {
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
                    AivoraSheetAction("Block", Icons.Default.Block, danger = true) {
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
                AivoraSheetAction("Clear chat", Icons.Default.Delete, danger = true) {
                    onDismiss()
                    onClear()
                },
            )
        }
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
            onAfterArchive = onAfterArchive,
        ).toMutableList()
        if (includeMedia && onMedia != null) {
            val insertAt = sheets.indexOfFirst { it.label == "Search" }.let { if (it < 0) 1 else it + 1 }
            sheets.add(
                insertAt.coerceAtMost(sheets.size),
                AivoraSheetAction("Media", Icons.Default.Photo) {
                    onDismiss()
                    onMedia()
                },
            )
        }
        // Prefer confirm dialog for block when provided.
        if (onBlockRequest != null && !chat.blockedByMe && !chat.isGroup) {
            val idx = sheets.indexOfFirst { it.label == "Block" }
            if (idx >= 0) {
                sheets[idx] = AivoraSheetAction("Block", Icons.Default.Block, danger = true) {
                    onDismiss()
                    onBlockRequest()
                }
            }
        }
        return sheets.map { OverflowMenuItem(it.label, it.icon, it.danger, it.onClick) }
    }
}
