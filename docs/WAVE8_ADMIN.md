# Wave 8 — Thin admin (moderation)

Extended for chat Waves 9–15 (groups, TTL, media/polls/events, presence, report message context).

## APIs

| Method | Path | Who |
|--------|------|-----|
| GET | `/api/admin/users?q=` | Admin — search users (includes `last_seen_*`, privacy) |
| POST | `/api/admin/users/:id/ban` | Admin — ban + revoke refresh tokens |
| POST | `/api/admin/users/:id/unban` | Admin |
| GET | `/api/admin/reports?status=open` | Admin — embeds reported message previews |
| POST | `/api/admin/reports/:id/resolve` | Admin — `{ action: resolve\|dismiss, ban_user? }` |
| GET | `/api/admin/metrics` | Admin — users, online, chats (direct/group/TTL), media/voice/polls/events |
| GET | `/api/admin/conversations?q=&type=` | Admin — list chats with last message preview |
| GET | `/api/admin/conversations/:id` | Admin — members + recent messages |
| POST | `/api/reports` | App user — file a report |

## Admin UI

- Dashboard: live metrics (online, groups/direct, disappearing, media/voice/polls) + OTPs + open reports  
- Users: search / ban / unban / delete; last seen + privacy columns  
- Conversations: live list + detail (TTL, group flags, message types)  
- Reports: open queue with reported message previews; resolve / dismiss / ban+resolve  

## App

Chat overflow → **Report** posts to `/api/reports`.

## Verify

1. Admin login → Users → ban a test phone → OTP request returns `banned`.  
2. App: Report a chat → admin Reports → see message preview → Resolve.  
3. Conversations → open a group with TTL → detail shows members + types.  
4. Dashboard counts match DB (groups/direct/disappearing/media).  
