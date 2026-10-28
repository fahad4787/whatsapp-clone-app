# Wave 16 — Calls (LiveKit Cloud)

Date: 2026-10-27  
Status: **16.4 done** — next roadmap: [`WAVE17_PLUS.md`](WAVE17_PLUS.md)  
Hosting: **LiveKit Cloud free (Build) tier**

## Setup

```
LIVEKIT_URL=wss://….livekit.cloud
LIVEKIT_API_KEY=API…
LIVEKIT_API_SECRET=…
```

## Scope

| # | Feature | Status |
|---|---------|--------|
| 16.1 | 1:1 audio/video + ring/answer/end | Done |
| 16.2 | Polish: Bolta call UI, sync end/decline, history + Calls tab, system notices | Done |
| 16.3 | Group / scheduled calls | Done |
| 16.4 | Admin live-calls view | Done |

## 16.4 — Admin live calls

### APIs

| Method | Path | Notes |
|--------|------|-------|
| GET | `/api/admin/calls/live` | RINGING + ACTIVE |
| GET | `/api/admin/calls` | History; `?q=&status=&media=&limit=` |
| POST | `/api/admin/calls/:id/end` | Force-end (`admin_force_end`) |

Metrics also expose `calls_active` and `calls_today`.

### Admin UI

- **Live calls** (`/live`): SSE stream (`GET /api/admin/calls/stream?token=`), Force end, no Refresh button  
- **Call history** (`/calls`): search + filters; reloads on call stream events  
- **Dashboard**: active/today call counters from stream + 30s metrics poll  

### Realtime

Call create / answer / reject / end / timeout / admin force-end emit `callBus.changed()` → SSE clients get a fresh live snapshot.

### Exit checks

- [x] Live list shows ringing/active rooms  
- [x] Force-end notifies participants via `call.ended`  
- [x] History searchable by phone/name/room/id  
- [x] Metrics include call counters  
