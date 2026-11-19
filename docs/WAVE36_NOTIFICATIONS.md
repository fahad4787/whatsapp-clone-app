# Wave 36 — Notification prefs

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 36.1 | `NotificationPrefs` — master enable, tones, high priority, reactions, preview, vibrate |
| 36.2 | Settings → Notifications wired + reset |
| 36.3 | Settings home Notifications toggle + opens detail |
| 36.4 | `MessageNotifier` / calls respect prefs (preview hide, silent, vibrate modes) |

## Notes

- Local-device only. Custom tone picker → Wave 47.
- Reaction filter is ready when reaction push events set `isReaction=true`.

## Deploy

APK only (no backend migrate).

## Parked (later)

FIDO2 passkeys, QR device link, SMTP, encrypted cloud backup, E2EE, admin TOTP. Custom tones → Wave 47. (Chat prefs → Wave 37.)
