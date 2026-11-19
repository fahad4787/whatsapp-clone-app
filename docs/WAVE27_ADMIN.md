# Wave 27 — Admin channels, stickers & firewall

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)  
**Closes:** Wave 20.5 (deferred until Channels + Stickers shipped)

## Shipped

| Slice | What |
|-------|------|
| 27.1 | Admin channels CRUD + list/verified toggles; hide posts (`deletedAt`) |
| 27.2 | Admin stickers read-only catalog from seeded emoji packs |
| 27.3 | Admin IP allowlist (`admin_ip_allowlist` in AppConfig); empty = allow all |
| 27.4 | Enforce allowlist on `POST /api/admin/auth/login` (`403 ip_blocked`) |

## Backend

| Endpoint | Auth | Notes |
|----------|------|--------|
| `GET/POST /api/admin/channels` | admin | List / create |
| `PATCH/DELETE /api/admin/channels/:id` | admin | Update / delete |
| `GET /api/admin/channels/:id/posts` | admin | Includes hidden |
| `DELETE …/posts/:postId` | admin | Soft-hide |
| `GET /api/admin/stickers` | admin | Seed catalog DTO |
| `GET/PUT /api/admin/firewall` | admin | Allowlist + `client_ip` |

Unlisted channels stay out of `GET /api/channels` (`listed: true` filter). Hidden posts stay out of public post feeds (`deletedAt: null`).

## Admin UI

Live (no mock store): **Channels**, **Stickers**, **Firewall**.

## Deploy

Push backend + redeploy admin frontend. No new Prisma migration (reuses Channel + AppConfig JSON).

## Parked (later)

Image sticker uploads, passkeys, backup, E2EE, background live location. (Legal / Security → Wave 28.)
