# Wave 49 — Block unknown messages

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 49.1 | `POST /api/conversations/direct` returns `403 unknown_blocked` when peer has Advanced → Block unknown On and no existing DM |
| 49.2 | Client maps `unknown_blocked` (QR / link / New chat) to clear copy |
| 49.3 | Advanced footnote describes real behavior |

## Notes

- Existing DIRECT chats are unchanged — only **new** inbound conversation creates are blocked.
- You can still start chats with anyone; the setting protects you from strangers starting one with you.
- Orthogonal to Links privacy (Wave 39) and block list.

## Deploy

Backend + APK.

## Parked (later)

FIDO2, SMTP, E2EE, encrypted cloud backup. (Open backup browser → Wave 50.)
