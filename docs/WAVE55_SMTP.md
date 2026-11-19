# Wave 55 — SMTP email OTP

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md) § BB

## Shipped

| Slice | What |
|-------|------|
| 55.1 | `mail.ts` + env (`SMTP_HOST` / `PORT` / `SECURE` / `USER` / `PASS` / `FROM`) |
| 55.2 | `issueEmailOtp` sends via nodemailer when host+from set; logs plaintext only when SMTP off |
| 55.3 | `otp_dev_bypass` / `000000` only when SMTP is not configured |
| 55.4 | `502 smtp_send_failed` on delivery errors; Settings email screen inbox hint when bypass off |

## Notes

- Mirrors Twilio SMS: optional env, no SMTP = staging log + bypass.
- Auth enumeration stubs return the same `otp_dev_bypass` shape as a real issue.
- Does not change APK flows beyond the verify-email helper text.

## Deploy

Backend redeploy with SMTP env (or leave unset for prior staging behavior). APK optional for inbox copy.

## Parked (later)

FIDO2, E2EE, encrypted cloud backup, admin TOTP, real multi-account, peer chat transfer. (Device link QR → Wave 56.)
