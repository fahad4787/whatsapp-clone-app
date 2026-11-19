# Wave 39 — Links privacy & read receipts

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 39.1 | Prisma: `linksPrivacy`, `readReceipts` |
| 39.2 | Privacy GET/PUT expose `links` + `read_receipts` |
| 39.3 | Links screen — Everyone / My contacts / Nobody (gates profile QR open) |
| 39.4 | Read receipts toggle persists; reciprocal — blue ticks only if both allow |
| 39.5 | Privacy hub shows live trailing labels |

## Notes

- **Contacts** for Links = already have a direct chat (not phone-book sync).
- Default Links = `contacts`; set **Everyone** so strangers can start a chat from your QR.

## Deploy

Backend migrate `20261105080000_wave39_privacy_links_receipts` + APK.

## Parked (later)

FIDO2, live camera QR, SMTP, encrypted cloud backup, E2EE, admin TOTP. (Help / invite → Wave 40.)
