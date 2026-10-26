# Wave 17 — Export chat

Date: 2026-10-27  
Status: **Done**

## Goal

Settings → Chat history → Export chat, and in-chat ⋮ → Export chat, produce a real shareable file (Bolta parity).

## API

| Method | Path | Notes |
|--------|------|-------|
| GET | `/api/conversations/:id/export?media=0\|1` | Member-only |

- `media=0` → `text/plain` WhatsApp-style transcript (`.txt`)
- `media=1` → `application/zip` with `_chat.txt` + local `media/` files when available on disk
- Honours `clearedAt`, per-user hides; view-once / deleted / expired stay redacted
- Cap: 50 000 messages

## Android

- `ChatExport.downloadAndShare` → cache + FileProvider share sheet
- Settings export: pick chat → Without media / Include media
- Chat ⋮ → Export chat → same choice dialog

## Exit checks

- [x] Non-member → 404  
- [x] Text export contains messages  
- [x] Zip export returns PK magic  
- [x] Share sheet opens from Settings + chat menu  
