# Wave 57 — Media visibility → gallery

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md) § BD

## Shipped

| Slice | What |
|-------|------|
| 57.1 | `MediaGallery` — MediaStore write to Pictures/Aivora · Movies/Aivora |
| 57.3 | Opening a photo with visibility On auto-publishes; Save always publishes |
| 57.4 | Chats settings subtitle clarifies send/open behavior |

## Notes

- View-once never hits the gallery.
- Explicit Save works even when Media visibility is Off.
- Documents/audio are not published.

## Deploy

APK only.

## Parked (later)

FIDO2, E2EE, encrypted cloud backup, admin TOTP, real multi-account, peer chat transfer.
| 57.2 | Outgoing image/video published when Media visibility On (not view-once) |
