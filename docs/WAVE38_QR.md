# Wave 38 — Profile QR

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 38.1 | My QR — encodes `aivora://user/{id}` (zxing); share HTTPS `/u/{id}` |
| 38.2 | Scan QR — pick image → decode → open / create DM |
| 38.3 | Deep links `aivora://user/{id}` + `https://…/u/{id}` → OpenUser screen |
| 38.4 | `GET /api/users/:id` + public HTML `/u/:id` landing |
| 38.5 | Show name on QR page toggle (`QrPrefs`) |

## Notes

- Gallery scan shipped here; live camera → Wave 44.
- Not companion device QR (Wave 31); this is contact profile QR.

## Deploy

Backend + APK (`zxing:core`).

## Parked (later)

Live camera QR → Wave 44. FIDO2, SMTP, encrypted cloud backup, E2EE, admin TOTP. (Links / read receipts → Wave 39.)
