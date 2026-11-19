# Wave 46 — Drag-reorder chat lists

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 46.1 | Settings → Lists — long-press + drag to reorder custom lists |
| 46.2 | Persist via existing `PUT /api/me/lists/order` on drag end |
| 46.3 | Tap still opens edit; Move up/down rows removed |

## Notes

- Default Home filters (Favourites / Unread / Groups) are not draggable.
- No new deps — Compose `detectDragGesturesAfterLongPress`.

## Deploy

APK only.

## Parked (later)

FIDO2, SMTP, E2EE, scheduled/encrypted backup. (Notification tone → Wave 47.)
