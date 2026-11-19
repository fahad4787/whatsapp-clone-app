# Wave 54 — Transfer / Add-account honesty

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md) § BA

## Shipped

| Slice | What |
|-------|------|
| 54.1 | Transfer chats copy: no fake peer-to-peer QR; points at local backup |
| 54.2 | `TransferNewPhoneScreen` → backup / open-backup steps + CTA |
| 54.3 | Add account: one-session honesty + confirm before sign-out switch |
| 54.4 | Switch-number navigates to Login with cleared back stack |

## Notes

- Does not implement multi-account or live QR transfer — only stops implying they exist.
- Passkeys / email links from Add account stay as this-account security paths.

## Deploy

APK only.

## Parked (later)

FIDO2, E2EE, encrypted cloud backup, real multi-account, peer QR transfer. (SMTP → Wave 55.)
