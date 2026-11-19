# Wave 29 — Chat backup (portable zip)

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 29.1 | `GET /api/me/backup?media=0\|1` — zip of all chats (`manifest.json` + per-chat `_chat.txt` [+ media]) |
| 29.2 | Android: Back up → download + share sheet; real last/size |
| 29.3 | Prefs: include media, Wi-Fi only, frequency label (preference only) |

## Notes

- Not Google Drive / cloud sync. Share sheet saves to Files / Drive / etc.
- New phone restore = sign in (server is source of truth). File restore parked.
- Encrypted cloud backup still parked. Scheduled auto-backup → Wave 48.

## Parked (later)

Passkeys, encrypted cloud backup, file restore, E2EE, admin TOTP. Scheduled WorkManager → Wave 48. (Account email → Wave 30.)
