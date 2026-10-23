# Wave 10 — Reply, react, star, forward

Date: 2026-10-23  
Status: Shipping

## Backend

- Schema: `Message.replyToId`, `Message.forwarded`, `MessageStar`, `MessageReaction`
- Migration: `20261023180000_message_actions`
- Send accepts `reply_to: { id }`
- `POST /api/messages/:id/reactions` `{ emoji }` (toggle same emoji)
- `POST|DELETE /api/messages/:id/star`
- `GET /api/me/starred`
- `POST /api/messages/:id/forward` `{ conversation_id }`
- DTO includes `reply_to`, `reactions`, `starred`, `forwarded`; WS broadcasts updated message on react

## Android

- Long-press → Reply / React / Star / Forward / Delete
- Reply bar on composer; quote strip in bubble
- Reaction chips on bubble; live via WS
- Starred messages screen loads from API
- Forward sheet picks another chat from home list

## Verify on two devices

1. Reply to a message — both see quote
2. React with 👍 — both see chip
3. Star → Settings → Starred messages lists it
4. Forward into another chat — appears with “Forwarded”
