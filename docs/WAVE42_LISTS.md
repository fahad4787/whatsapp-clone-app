# Wave 42 — Custom chat lists

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 42.1 | Prisma `ChatList` + `ChatListMember` |
| 42.2 | `GET/POST/PATCH/DELETE /api/me/lists` + `PUT /api/me/lists/order` |
| 42.3 | Settings — create list (name + chats), edit/rename/delete, reorder |
| 42.4 | Home filter bar shows up to 6 custom lists |

## Notes

- Default Home chips (All / Unread / Groups / Archived) unchanged.
- Favourites remains the star-based builtin (overflow menu).

## Deploy

Backend migrate `20261105100000_wave42_chat_lists` + APK.

## Parked (later)

FIDO2, live camera QR, SMTP, E2EE. Drag reorder → Wave 46. (Proxy → Wave 43.)
