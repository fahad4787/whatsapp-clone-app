# Wave 32 — Account info report & security prefs

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 32.1 | `GET /api/me/data` — JSON account report (profile, privacy, devices, chat list meta; **no** message bodies) |
| 32.2 | Android: Request account info → download + share sheet |
| 32.3 | Security notifications toggle persists per device |

## Notes

- Report is metadata only; use Wave 29 backup / Wave 17 export for messages.
- Security-notif preference is local until E2EE identity-key alerts exist.

## Deploy

Backend + APK. No migration.

## Parked (later)

FIDO2 passkeys, QR link, SMTP, encrypted cloud backup, E2EE, admin TOTP. (App lock → Wave 33.)
