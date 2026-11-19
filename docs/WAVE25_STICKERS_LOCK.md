# Wave 25 — Stickers & chat lock

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 25.1 | Emoji sticker packs (`GET /api/stickers`) + send (`POST /api/conversations/:id/stickers`) |
| 25.2 | Chat lock PIN (`/api/me/chat-lock*`) + per-chat `locked` setting |
| 25.3 | Android: sticker picker in attach sheet; lock/unlock in chat menu; Locked chats unlock |

## Notes

- Stickers are thin emoji packs (no media hosting).
- Locking a chat requires a secret code first (Privacy → Chat lock).
- Locked chats leave the home list; open via Locked chats footer + PIN.

## Parked (later)

Image/animated sticker packs, passkeys, backup, E2EE, admin firewall, channel admin moderation. (Live location → Wave 26.)
