# Architecture

## Current phase: frontend first

The Compose UI runs with **local mock data** (`ui/mock/MockData`).  
**Backend / Firebase wiring is deferred** until the frontend is signed off.

Saved backend discussion plan (later): Cursor plan `backend_rebuild_discussion_a85c9718`.

When wiring data later, choose either:

- **Custom API** matching recovered Retrofit paths in `AivoraApi`, or
- **Firebase** (Auth / Firestore / Storage / FCM) — requires rewriting the client data layer

Target topology (future):

```
┌─────────────┐     HTTPS      ┌──────────────────────┐
│  Compose UI │ ─────────────► │ API host (TBD)       │
│  NavHost    │                │  /api/...             │
└──────┬──────┘                └──────────────────────┘
       │
       │  WSS
       ▼
┌──────────────────────┐
│ ws host (TBD) /ws/   │  typing, presence, receipts
└──────────────────────┘
```

## Layers

1. **ui** — Compose screens + navigation (`Routes` / `AivoraNavHost`) + mocks
2. **data.api** — Retrofit `AivoraApi` + OkHttp auth interceptor (not wired yet)
3. **data.model** — Serializable DTOs (field names from APK serializers)
4. **data.realtime** — Client/Server WebSocket frames (not wired yet)

## Auth (from APK — future)

- Bearer access token on requests (`Authorization: Bearer …`)
- Refresh via `POST api/auth/refresh`
- OTP: `api/auth/otp/*`, email, passkeys, two-step PIN

## Calls (future)

- LiveKit tokens from `CallTokenResult` (`token`, `livekit_url`)
- Native libs were present in APK (`liblivekit_uniffi.so`) — add LiveKit Android SDK when implementing calls
