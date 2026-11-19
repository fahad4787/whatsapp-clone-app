# Wave 26 — Live location

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 26.1 | Start live share via location message payload (`live`, duration 15m/1h/8h) |
| 26.2 | `PUT /api/messages/:id/live-location` pings; `GET/POST /api/me/live-locations*` |
| 26.3 | Android: share sheet, foreground GPS pings (~30s), Privacy stop-all |

## Notes

- Thin: updates only while the app process is alive (no background service).
- Last pin remains after stop/expiry (not a disappearing message).
- Static one-shot location unchanged.

## Parked (later)

Background live location service, passkeys, backup, E2EE. (Admin firewall + channel moderation → Wave 27.)
