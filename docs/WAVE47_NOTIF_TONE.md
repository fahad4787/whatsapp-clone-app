# Wave 47 — Notification tone picker

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 47.1 | `NotificationPrefs.msgSoundUri` + `soundLabel` |
| 47.2 | Settings → Notifications → Notification tone (system ringtone picker) |
| 47.3 | `MessageNotifier` recreates Messages channel with custom sound; builder uses URI |

## Notes

- Android O+ channel sound is immutable — we delete/recreate the `messages` channel when the tone changes or prefs reset.
- Conversation tones Off still silences sound regardless of the picked URI.
- Call ringtone shipped in Wave 53.

## Deploy

APK only.

## Parked (later)

FIDO2, SMTP, E2EE, encrypted cloud backup, per-chat notification tones. (Scheduled backup → Wave 48; call ringtone → Wave 53.)
