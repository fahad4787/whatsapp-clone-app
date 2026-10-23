# Wave 14 — Chat extras

Date: 2026-10-24  
Status: **Done**

## Scope

| # | Feature | Status |
|---|---------|--------|
| 14.1 | Contact card message | Done |
| 14.2 | Location pin | Done |
| 14.3 | Polls (create, vote, live tallies) | Done |
| 14.4 | Events (create + RSVP) | Done |
| 14.5 | Disappearing messages (per-chat TTL) | Done |
| 14.6 | Last-seen + online privacy | Done |

## Backend

- Schema: `MessageType` CONTACT / LOCATION / POLL / EVENT; `Message.payload` + `expiresAt`; `Conversation.disappearingSeconds`; `User.lastSeenAt` / `lastSeenPrivacy` / `onlinePrivacy`
- Migration: `20261024020000_wave14_chat_extras`
- Send accepts `contact` / `location` / `poll` / `event` bodies; sets preview text + optional `expiresAt` from chat TTL
- `POST /api/messages/:id/vote`, `POST /api/messages/:id/rsvp`
- `PUT /api/conversations/:id/disappearing` (`0` / `86400` / `604800` / `7776000`)
- `GET` / `PUT /api/me/privacy`
- Conversation peer includes privacy-gated `last_seen_at` + `presence_visible`
- WS close touches `lastSeenAt`
- Tests: `messages.wave14.test.ts`

## Android

- Typed send/response models (`PollCreateBody`, `ContactCardDto`, `LocationBody`, `EventCreateBody`, vote/rsvp/disappearing/privacy)
- `LocalMessage` rich payloads + bubbles in `RichMessageBubbles.kt`
- Attach → Contact / Location / Poll / Event wired to API
- Contact info + Group info: disappearing dialog
- Settings → Last seen and online: loads/saves privacy
- Chat subtitle respects `peerPresenceVisible` + last seen

## Exit checks

- [ ] Share contact → peer sees card
- [ ] Share location → peer sees pin
- [ ] Create poll → peer votes → tallies update (WS/refresh)
- [ ] Create event → RSVP Going/Maybe/Can't
- [ ] Set disappearing 24h → new messages carry `expires_at`
- [ ] Privacy Nobody → peer no longer shows Online / last seen
