# Wave 8 — Thin admin (moderation)

## APIs

| Method | Path | Who |
|--------|------|-----|
| GET | `/api/admin/users?q=` | Admin — search users |
| POST | `/api/admin/users/:id/ban` | Admin — ban + revoke refresh tokens |
| POST | `/api/admin/users/:id/unban` | Admin |
| GET | `/api/admin/reports?status=open` | Admin |
| POST | `/api/admin/reports/:id/resolve` | Admin — `{ action: resolve\|dismiss, ban_user? }` |
| GET | `/api/admin/metrics` | Admin — users, messages, open reports |
| POST | `/api/reports` | App user — file a report |

## Admin UI

- Dashboard: live metrics + recent OTPs + open reports  
- Users: search / ban / unban  
- Reports: open queue, resolve / dismiss / ban+resolve  

## App

Chat overflow → **Report** posts to `/api/reports`.

## Verify

1. Admin login → Users → ban a test phone → OTP request returns `banned`.  
2. App: Report a chat → admin Reports → Resolve.  
3. Dashboard counts match DB.  
