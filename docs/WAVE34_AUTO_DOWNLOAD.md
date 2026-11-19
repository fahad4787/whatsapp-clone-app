# Wave 34 — Media auto-download & data prefs

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 34.1 | Prisma: `autoDlMobile` / `autoDlWifi` / `autoDlRoaming` / `lessDataCalls` / `photoUploadQuality` |
| 34.2 | `GET`/`PATCH /api/me/auto-download` |
| 34.3 | Storage and data — network / auto-download / photo quality pickers persist |
| 34.4 | Prefs included in `GET /api/me/data` account report |

## Notes

- Prefs sync to the server (multi-device). Client download enforcement can tighten later.
- Defaults: mobile=`photos`, wifi=`all`, roaming=`none`, quality=`auto`.

## Deploy

Backend migrate `20261105070000_wave34_auto_download` + APK.

## Parked (later)

FIDO2 passkeys, QR device link, SMTP, encrypted cloud backup, E2EE, admin TOTP.
