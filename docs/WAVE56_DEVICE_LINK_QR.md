# Wave 56 — Device link QR

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md) § BC

## Shipped

| Slice | What |
|-------|------|
| 56.1 | `QrCodec.linkPayload` / `linkCodeFromPayload` (`aivora://link/<CODE>`) |
| 56.2 | Link-this-device screen shows QR + code |
| 56.3 | Scan QR (CameraX) approves companion link; gallery pick too |
| 56.4 | Link-a-device → “Scan QR code instead” |

## Notes

- Reuses Wave 44 camera stack; profile QR scan still opens DMs.
- Bare 8-char codes only match the backend pairing alphabet (no I/O/0/1).
- Manual code entry remains supported.

## Deploy

APK only.

## Parked (later)

FIDO2, E2EE, encrypted cloud backup, admin TOTP, real multi-account, peer chat transfer. (Media visibility → Wave 57.)
