package com.aivora.app.data

data class HelpArticle(
    val id: String,
    val title: String,
    val subtitle: String,
    val body: String,
)

object HelpArticles {
    val all: List<HelpArticle> = listOf(
        HelpArticle(
            id = "get-started",
            title = "Get started",
            subtitle = "Set up your account and start chatting",
            body = """
                Sign in with your phone number and the one-time code we send (or the staging code when SMS is off).

                After login, set your name and photo under Edit profile. Open New chat to find people already on Aivora, or share your QR code so friends can add you.

                Home tabs: Chats, Updates (status), Calls, and My Profile for settings.
            """.trimIndent(),
        ),
        HelpArticle(
            id = "chats-media",
            title = "Chats and media",
            subtitle = "Messages, photos, documents, and more",
            body = """
                Open a chat to send text, photos, voice notes, documents, stickers, polls, events, and location.

                Long-press a message for reply, react, star, forward, or delete. Use Share (+) for attachments.

                Storage and data controls auto-download and photo quality. Export or back up chats from Chats settings when you need a portable copy.
            """.trimIndent(),
        ),
        HelpArticle(
            id = "calls",
            title = "Calls and video",
            subtitle = "Voice, video, and scheduled calls",
            body = """
                Start a voice or video call from a chat header or the Calls tab. Incoming calls show a full-screen overlay when the app is open.

                Create a call link from Calls ⋮ so others can join the same room. Scheduled calls appear in your Calls history until they start or expire.
            """.trimIndent(),
        ),
        HelpArticle(
            id = "privacy",
            title = "Privacy and security",
            subtitle = "Controls that protect your account",
            body = """
                Privacy settings control last seen, profile photo, about, status audience, groups, links (who can open your QR), and read receipts.

                App lock uses your device biometric or PIN. Chat lock hides selected chats behind a secret code. Two-step verification adds a PIN at login.

                Blocked contacts cannot message you. Request account info downloads a metadata report from the server.
            """.trimIndent(),
        ),
        HelpArticle(
            id = "notifications",
            title = "Notifications",
            subtitle = "Manage alerts and sounds",
            body = """
                Turn notifications on or off from Settings. Inside Notifications you can change the notification tone, conversation tones, high-priority banners, reaction alerts, message preview, and vibrate patterns for messages, groups, and calls.

                Mute a single chat from the chat menu when you need quiet without changing global prefs.
            """.trimIndent(),
        ),
        HelpArticle(
            id = "account-backup",
            title = "Account and backup",
            subtitle = "Number, email, and chat backup",
            body = """
                Change number, add a recovery email, or delete your account under Account settings. Linked devices shows companion sessions you can revoke.

                Chat backup builds a zip you can share to another app or drive. Signing in on a new phone restores live data from the server; the zip is a portable archive, not encrypted cloud restore yet.
            """.trimIndent(),
        ),
    )

    fun byId(id: String): HelpArticle? = all.find { it.id == id }
}
