# Wave 16 — Calls (LiveKit Cloud)

Date: 2026-10-26  
Status: **16.2 in progress**  
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
| 16.2 | Polish: WhatsApp-like UI, sync end/decline, single timer, history + Calls tab | In progress |
| 16.3 | Group / scheduled calls | Later |
| 16.4 | Admin live-calls view | Later |

## 16.2 use cases

1. Start voice/video from chat header  
2. Incoming full-screen Accept / Decline (ringtone)  
3. Decline → both sides leave (no connect)  
4. Cancel / hang up while ringing → peer dismisses  
5. No answer timeout → both leave + missed in history  
6. In-call: mute, speaker/camera, hang up — icon-only controls, one timer  
7. Calls tab: live history (all / missed / voice / video) + call back  

## APIs

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/calls` | start |
| GET | `/api/calls/:id` | detail |
| POST | `/api/calls/:id/:action` | `answer` \| `reject` \| `end` |
| GET | `/api/calls/history` | recent calls for me |

## Exit checks (16.2)

- [ ] Decline never puts either side into Connected  
- [ ] Cancel/no-answer clears the other device’s call UI  
- [ ] One timer only when live  
- [ ] Calls tab shows real history; tap redials  
