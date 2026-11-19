# Wave 51 — Generate chat transcripts

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 51.1 | `ChatTranscripts.downloadAndShareZip` — multi-chat text exports in one zip |
| 51.2 | Settings → Chat transcripts — multi-select + Generate |
| 51.3 | Entry points from Chats settings + Chat history |

## Notes

- Text only (`media=0`). Single-chat with media remains Export chat.
- Zip layout mirrors backup: `manifest.json` + `chats/{name}/_chat.txt`.

## Deploy

APK only.

## Parked (later)

FIDO2, SMTP, E2EE, encrypted cloud backup. (Silence unknown callers → Wave 52.)
