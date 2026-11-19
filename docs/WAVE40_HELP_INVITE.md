# Wave 40 — Help center & invite

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 40.1 | Help Center articles (6 topics) with real body copy |
| 40.2 | Contact us → email compose with app version + user hint |
| 40.3 | App info — version / build + legal links |
| 40.4 | Invite a friend — profile QR HTTPS link; share / copy |
| 40.5 | Device contacts list + per-row SMS invite |

## Notes

- Invite link = `https://…/u/{userId}` (same as Wave 38 profile QR).
- Contacts require `READ_CONTACTS`; denial still allows share-link flow.

## Deploy

APK only (no backend migrate).

## Parked (later)

FIDO2, live camera QR, SMTP, encrypted cloud backup, E2EE, admin TOTP, custom chat lists. (Defaults → Wave 41.)
