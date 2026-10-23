package com.aivora.app.ui.navigation

/** All destinations recovered from APK NavHost (`ay4`). */
object Routes {
    const val ChannelSlug = "channel/{slug}"
    const val Channels = "channels"
    const val ChatId = "chat/{id}"
    const val ChatMedia = "chat/{id}/media"
    const val CommunityId = "community/{id}"
    const val EventCreateChat = "event-create/{chat}"
    const val EventDetailsChatMessage = "event-details/{chat}/{message}"
    const val EventEditChatMessage = "event-edit/{chat}/{message}"
    const val Home = "home"
    const val LockedChats = "locked-chats"
    const val Login = "login"
    const val MessageInfoChatMessage = "message-info/{chat}/{message}"
    const val Camera = "camera/{target}"
    /** APK route kept for backend map; UI uses system photo picker instead of a fake gallery screen. */
    const val Gallery = "gallery/{target}"
    const val PollCreate = "poll-create/{chat}"
    const val LocationShare = "location-share/{chat}"
    const val ContactShare = "contact-share/{chat}"
    const val NewCall = "new-call"
    const val Keypad = "keypad"
    const val AudioCall = "call/audio/{name}"
    const val VideoCall = "call/video/{name}"
    const val NewChat = "new-chat"
    const val NewGroup = "new-group"
    const val NewStatus = "new-status"
    const val StatusId = "status/{id}"
    const val ProfileSetup = "profile-setup"
    const val ProfileId = "profile/{id}"
    const val ScheduledCalls = "scheduled-calls"
    const val SettingsAccount = "settings/account"
    const val SettingsAccountAdd = "settings/account/add"
    const val SettingsAccountChangeNumber = "settings/account/change-number"
    const val SettingsAccountDeleted = "settings/account/deleted"
    const val SettingsAccountEmail = "settings/account/email"
    const val SettingsAccountPasskeys = "settings/account/passkeys"
    const val SettingsAccountRequestInfo = "settings/account/request-info"
    const val SettingsAccountSecurityNotifications = "settings/account/security-notifications"
    const val SettingsAccountTwoStepNewPin = "settings/account/two-step/new-pin"
    const val SettingsBlocked = "settings/blocked"
    const val SettingsChats = "settings/chats"
    const val SettingsChatsArchived = "settings/chats/archived"
    const val SettingsChatsBackup = "settings/chats/backup"
    const val SettingsChatsBackupEncrypted = "settings/chats/backup/encrypted"
    const val SettingsChatsBackupFrequency = "settings/chats/backup/frequency"
    const val SettingsChatsBackupLocation = "settings/chats/backup/location"
    const val SettingsChatsBackupOpen = "settings/chats/backup/open"
    const val SettingsChatsExport = "settings/chats/export"
    const val SettingsChatsFontSize = "settings/chats/font-size"
    const val SettingsChatsHistory = "settings/chats/history"
    const val SettingsChatsTranscripts = "settings/chats/transcripts"
    const val SettingsChatsTransfer = "settings/chats/transfer"
    const val SettingsCommunities = "settings/communities"
    const val SettingsDeleteAccount = "settings/delete-account"
    const val SettingsDevices = "settings/devices"
    const val SettingsDevicesLink = "settings/devices/link"
    const val SettingsListsEditId = "settings/lists/edit/{id}"
    const val SettingsListsNew = "settings/lists/new"
    const val SettingsListsReorder = "settings/lists/reorder"
    const val SettingsPermissions = "settings/permissions"
    const val HelpFeedback = "help/feedback"
    const val HelpCenter = "help/center"
    const val InviteFriend = "invite/friend"
    const val SettingsPrivacy = "settings/privacy"
    const val SettingsPrivacyAbout = "settings/privacy/about"
    const val SettingsPrivacyAdvanced = "settings/privacy/advanced"
    const val SettingsPrivacyAppLock = "settings/privacy/app-lock"
    const val SettingsPrivacyCalls = "settings/privacy/calls"
    const val SettingsPrivacyChatLock = "settings/privacy/chat-lock"
    const val SettingsPrivacyChatLockSecretCode = "settings/privacy/chat-lock/secret-code"
    const val SettingsPrivacyCheckup = "settings/privacy/checkup"
    const val SettingsPrivacyDefaultTimer = "settings/privacy/default-timer"
    const val SettingsPrivacyGroups = "settings/privacy/groups"
    const val SettingsPrivacyLastSeen = "settings/privacy/last-seen"
    const val SettingsPrivacyLiveLocation = "settings/privacy/live-location"
    const val SettingsPrivacyProfilePhoto = "settings/privacy/profile-photo"
    const val SettingsPrivacyStatus = "settings/privacy/status"
    const val SettingsProfile = "settings/profile"
    const val SettingsQr = "settings/qr"
    const val SettingsStarred = "settings/starred"
    const val SettingsStorage = "settings/storage"
    const val SettingsManageStorage = "settings/storage/manage"
    const val SettingsAppearance = "settings/appearance"
    const val SettingsNotifications = "settings/notifications"
    const val SettingsTwoStep = "settings/two-step"
    const val TransferNewPhone = "transfer/new-phone"
    const val YourDetails = "your-details"

    fun chatMedia(id: String): String =
        ChatMedia.replace("{id}", android.net.Uri.encode(id))

    fun camera(target: String = "status"): String =
        Camera.replace("{target}", android.net.Uri.encode(target))

    fun gallery(target: String = "status"): String =
        Gallery.replace("{target}", android.net.Uri.encode(target))

    fun pollCreate(chat: String): String =
        PollCreate.replace("{chat}", android.net.Uri.encode(chat))

    fun locationShare(chat: String): String =
        LocationShare.replace("{chat}", android.net.Uri.encode(chat))

    fun contactShare(chat: String): String =
        ContactShare.replace("{chat}", android.net.Uri.encode(chat))

    fun eventCreate(chat: String): String =
        EventCreateChat.replace("{chat}", android.net.Uri.encode(chat))

    fun status(id: String): String = StatusId.replace("{id}", id)

    fun audioCall(name: String = "Contact"): String =
        AudioCall.replace("{name}", android.net.Uri.encode(name))

    fun videoCall(name: String = "Contact"): String =
        VideoCall.replace("{name}", android.net.Uri.encode(name))
}
