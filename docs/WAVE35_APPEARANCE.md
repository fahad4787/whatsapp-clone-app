# Wave 35 — Appearance (theme + default wallpaper)

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 35.1 | Theme: System / Light / Dark via `AppearancePrefs` (local) |
| 35.2 | `AivoraTheme` honors theme mode (not only system) |
| 35.3 | Default chat wallpaper (mint / dusk / sand / default) for chats without per-chat theme |
| 35.4 | Appearance + Chats settings show live labels; **Reset appearance** clears theme, wallpaper, font |

## Notes

- Local-device prefs only (no server sync).
- Per-chat wallpaper (chat ⋮) still overrides the default.

## Deploy

APK only (no backend migrate).

## Parked (later)

FIDO2 passkeys, QR device link, SMTP, encrypted cloud backup, E2EE, admin TOTP. (Notifications → Wave 36.)
