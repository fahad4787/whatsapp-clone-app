# Wave 41 — Default timer & advanced privacy

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 41.1 | `GET`/`PATCH /api/me/preferences` — default disappearing + advanced toggles |
| 41.2 | Default message timer UI persists (Off / 24h / 7d / 90d) |
| 41.3 | New DIRECT chats inherit creator’s default timer |
| 41.4 | Advanced privacy toggles persist; **Disable link previews** skips unfurl on send |
| 41.5 | Privacy hub shows live disappearing trailing label |

## Notes

- Protect IP enforced as LiveKit ICE RELAY in Wave 45; block unknown enforced in Wave 49.
- Existing chats are unchanged when you change the default timer.

## Deploy

Backend migrate `20261105090000_wave41_defaults_advanced` + APK.

## Parked (later)

FIDO2, live camera QR, SMTP, E2EE. LiveKit force-relay → Wave 45. (Custom lists → Wave 42.)
