# Wave 31 — Companion device link

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 31.1 | `DeviceLinkSession` + pairing code APIs |
| 31.2 | New device: Login → Link this device → show code → poll → session |
| 31.3 | Phone: Linked devices → Link a device → enter code → approve |

## API

| Endpoint | Auth | Notes |
|----------|------|--------|
| `POST /api/auth/link/start` | public | `{ code, expires_in }` (~3 min) |
| `POST /api/auth/link/approve` | user | Phone confirms code |
| `GET /api/auth/link/approved/:code` | public | `pending` or `{ access, refresh, phone }` once |
| `POST /api/auth/link/cancel` | public | Cancel pending |
| `POST /api/auth/link/claim` | public | Status peek |

## Notes

- Approving creates a Device row via existing session issue helper.
- QR show/scan shipped in Wave 56 (code entry kept).

## Deploy

Migrate `20261105060000_wave31_device_link` + backend + APK.

## Parked (later)

Passkeys, encrypted cloud backup, E2EE, admin TOTP. (Account info report → Wave 32; device link QR → Wave 56.)
