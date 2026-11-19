# Wave 45 — Protect IP → LiveKit force-relay

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 45.1 | `CallPrivacyPrefs` — local mirror of Advanced → Protect IP |
| 45.2 | `LiveKitCallController.connect` uses ICE `RELAY` when Protect IP is on |
| 45.3 | Advanced toggle updates local + server prefs; honest footnote |

## Notes

- Relay needs LiveKit/TURN configured on the call backend; without TURN, Protect IP On may fail to connect media.
- Off restores default ICE (host + srflx + relay as offered by server).
- HTTP proxy (Wave 43) does not carry LiveKit media — this flag does.

## Deploy

APK only (prefs already on server from Wave 41).

## Parked (later)

FIDO2, SMTP, E2EE, scheduled/encrypted backup. (List drag reorder → Wave 46.)
