# Performance & UX foundation (pre–Wave 13)

Date: 2026-10-24  
Status: **F0–F5 complete** — resume Wave 13 (groups) next.

Goal: **WhatsApp-class feel on low-RAM phones** — cold start, chat open, scroll, icons, session, and data loading.

---

## Waves (foundation only)

### F0 — Critical UX bugs — **Done**
1. Chat open: instant jump to latest message  
2. Unify sheet + overflow icon chrome  
3. Profile setup → `PUT /api/me`  
4. Session: Home when `hasSession`  

### F1 — Instant home (disk cache) — **Done**
1. `ChatListCache` + paint-first refresh  
2. `MessageCache` (80 msgs × 30 chats)  

### F2 — Startup budget — **Done**
1. FCM / notification permission after `awaitFrame` + short settle  
2. Single shared OkHttp/Retrofit (`NetworkModule`)  
3. Splash `bg_app` matches Compose light/dark (`#0B141A` night)  
4. Optional baselineProfile / R8 — deferred  

**Exit:** TTI to Home chrome &lt; 1s on mid device with session.

### F3 — Smooth motion — **Done**
1. Fade-only nav (`NavTransitions`)  
2. Nav duration follows `ANIMATOR_DURATION_SCALE` (`MotionPrefs`)  
3. Instant `scrollToItem` on open (no slide fight)  

### F4 — Code reuse — **Done**
1. Chat ⋮ built from `ChatControls.overflowMenuItems` (same as home sheet)  
2. `MediaUploader` helpers in place  
3. `ChatRepository` façade over API + store + cache  

### F5 — Memory & polling — **Done**
1. 60-msg memory window; scroll-up loads older via `before=` (API returns latest page)  
2. Home/chat HTTP poll only while `STARTED` + WS down  
3. Coil 3 ImageLoader: 18% memory / 48MB disk, no crossfade  

**Exit:** stable on 2–3GB devices with 50+ chats.

---

## Free-tier backend vs smoothness

| What paid/always-on hosting fixes | What it does **not** fix |
|----------------------------------|--------------------------|
| Cold-start delays (Render free sleep) | Chat **scroll** jank (Compose / RAM) |
| Slow first API after idle | Heavy bubbles / too many list items |

---

## Suggested order (complete)

```
F0 → F1 → F2 → F3 → F4 → F5  ✓
```

Then **Wave 13** (groups).

---

## Success metrics

- [x] Session user never sees OTP on normal relaunch  
- [x] Chat open lands on latest message with no top→bottom animation  
- [x] Home long-press and chat ⋮ icons look the same family  
- [x] Profile name from setup appears in Your Details + peers  
- [x] Home paints from cache before network returns  
- [x] Shared HTTP client; polls pause in background  
- [x] Older history pages on scroll-up  
