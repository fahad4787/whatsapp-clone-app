# Wave 28 — Admin Legal / Security + Help docs

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 28.1 | Persist privacy + terms in AppConfig; admin Legal page live |
| 28.2 | `GET /api/legal` public; Android Help → Terms / Privacy screens |
| 28.3 | Admin Security: session hours, lock-after-fail, fail/lock thresholds |
| 28.4 | Login uses session TTL; failed logins lock (in-memory) when enabled |

## Backend

| Endpoint | Auth | Notes |
|----------|------|--------|
| `GET /api/legal` | public | `privacy_policy`, `terms_of_service` |
| `GET/PUT /api/admin/legal` | admin | Edit docs + audit |
| `GET/PUT /api/admin/security` | admin | Persist policy + audit |

`require_2fa` is stored but TOTP is **not** enforced yet (API returns a note when on).

## Admin UI

Live (no mock store): **Legal**, **Security**.

## Android

Help and feedback → Terms of service / Privacy policy open server copy.

## Deploy

Push backend + redeploy admin; ship APK for Help screens. No Prisma migration.

## Parked (later)

Admin TOTP 2FA, passkeys, E2EE, background live location, image sticker uploads. (Chat backup zip → Wave 29.)
