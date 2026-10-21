package com.aivora.app.data.local

data class MockChat(
    val id: String,
    val title: String,
    val preview: String,
    val time: String,
    val unread: Int = 0,
    val isGroup: Boolean = false,
)

data class MockMessage(
    val id: String,
    val mine: Boolean,
    val body: String,
    val time: String,
)

object MockData {
    val chats = listOf(
        MockChat("1", "Ahmad Khan", "Yes, joining in 5.", "9:41 PM", unread = 2),
        MockChat("2", "Family Group", "Fatima: See you tomorrow", "8:12 PM", isGroup = true),
        MockChat("3", "Aivora", "Welcome to Aivora", "Yesterday"),
        MockChat("4", "Usman Raza", "Photo", "Yesterday", unread = 1),
        MockChat("5", "Work", "Meeting notes shared", "Monday", isGroup = true),
        MockChat("6", "Zainab Noor", "Voice message", "Sunday"),
        MockChat("7", "Hassan Malik", "Location shared", "Saturday"),
        MockChat("8", "Community Announcements", "New post in General", "Friday", isGroup = true),
    )

    fun chat(id: String): MockChat =
        chats.find { it.id == id } ?: MockChat(id, "Chat", "Say hello", "Now")

    fun messagesFor(chatId: String): List<MockMessage> = when (chatId) {
        "2" -> listOf(
            MockMessage("m1", false, "Who's bringing dessert?", "7:58 PM"),
            MockMessage("m2", true, "I can pick something up.", "8:01 PM"),
            MockMessage("m3", false, "Fatima: See you tomorrow", "8:12 PM"),
        )
        else -> listOf(
            MockMessage("m1", false, "Assalam o Alaikum!", "9:38 PM"),
            MockMessage("m2", true, "Walikum Assalam — how's it going?", "9:39 PM"),
            MockMessage("m3", false, "All good. Are we still on for the call?", "9:40 PM"),
            MockMessage("m4", true, "Yes, joining in 5.", "9:41 PM"),
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
