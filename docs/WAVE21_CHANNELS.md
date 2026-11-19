# Wave 21 — Channels & communities

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 21.1 | Channels: list/search/create, follow/unfollow, posts, react, report |
| 21.2 | Communities: list/create, detail, join linked group |
| 21.3 | Thin seed on first GET (`aivora-news`, `aivora-updates`, Local Neighbors) |

## Backend

- Migration `20261105030000_wave21_channels`
- Routes: `/api/channels*`, `/api/communities*`
- Admin moderate (hide/ban) deferred

## Parked (later)

Live location, backup, passkeys, E2EE, admin firewall, channel admin moderation. (Stickers + chat lock → Wave 25.)
