# Wave 48 — Scheduled chat backup

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 48.1 | `ChatBackup.downloadZip` / `downloadAuto` — durable zip under `filesDir/backups` (keep 5) |
| 48.2 | `ChatBackupWorker` + `ChatBackupScheduler` (WorkManager periodic) |
| 48.3 | Frequency Daily / Weekly / Monthly / Off + Wi-Fi-only constraint reschedule |
| 48.4 | Low-priority “Chat backup saved” notification |

## Notes

- Still not Google Drive cloud sync or encrypted backup.
- WorkManager may defer runs (Doze / battery). Manual **Back up** still uses the share sheet.
- Auto zips are private to the app; uninstall removes them.

## Deploy

APK only (`work-runtime-ktx`).

## Parked (later)

FIDO2, SMTP, E2EE, encrypted cloud backup, file restore. (Block unknown → Wave 49.)
