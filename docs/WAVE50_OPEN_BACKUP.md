# Wave 50 — Open backup browser

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 50.1 | `BackupZipBrowser` — parse `manifest.json` + `chats/*/\_chat.txt` |
| 50.2 | Settings → Chat backup → Open backup — pick zip or local auto-backup |
| 50.3 | Share individual chat `.txt` transcripts via share sheet |

## Notes

- Does **not** re-upload / restore chats onto the server (sign-in still does that).
- Accepts Wave 29/48 Aivora backup zips (`aivora-backup-v1`).

## Deploy

APK only.

## Parked (later)

FIDO2, SMTP, E2EE, encrypted cloud backup. (Generate transcripts → Wave 51.)
