package com.aivora.app.data.local

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Per-conversation message snapshots for instant reopen.
 * Keeps last [MAX_MESSAGES] per chat; LRU-prunes to [MAX_CHATS] files.
 */
object MessageCache {
    private const val DIR = "msg_cache_v1"
    private const val MAX_MESSAGES = 80
    private const val MAX_CHATS = 30
    private const val INDEX = "index"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Serializable
    private data class CachedReply(val id: String, val body: String, val deleted: Boolean = false)

    @Serializable
    private data class CachedReaction(val emoji: String, val count: Int, val mine: Boolean = false)

    @Serializable
    private data class CachedLink(
        val url: String,
        val title: String? = null,
        val description: String? = null,
        val site: String? = null,
        val imageUrl: String? = null,
    )

    @Serializable
    private data class CachedMessage(
        val id: String,
        val mine: Boolean,
        val body: String,
        val time: String,
        val mediaUri: String? = null,
        val mediaKind: String? = null,
        val fileName: String? = null,
        val linkPreview: CachedLink? = null,
        val senderName: String? = null,
        val voiceDurationSec: Int? = null,
        val status: String = "Sent",
        val deletedForEveryone: Boolean = false,
        val replyTo: CachedReply? = null,
        val reactions: List<CachedReaction> = emptyList(),
        val starred: Boolean = false,
        val forwarded: Boolean = false,
        val conversationId: String? = null,
        val createdAtIso: String? = null,
        val editedAtIso: String? = null,
        val viewOnce: Boolean = false,
    )

    fun load(context: Context, chatId: String): List<LocalMessage> {
        val file = chatFile(context, chatId)
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<CachedMessage>>(file.readText()).map { it.toLocal() }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, chatId: String, messages: List<LocalMessage>) {
        if (chatId.isBlank()) return
        val trimmed = messages.takeLast(MAX_MESSAGES).map { it.toCached() }
        runCatching {
            val dir = cacheDir(context).apply { mkdirs() }
            chatFile(context, chatId).writeText(json.encodeToString(trimmed))
            touchIndex(context, chatId)
            prune(context)
        }
    }

    fun clearChat(context: Context, chatId: String) {
        runCatching { chatFile(context, chatId).delete() }
        runCatching {
            val ids = readIndex(context).filterNot { it == chatId }
            writeIndex(context, ids)
        }
    }

    fun clear(context: Context) {
        runCatching {
            cacheDir(context).deleteRecursively()
        }
    }

    private fun cacheDir(context: Context): File =
        File(context.filesDir, DIR)

    private fun chatFile(context: Context, chatId: String): File {
        val safe = chatId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(cacheDir(context), "$safe.json")
    }

    private fun indexFile(context: Context): File = File(cacheDir(context), INDEX)

    private fun readIndex(context: Context): List<String> {
        val f = indexFile(context)
        if (!f.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<String>>(f.readText())
        }.getOrDefault(emptyList())
    }

    private fun writeIndex(context: Context, ids: List<String>) {
        cacheDir(context).mkdirs()
        indexFile(context).writeText(json.encodeToString(ids))
    }

    private fun touchIndex(context: Context, chatId: String) {
        val next = listOf(chatId) + readIndex(context).filterNot { it == chatId }
        writeIndex(context, next.take(MAX_CHATS * 2))
    }

    private fun prune(context: Context) {
        val ids = readIndex(context)
        if (ids.size <= MAX_CHATS) return
        val keep = ids.take(MAX_CHATS).toSet()
        val drop = ids.drop(MAX_CHATS)
        drop.forEach { chatFile(context, it).delete() }
        writeIndex(context, ids.filter { it in keep })
    }

    private fun LocalMessage.toCached() = CachedMessage(
        id = id,
        mine = mine,
        body = body,
        time = time,
        mediaUri = mediaUri,
        mediaKind = mediaKind?.name,
        fileName = fileName,
        linkPreview = linkPreview?.let {
            CachedLink(it.url, it.title, it.description, it.site, it.imageUrl)
        },
        senderName = senderName,
        voiceDurationSec = voiceDurationSec,
        status = status.name,
        deletedForEveryone = deletedForEveryone,
        replyTo = replyTo?.let { CachedReply(it.id, it.body, it.deleted) },
        reactions = reactions.map { CachedReaction(it.emoji, it.count, it.mine) },
        starred = starred,
        forwarded = forwarded,
        conversationId = conversationId,
        createdAtIso = createdAtIso,
        editedAtIso = editedAtIso,
        viewOnce = viewOnce,
    )

    private fun CachedMessage.toLocal() = LocalMessage(
        id = id,
        mine = mine,
        body = body,
        time = time,
        mediaUri = mediaUri,
        mediaKind = mediaKind?.let { runCatching { LocalMediaKind.valueOf(it) }.getOrNull() },
        fileName = fileName,
        linkPreview = linkPreview?.let {
            LocalLinkPreview(it.url, it.title, it.description, it.site, it.imageUrl)
        },
        senderName = senderName,
        voiceDurationSec = voiceDurationSec,
        status = runCatching { MessageDeliveryStatus.valueOf(status) }
            .getOrDefault(MessageDeliveryStatus.Sent),
        deletedForEveryone = deletedForEveryone,
        replyTo = replyTo?.let { LocalReplyPreview(it.id, it.body, it.deleted) },
        reactions = reactions.map { LocalReaction(it.emoji, it.count, it.mine) },
        starred = starred,
        forwarded = forwarded,
        conversationId = conversationId,
        createdAtIso = createdAtIso,
        editedAtIso = editedAtIso,
        viewOnce = viewOnce,
    )
}
