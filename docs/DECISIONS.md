# Backend decisions (locked)

Date: 2026-10-22  
Source: Backend Scope Discussion plan (recommended defaults).

| Decision | Choice |
|----------|--------|
| Stack | **Custom API** (Fastify + Prisma + Postgres + Redis + object storage) — matches recovered `AivoraApi` |
| Not chosen | Firebase (would force rewriting the Android data layer) |
| Hosting (now) | **Render free/trial** (staging); upgrade later for prod |
| OTP | **Dev OTP** — show code in admin + logs; real SMS later |
| MVP cut | Feature waves 0–8 in `BACKEND_PLAN.md` (foundation → admin) |
| Calls (LiveKit) | **LiveKit Cloud Build (free)** — Wave 16; self-host later if needed |
| Also deferred | Communities, passkeys, backup/transfer, stickers, live location, real SMS |
| Quality | Feature-by-feature; tests + Semgrep CI; no next wave until gate passes |

## MVP includes

- Phone OTP (dev) + JWT access/refresh
- Profile (`/api/me`)
- DM + group text messaging
- Media presign upload
- WebSocket: typing, presence, receipts
- FCM push (Android) — needs Firebase project at that wave
- Admin: login, OTP viewer, users, bans, reports, basic metrics

## Plan

See **`BACKEND_PLAN.md`** — one feature at a time, test, then next.

## Architecture (one backend, two clients)

```
Android app  ──┐
               ├──►  aivora-backend  (same Postgres / Redis / storage)
Admin portal ──┘
```

- **One API** serves both the mobile app and `aivora-admin`.
- Admin uses the same backend with **admin-role** JWT (not a separate database).
- App users cannot call admin routes; admins manage users/content via admin UI.

## Repos

| Repo | Role |
|------|------|
| `aivora/` | Android app (user client) |
| `aivora-backend/` | Shared API + WebSocket for app **and** admin |
| `aivora-admin/` | Web console to manage the product |
