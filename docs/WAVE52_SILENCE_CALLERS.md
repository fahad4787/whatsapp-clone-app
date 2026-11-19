# Wave 52 — Silence unknown callers

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 52.1 | `createAndRingCall` skips WS + FCM ring for callees with Silence unknown On and no prior DIRECT / shared conversation |
| 52.2 | Client `CallSilence` suppresses ring UI if a notify still arrives |
| 52.3 | Privacy → Calls footnote matches real behavior |

## Notes

- Call row still created; timeout → missed in history for the callee.
- Group / existing-DM calls (with `conversation_id`) always ring.
- Messaging unaffected — use Block unknown (Wave 49) for that.

## Deploy

Backend + APK.

## Parked (later)

FIDO2, SMTP, E2EE, encrypted cloud backup.
