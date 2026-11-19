# Wave 37 — Chat prefs & archive all

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 37.1 | `ChatComposerPrefs` — Enter is send, media visibility, keep chats archived |
| 37.2 | Composer: Enter/Send IME when Enter-is-send on; multiline when off |
| 37.3 | Keep archived off → inbound message unarchives chat on home |
| 37.4 | Archived chats → **Archive all** (confirm → marks every chat archived + API) |

## Notes

- Archive-all uses existing `PUT …/settings` per chat (no new endpoint).
- Media visibility gallery publish shipped in Wave 57.

## Deploy

APK only (no backend migrate).

## Parked (later)

FIDO2 passkeys, encrypted cloud backup, E2EE, admin TOTP. (Profile QR → Wave 38; media visibility → Wave 57.)
