# Aivora — Reconstructed Android App

Polished **Jetpack Compose** project rebuilt from APK reverse-engineering.

> This is **not** byte-identical original source. The release APK was R8-obfuscated.
> Structure, routes, API paths, DTO field names, theme, and assets are evidence-backed.
> Screen *layouts* for most destinations are stubs you fill in using `docs/UI_STRINGS.txt` + the running APK.

## Evidence sources

| Source | Role |
|--------|------|
| jadx decompile of `aivora-1.apk` | Classes, serializers, NavHost |
| [REA (morluto/rea)](https://github.com/morluto/rea) | Android APK analysis workflow (JADX-backed); confirmed same approach |
| Recovered resources | Fonts, brand mark, colors, launcher |

**Backend:** `https://wa.zirvik.com/` · **Realtime:** `wss://wa.zirvik.com/ws/`

## Project structure

```
aivora/
├── app/src/main/
│   ├── java/com/aivora/app/
│   │   ├── AivoraApp.kt
│   │   ├── ui/
│   │   │   ├── MainActivity.kt
│   │   │   ├── theme/          # Brand colors + Inter typography
│   │   │   ├── navigation/     # 67 routes from APK NavHost
│   │   │   ├── screens/        # Login + Home polished; others stubbed
│   │   │   └── components/
│   │   └── data/
│   │       ├── api/            # 139 Retrofit endpoints
│   │       ├── model/          # 112 kotlinx.serialization DTOs
│   │       └── realtime/       # WebSocket frame models
│   └── res/                    # Fonts, brand, mipmaps (from APK)
└── docs/
    ├── ROUTES.txt
    ├── SCREENS.md
    └── UI_STRINGS.txt          # ~2200 recovered UI copy strings
```

## Theme (from APK)

- Primary: `#1DAA61`
- Dark background: `#111B21`
- Font: Inter (regular / medium / semibold / bold)
- Brand mark: `res/drawable/aivora_mark.xml` (+ `drawable-nodpi/brand_mark.png` for raster)

## Open in Android Studio

1. **File → Open** → `Whatsapp Clone/aivora`
2. Sync Gradle (needs Android SDK + JDK 17)
3. Run on emulator/device

Start destination is **Login** → Profile setup → **Home**.

## Frontend coverage

**All 67 NavHost destinations have real Compose screens** (no empty placeholders):

- Login (phone + OTP) · Profile setup · Home (Chats / Status / Communities / Calls)
- Chat thread · Channels · Channel detail · Communities · Contact profile
- New chat / group / call / status · Scheduled calls · Locked chats
- Events create/edit/details · Message info · Chat transfer
- Full Settings tree (account, privacy, chats/backup, devices, lists, storage, QR, permissions, two-step, blocked, delete, …)

| Layer | Status |
|-------|--------|
| Package / Gradle / theme / fonts / icons | Ready |
| Navigation (67 routes) | Ready |
| Every screen UI | Implemented (demo data; polish iteratively) |
| API (139) + DTOs (112) | Ready |
| Auth / Room / LiveKit / FCM | Not wired yet |

## Related folders (raw recovery)

- `../aivora-decompiled` — full jadx dump
- `../aivora-frontend` — Compose-obfuscated classes
- `../aivora-recovered` — earlier package extract

## REA note

[REA](https://github.com/morluto/rea) wraps JADX for APK inspection (`inspect-android-package`, class/method decompile).
It improves *guided* analysis; it cannot restore original Kotlin names without an R8 mapping file.
We already ran equivalent JADX extraction; this repo is the cleaned reconstruction.
