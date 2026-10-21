# Figma → Aivora Screen Map

**File:** `EWBSJebF5KWRv6BIGzq1VO` · Page Design `0:1`
**Brand in app:** Aivora (never Bolta Punjab in UI strings)

Status: `todo` | `wip` | `pass` | `deferred`

## Primary frames (Waves 2–6, 8–9)

| Wave | Frame | nodeId | Feature | Route | Status |
|------|-------|--------|---------|-------|--------|
| 2 · Home shells | Chats · Mobile | `15:885` | `home` | `Routes.Home` | wip |
| 2 · Home shells | Updates · Browse | `22:7920` | `home` | `Routes.Home` | todo |
| 2 · Home shells | Calls · Mobile | `55:6764` | `home` | `Routes.Home` | todo |
| 2 · Home shells | My Profile · Mobile | `160:11428` | `home` | `Routes.Home` | wip |
| 3 · Conversation | Conversation · Mobile | `33:3379` | `chat` | `Routes.ChatId` | todo |
| 3 · Conversation | Group Conversation · Mobile | `106:9702` | `chat` | `Routes.ChatId` | todo |
| 4 · Calls deep | Schedule · Empty state | `213:12906` | `calls` | `Routes.ScheduledCalls` | todo |
| 4 · Calls deep | Schedule · New call | `213:12847` | `calls` | `Routes.ScheduledCalls` | todo |
| 4 · Calls deep | Keypad · Voice dialer | `213:13580` | `calls` | `Routes.NewCall` | todo |
| 4 · Calls deep | Audio call · Mobile | `49:2801` | `calls` | `(add in-call)` | todo |
| 4 · Calls deep | Video call · Mobile | `49:2883` | `calls` | `(add in-call)` | todo |
| 5 · Profile edit | Profile details · Mobile | `160:11871` | `profile` | `Routes.YourDetails` | todo |
| 5 · Profile edit | Edit profile picture · Mobile | `160:11747` | `profile` | `Routes.YourDetails` | todo |
| 6 · Settings hubs | Appearance · Overview | `412:11908` | `settings` | `Routes.SettingsAppearance` | todo |
| 6 · Settings hubs | Notifications · Overview | `412:13285` | `settings` | `Routes.SettingsNotifications` | todo |
| 6 · Settings hubs | Account · Overview | `412:8851` | `settings` | `Routes.SettingsAccount` | todo |
| 6 · Settings hubs | Privacy · Overview | `412:10040` | `settings` | `Routes.SettingsPrivacy` | todo |
| 6 · Settings hubs | Chats settings · Overview | `412:10147` | `settings` | `Routes.SettingsChats` | todo |
| 6 · Settings hubs | Storage and data · Overview | `412:14733` | `settings` | `Routes.SettingsStorage` | todo |
| 8 · Attachments & create | Create · Camera | `22:8320` | `camera` | `Routes.Camera` | todo |
| 8 · Attachments & create | Create · Photos and videos | `22:8455` | `camera` | `Routes.Camera` | todo |
| 8 · Attachments & create | Poll · Create poll | `412:5262` | `events` | `(poll create)` | todo |
| 8 · Attachments & create | Event · Create event | `412:4930` | `events` | `Routes.EventCreateChat` | todo |
| 8 · Attachments & create | Location · Share location | `412:6357` | `chat` | `(location sheet)` | todo |
| 9 · Help / Invite / leftovers | Help and feedback · Menu | `412:15259` | `settings` | `Routes.SettingsPermissions` | todo |
| 9 · Help / Invite / leftovers | Invite a friend · Contacts | `412:14994` | `settings` | `(invite)` | todo |

## Wave 7 — Settings deep (sample; full list in Figma metadata)

Privacy children, Account children, Backup/Transfer, Manage storage, Lists, Starred, Passkeys, Two-step, Email, Change number, Delete account, Proxy, Network usage, Auto-download — see Design canvas top-level frames named `Privacy · …`, `Account · …`, `Chat backup · …`, `Manage storage · …`, etc.

## Wave notes

- Duplicate frames (e.g. multiple `Calls · Mobile`) — use lowest id as canonical unless a later frame is clearly the polished variant.
- After each wave, update Status column and run `graphify . --update` in `aivora/`.
