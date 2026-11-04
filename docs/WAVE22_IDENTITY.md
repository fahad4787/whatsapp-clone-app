# Wave 22 — Account & identity

**Status:** Done (2026-11-05)  
**Goal:** Store-ready account surfaces — linked devices revoke, two-step PIN, delete/change number, optional Twilio SMS.

## APIs

| Method | Path | Notes |
|--------|------|--------|
| GET | `/api/me/devices` | `device_name`, `last_active_at`, `is_current` |
| DELETE | `/api/me/devices/:id` | Revoke device + linked refresh tokens (204) |
| GET | `/api/me/two-step` | `{ enabled, enabled_at, email }` |
| PATCH | `/api/me/two-step` | Body `{ pin, current?, email? }` — enable / change PIN |
| POST | `/api/me/two-step/off` | Body `{ pin }` |
| POST | `/api/auth/otp/verify` | May return `{ two_step_required, challenge }` instead of tokens |
| POST | `/api/auth/two-step` | Body `{ challenge, pin, device? }` → tokens |
| POST | `/api/me/delete` | Body `{ phone }` or `{ country, national_number }` → 204 |
| POST | `/api/me/delete/request` | Confirm helper |
| POST | `/api/me/change-number/request` | Authenticated OTP to new number |
| POST | `/api/me/change-number/verify` | Updates `User.phone` |

## SMS (22.1)

- Admin setting `sms_enabled` + env `SMS_PROVIDER=twilio` + `TWILIO_*` → real SMS.
- Otherwise staging bypass: `OTP_DEV_BYPASS=1` accepts `000000`; codes logged / stored as `devCode`.
- Public config exposes `otp_dev_bypass` and `sms_enabled` so the APK can hide the “dev OTP” hint when SMS is live.

## Android

- **Linked devices** — live list + Sign out → DELETE.
- **Two-step** — status, enable/change PIN, disable with PIN prompt; login step 2 after OTP when required.
- **Delete account** — confirms phone, clears session on 204.
- **Change number** — request/verify OTP against new E.164.

## Exit checks

- [ ] Enable two-step → sign out → OTP → PIN → home
- [ ] Devices list shows this phone; revoke another device
- [ ] Change number updates SessionStore phone
- [ ] Delete account signs out permanently
- [ ] With Twilio + `sms_enabled`, no OTP in logs / no `000000`

## Parked → Wave 24

Passkeys, email recovery reset, companion link, backup chrome.

## Graphify

Regenerated at repo root: `graphify-out/graph.json` (code-only, ~2691 nodes).
