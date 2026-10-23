# Wave 15 — Chat polish

Date: 2026-10-24  
Status: **Done**

## Scope

| # | Feature | Status |
|---|---------|--------|
| 15.1 | Edit message (15 min) + “Edited” label | Done |
| 15.2 | Message info (real delivered / read lists) | Done |
| 15.3 | Per-chat wallpaper | Done |
| 15.4 | Favourites (separate from pin) | Done |
| 15.5 | View-once image | Done |

## Backend

- Migration `20261024030000_wave15_chat_polish`
- `Message.editedAt`, `viewOnce`; `MessageReceipt`; `MessageViewOnceOpen`
- `ConversationMember.favoritedAt`, `wallpaper`
- `PUT /api/messages/:id` (edit), `GET /api/messages/:id/info`, `POST /api/messages/:id/view-once`
- Settings PUT: `favorite`, `wallpaper`
- Tests: `messages.wave15.test.ts`

## Android

- Edit from long-press; “Edited” on bubble
- Info screen loads real receipts
- Chat wallpaper picker (Default / Mint / Dusk / Sand)
- Favourites toggle + home Favourites filter uses `favorite` only
- Attach → View once photo

## Exit checks

- [ ] Edit own text within 15m → peer sees update + Edited
- [ ] Message info shows who delivered / read (1:1 and group)
- [ ] Wallpaper persists after reopen
- [ ] Favourite shows in Favourites filter without pinning
- [ ] View-once opens once then “Opened”
