# Screen map (from NavHost `ay4.java`)

## Auth / onboarding
- `login`
- `profile-setup`
- `your-details`
- `transfer/new-phone`

## Main tabs / core
- `home`
- `chat/{id}`
- `locked-chats`
- `channels`
- `channel/{slug}`
- `community/{id}`
- `profile/{id}`

## Create / actions
- `new-chat`
- `new-group?with={with}`
- `new-call`
- `new-status?camera={camera}&gallery={gallery}`
- `scheduled-calls?open={open}&chat={chat}`
- `event-create/{chat}`
- `event-details/{chat}/{message}`
- `event-edit/{chat}/{message}`
- `message-info/{chat}/{message}`

## Settings
- `settings/profile`
- `settings/account` (hub)
- `settings/chats` (hub)
- `settings/qr`
- `settings/starred`
- `settings/storage`
- `settings/two-step`
- `settings/communities`
- `settings/blocked`
- `settings/devices`
- `settings/devices/link`
- `settings/permissions`
- `settings/delete-account`

### Account
- `settings/account/add`
- `settings/account/email`
- `settings/account/passkeys`
- `settings/account/change-number`
- `settings/account/deleted`
- `settings/account/request-info`
- `settings/account/security-notifications`
- `settings/account/two-step/new-pin`

### Chats
- `settings/chats/archived`
- `settings/chats/backup`
- `settings/chats/backup/encrypted`
- `settings/chats/backup/frequency`
- `settings/chats/backup/location`
- `settings/chats/backup/open`
- `settings/chats/export`
- `settings/chats/font-size`
- `settings/chats/history`
- `settings/chats/transcripts`
- `settings/chats/transfer`

### Privacy
- `settings/privacy`
- `settings/privacy/status`
- `settings/privacy/last-seen`
- `settings/privacy/profile-photo`
- `settings/privacy/about`
- `settings/privacy/groups`
- `settings/privacy/calls`
- `settings/privacy/default-timer`
- `settings/privacy/advanced`
- `settings/privacy/app-lock`
- `settings/privacy/chat-lock`
- `settings/privacy/chat-lock/secret-code?hide={hide}`
- `settings/privacy/checkup`
- `settings/privacy/live-location`

### Lists
- `settings/lists/new`
- `settings/lists/edit/{id}`
- `settings/lists/reorder`

## Entry activities
- `MainActivity` → Compose `NavHost` (`home` after login)
- `IncomingCallActivity` → full-screen incoming call UI
