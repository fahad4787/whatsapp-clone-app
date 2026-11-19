# Wave 33 — App lock & permissions

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 33.1 | Privacy → App lock — biometric / device credential via `androidx.biometric` |
| 33.2 | Lock gate after cold start / leaving foreground (`ProcessLifecycleOwner`) |
| 33.3 | Settings → Permissions — live system grant status; tap opens app settings |
| 33.4 | Passkeys screen — honest “coming later” + link to app lock |

## Notes

- `MainActivity` is now a `FragmentActivity` (required by BiometricPrompt).
- App lock is local-device only (not a server passkey).

## Deploy

APK only (no backend migrate).

## Parked (later)

FIDO2 passkeys, QR device link, SMTP, encrypted cloud backup, E2EE, admin TOTP. (Auto-download → Wave 34.)
