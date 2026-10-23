package com.aivora.app.data.local

data class LocalChat(
    val id: String,
    val title: String,
    val preview: String,
    val time: String,
    val unread: Int = 0,
    val isGroup: Boolean = false,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val pinned: Boolean = false,
    /** ISO muted-until; null/blank = not muted. */
    val mutedUntil: String? = null,
    /** I blocked this peer. */
    val blockedByMe: Boolean = false,
    /** This peer blocked me. */
    val blockedMe: Boolean = false,
    /** Peer user id for 1:1 chats (reports / profile). */
    val peerUserId: String? = null,
    /** Group: "admin" | "member". */
    val myRole: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null,
    val onlyAdminsCanSend: Boolean = false,
    val onlyAdminsCanEditInfo: Boolean = false,
) {
    val isGroupAdmin: Boolean get() = myRole.equals("admin", ignoreCase = true)
    val isMuted: Boolean
        get() {
            val until = mutedUntil ?: return false
            return runCatching {
                java.time.Instant.parse(until).isAfter(java.time.Instant.now())
            }.getOrDefault(false)
        }
}

enum class MessageDeliveryStatus {
    Sending,
    Sent,
    Delivered,
    Read,
}

data class LocalReplyPreview(
    val id: String,
    val body: String,
    val deleted: Boolean = false,
)

data class LocalReaction(
    val emoji: String,
    val count: Int,
    val mine: Boolean = false,
)

enum class LocalMediaKind {
    Image,
    Audio,
    Video,
    File,
}

data class LocalLinkPreview(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val site: String? = null,
    val imageUrl: String? = null,
)

data class LocalMessage(
    val id: String,
    val mine: Boolean,
    val body: String,
    val time: String,
    val mediaUri: String? = null,
    val mediaKind: LocalMediaKind? = null,
    val fileName: String? = null,
    val linkPreview: LocalLinkPreview? = null,
    val senderName: String? = null,
    /** When non-null, message is a voice note (duration in seconds). */
    val voiceDurationSec: Int? = null,
    val status: MessageDeliveryStatus = MessageDeliveryStatus.Sent,
    val deletedForEveryone: Boolean = false,
    val replyTo: LocalReplyPreview? = null,
    val reactions: List<LocalReaction> = emptyList(),
    val starred: Boolean = false,
    val forwarded: Boolean = false,
    val conversationId: String? = null,
    /** ISO created_at from server — used for older-history paging. */
    val createdAtIso: String? = null,
)

/** Seed data for the in-memory store (no persistence). */
object SeedData {
    val chats = listOf(
        LocalChat("1", "Ahmad Khan", "Yes, joining in 5.", "9:41 PM", unread = 2, favorite = true),
        LocalChat("2", "Family Group", "Fatima: See you tomorrow", "8:12 PM", isGroup = true, favorite = true),
        LocalChat("3", "Aivora", "Welcome to Aivora", "Yesterday"),
        LocalChat("4", "Usman Raza", "Photo", "Yesterday", unread = 1),
        LocalChat("5", "Work", "Meeting notes shared", "Monday", isGroup = true),
        LocalChat("6", "Zainab Noor", "Voice message", "Sunday", favorite = true),
        LocalChat("7", "Hassan Malik", "Location shared", "Saturday", archived = true),
        LocalChat("8", "Community Announcements", "New post in General", "Friday", isGroup = true, archived = true),
    )

    fun chat(id: String): LocalChat =
        chats.find { it.id == id } ?: LocalChat(id, "Chat", "Say hello", "Now")

    fun messagesFor(chatId: String): List<LocalMessage> = when (chatId) {
        "2" -> listOf(
            LocalMessage("m1", false, "Who's bringing dessert?", "7:58 PM", senderName = "George"),
            LocalMessage("m2", true, "I can pick something up.", "8:01 PM"),
            LocalMessage("m3", false, "See you tomorrow", "8:12 PM", senderName = "Fatima"),
        )
        else -> listOf(
            LocalMessage("m1", false, "Hey! Found a spot for our Sunday catch-up?", "4:24 PM"),
            LocalMessage(
                id = "m1v",
                mine = false,
                body = "Voice message",
                time = "4:24 PM",
                voiceDurationSec = 18,
            ),
            LocalMessage("m2", true, "Yes! Chai and a long walk?", "4:25 PM"),
            LocalMessage("m3", true, "This view + chai. Sunday sorted?", "4:26 PM"),
            LocalMessage(
                id = "m3v",
                mine = true,
                body = "Voice message",
                time = "4:27 PM",
                voiceDurationSec = 9,
            ),
            LocalMessage("m4", false, "Perfect. See you at 5!", "4:28 PM"),
            LocalMessage("m5", true, "Yes, joining in 5.", "4:28 PM"),
        )
    }

    val contacts = listOf(
        "Ahmad Khan" to "Hey there! I am using Aivora.",
        "Fatima Ali" to "Available",
        "Usman Raza" to "At work",
        "Zainab Noor" to "Busy",
        "Hassan Malik" to "Battery about to die",
        "Sana Iqbal" to "In a meeting",
        "Bilal Ahmed" to "Can't talk now",
    )
}
