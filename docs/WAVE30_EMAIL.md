# Wave 30 — Account email & recovery

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 30.1 | Verify recovery email (`GET/POST/DELETE /api/me/email*`) → `twoStepEmail` + `emailVerifiedAt` |
| 30.2 | Two-step PIN reset via email (`/api/auth/two-step/reset/*`) — was 501 |
| 30.3 | Email sign-in OTP (`/api/auth/email/*`) + login “Use email instead” |
| 30.4 | Android Settings → Email address (no fake leslie@) |

## Notes

- Email OTP reuses `OtpChallenge.phone` as the normalized email key + purpose (`email_verify` / `email_login` / `two_step_reset`).
- SMTP delivery shipped in Wave 55 (optional env).

## Deploy

Backend migrate `20261105050000_wave30_email` + redeploy; ship APK.

## Parked (later)

Passkeys (FIDO2), encrypted cloud backup, E2EE, admin TOTP. (Companion device link → Wave 31; SMTP → Wave 55.)
