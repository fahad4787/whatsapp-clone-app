# Backend + admin implementation plan

Locked stack and MVP: see [DECISIONS.md](./DECISIONS.md).  
Android contract: [`AivoraApi.kt`](../app/src/main/java/com/aivora/app/data/api/AivoraApi.kt), DTOs under `data/model/`, WS frames in `RealtimeModels.kt`.

## Goals

1. Ship a usable chat MVP (app + thin admin) in ~6–8 weeks.
2. Keep path shapes and JSON field names compatible with the recovered Retrofit surface.
3. Defer LiveKit calls and heavy settings until after Wave 1.

## Repo layout

```
aivora-backend/
  prisma/schema.prisma
  src/
    index.ts              # Fastify entry
    config.ts
    db.ts
    auth/jwt.ts
    routes/
      health.ts
      auth.ts             # OTP, refresh, logout
      me.ts
      admin/
        auth.ts
        users.ts          # Wave 1+
    ws/                   # Wave 1
  docker-compose.yml      # Postgres + Redis
  .env.example

aivora-admin/
  src/app/login/          # posts to POST /api/admin/auth/login
  src/lib/api.ts          # API base URL helper
```

## Wave 0 — Foundation (this sprint)

Canonical checklist lives in [`BACKEND_PLAN.md`](./BACKEND_PLAN.md). Auth OTP endpoints are **Wave 1** there; this section tracks the older expanded draft.

**Success criteria**

- [x] Decisions + this plan committed in docs
- [x] Scaffold Fastify + Prisma + `/health` (db/redis) + tests + CI + Semgrep config
- [x] Admin dashboard shows API health via `NEXT_PUBLIC_API_URL`
- [ ] `docker compose up` starts Postgres (+ Redis) on your machine
- [ ] `npm run db:migrate` applies Prisma schema against live Postgres
- [ ] Render Blueprint deploy for API + admin
- [ ] (Wave 1) `POST /api/auth/otp/request` + `POST /api/auth/otp/verify` issue tokens
- [ ] (Wave 1) `POST /api/auth/refresh` rotates refresh
- [ ] (Wave 1) `GET /api/me` with Bearer access token
- [ ] (Wave 1) `POST /api/admin/auth/login` returns admin JWT
- [ ] (Wave 1) Admin login page can use real API (`NEXT_PUBLIC_API_URL`)

**Schema (initial)**

- `User` — phone E.164, displayName, about, avatarUrl
- `OtpChallenge` — phone, codeHash, expiresAt, attempts
- `RefreshToken` — userId, tokenHash, deviceId, expiresAt, revokedAt
- `Device` — userId, pushToken, platform, name
- `AdminUser` — email, passwordHash, role (`SUPER` \| `MOD`)
- `AdminAuditLog` — actorId, action, meta (JSON)

**Auth contract (MVP)**

| Method | Path | Body / notes |
|--------|------|----------------|
| POST | `/api/auth/otp/request` | `{ country, national_number }` → `{ phone, expires_in, resend_in }` |
| POST | `/api/auth/otp/verify` | `{ phone, code, device? }` → `{ access, refresh }` (`RefreshResult`) |
| POST | `/api/auth/refresh` | `{ refresh }` → `{ access, refresh }` |
| POST | `/api/auth/logout` | Bearer + optional refresh revoke |
| GET | `/api/me` | Bearer → user profile stub |
| POST | `/api/admin/auth/login` | `{ email, password }` → `{ access, admin }` |

Dev OTP: code logged to server console; fixed accept `000000` when `OTP_DEV_BYPASS=1`.

## Wave 1 — Chat MVP

1. Conversations CRUD (DM + group), members/roles
2. Messages send/list/edit/delete + pagination
3. Read receipts + clear chat
4. Media: S3/R2 presign → attach URL on message
5. WebSocket hub (Redis pub/sub): typing, presence, delivery/read
6. FCM: register `me/devices`, message pushes
7. Privacy flags stored; enforce on profile reads
8. Thin admin: user search, ban, reports queue

**Rough order:** schema conversations/messages → REST send/list → WS → media → FCM → admin users/reports.

## Wave 2+ (after MVP)

Status/stories → blocks/search → channels → settings deep → LiveKit calls → communities → polls/events.

## Admin portal v1

After Wave 0 auth exists:

| Screen | API |
|--------|-----|
| Login | `POST /api/admin/auth/login` |
| Users | list/search, ban, force logout |
| Reports | queue + resolve |
| Metrics | DAU / messages / signups (basic SQL) |
| Channels | create + publish post (when channels land) |

## Local run (Wave 0)

```bash
# Backend
cd aivora-backend
cp .env.example .env
docker compose up -d
npm install
npm run db:migrate
npm run db:seed
npm run dev          # http://localhost:8080

# Admin
cd aivora-admin
# set NEXT_PUBLIC_API_URL=http://localhost:8080
npm run dev          # http://localhost:3000
```

## Verification checklist (Wave 0)

1. Health OK  
2. Request OTP for `+923001234567` → see code in logs  
3. Verify with code → receive `access` + `refresh`  
4. `Authorization: Bearer <access>` on `/api/me`  
5. Refresh rotates tokens  
6. Admin login with seed credentials → dashboard session  
