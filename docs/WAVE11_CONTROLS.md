# Wave 11 — Conversation controls

Date: 2026-10-23  
Status: Shipping

## Features

| # | Feature | How |
|---|---------|-----|
| 11.1 | Archive | Chat menu → Archive; home Archived filter |
| 11.2 | Mute 8h | Chat menu → Mute; FCM skipped while muted |
| 11.3 | Pin (max 3) | Chat menu → Pin; pinned sort to top |
| 11.4 | Block | Chat menu → Block; Settings → Blocked; send returns 403 |
| 11.5 | Search | Chat menu → Search; filters loaded messages |

## API

- `PUT /api/conversations/:id/settings` `{ archived?, pinned?, muted?, muted_until? }`
- `GET|POST /api/blocks`, `DELETE /api/blocks/:userId`
- `GET /api/conversations/:id/messages?q=`
