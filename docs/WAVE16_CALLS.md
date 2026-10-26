# Wave 16 — Calls (LiveKit Cloud)

Date: 2026-10-26  
Status: **16.1 shipped (needs LiveKit Cloud env on Render)**  
Hosting: **LiveKit Cloud free (Build) tier**

## Setup (you)

1. Create a project at [cloud.livekit.io](https://cloud.livekit.io) (Build plan, $0).
2. Copy **WebSocket URL**, **API Key**, **API Secret**.
3. Set on Render + local `aivora-backend/.env`:

```
LIVEKIT_URL=wss://<your-project>.livekit.cloud
LIVEKIT_API_KEY=APIxxxxxxxx
LIVEKIT_API_SECRET=xxxxxxxx
```

## Scope

| # | Feature | Status |
|---|---------|--------|
| 16.1 | 1:1 audio/video: start → ring (WS) → answer → LiveKit join → end | Done (code) |
| 16.2 | Call history + missed on Calls tab | Later |
| 16.3 | Group / scheduled calls | Later |
| 16.4 | Admin live-calls view | Later |

## APIs

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/calls` | `{ conversation_id, media: audio\|video }` → `CallTokenResult` |
| GET | `/api/calls/:id` | Call DTO |
| POST | `/api/calls/:id/:action` | `answer` \| `reject` \| `end` → token on answer |

## WS frames

| Type | To | Data |
|------|-----|------|
| `call.incoming` | Callee | call + caller |
| `call.answered` | Caller | call |
| `call.ended` | Both | call + reason |
| `call.rejected` | Caller | call |

## Android

- Chat header call/video → `POST /api/calls` → in-call screen with LiveKit
- Incoming → accept/reject overlay; accept joins LiveKit room
- Mute / speaker / camera toggle / hang up

## Exit checks (16.1)

- [ ] Two devices: A calls B → B sees incoming → answer → both hear audio
- [ ] Reject ends for caller
- [ ] Hang up ends for both
- [ ] Video call publishes camera when permission granted
