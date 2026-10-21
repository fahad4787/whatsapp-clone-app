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
)

data class LocalMessage(
    val id: String,
    val mine: Boolean,
    val body: String,
    val time: String,
    val mediaUri: String? = null,
    val senderName: String? = null,
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
            LocalMessage("m2", true, "Yes! Chai and a long walk?", "4:25 PM"),
            LocalMessage("m3", true, "This view + chai. Sunday sorted?", "4:26 PM"),
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
