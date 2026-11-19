# Wave 53 — Call ringtone picker

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md) § AZ

## Shipped

| Slice | What |
|-------|------|
| 53.1 | `NotificationPrefs.callSoundUri` + shared `soundLabel` |
| 53.2 | Settings → Notifications → Calls → Ringtone (system ringtone picker) |
| 53.3 | `MessageNotifier` recreates Calls channel with custom sound; `showCall` uses URI |
| 53.4 | Reset notification settings also recreates Calls channel |

## Notes

- Same O+ channel immutability pattern as Wave 47 Messages tone.
- Ringtone picker uses `TYPE_RINGTONE` (message tone uses `TYPE_NOTIFICATION`).
- Call vibrate prefs still apply independently of the ringtone URI.

## Deploy

APK only.

## Parked (later)

SMTP email delivery, FIDO2, E2EE, encrypted cloud backup. (Transfer/Add-account honesty → Wave 54.)
