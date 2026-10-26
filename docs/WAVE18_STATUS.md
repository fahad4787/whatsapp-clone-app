# Wave 18 — Status / Updates

Date: 2026-10-27  
Status: **Done**

## Goal

Updates tab is server-backed: post, feed, view, viewers, privacy, 24h expiry.

## API

| Method | Path | Notes |
|--------|------|-------|
| GET | `/api/status` | Feed rows (`StatusRowDto[]`); contacts + blocks |
| POST | `/api/status` | Create; `expires_at` = now+24h |
| POST | `/api/status/:id` | Mark viewed |
| GET | `/api/status/:id/viewers` | Owner-only |
| DELETE | `/api/status/:id` | Owner |
| POST | `/api/status/:id/reaction` | Upsert reaction |
| GET/PUT | `/api/me/privacy` | Adds `status`, `status_ids` |

Audience: `everyone` \| `contacts` \| `contacts_except` \| `only_share`  
Contacts = DIRECT conversation peers. WS `status.new` notifies eligible peers. Expired rows filtered; 15‑min cleanup deletes them.

## Android

- `StatusRepository` + `StatusCache` replace seed-only store
- Updates tab refreshes when logged in
- New status uploads media + background; viewer marks seen / shows viewers / delete
- Settings → Privacy → Status saves via me/privacy (+ people multi-select for except/only)

## Exit checks

- [x] Peer with DIRECT chat sees status; stranger does not  
- [x] Block hides status  
- [x] Viewers list for owner  
- [x] Expired filtered  
- [x] Mate/Vivo install  
