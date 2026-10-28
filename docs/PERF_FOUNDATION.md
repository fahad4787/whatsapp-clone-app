# Performance & UX foundation (pre–Wave 13)

Date: 2026-10-24  
Status: **F0–F6** — F6 low-end chat smoothness (2026-10-26).

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

### F6 — Low-end chat smoothness — **Done**
1. Isolate chat presence / voice levels so open chat does not full-recompose  
2. Coil decode size caps + lower cache on low-RAM devices  
3. Home list: no per-row `find`; pager swipe progress only in bottom nav  
4. Release R8 minify enabled  
5. `material-icons-extended` deferred (wide icon usage; trim in a later pass)

**Exit:** usable scroll/idle on Mate-class devices.

### F7 — Deeper low-end — **Done**
1. Composer owns draft text — typing no longer recomposes message list  
2. Lazy MediaPlayer for voice (create on play); slower progress tick  
3. Home: unread-only flow at shell; ChatsTab collects list itself  
4. Lighter bottom-nav shadow; status ring = border (no Canvas sweep)  
5. In-memory message window 60 → 40

### F8 — Screen-change feel (measured on Mate + Vivo) — **Done**
Evidence (Mate gfxinfo, post-settle): high input-latency counts + 99th% spikes on navigate.
1. Nav transitions → none (fade composed 2 screens)  
2. Home pager: instant snap; swipe re-enabled  
3. Logo animation off by default; release R8 only (debug minify off)  
4. Defer home/chat HTTP ~80–100ms after disk paint  
5. `scripts/perf-nav-jank.sh` for repeatable jank checks  

### F8.1 — Chat open crash + flash (2026-10-26)
Vivo FATAL: duplicate LazyColumn key on open. Fixed: `distinctBy { id }` in store + UI; sync disk hydrate before paint; hide list until scrolled to latest.

### F8.2 — Chat open black flash (2026-10-26)
Cause was client: `listReady` kept LazyColumn at `alpha=0` until `scrollToItem` (felt like a black second).  
Fix: `reverseLayout = true` + newest-first items so latest paints on frame 1; removed alpha gate; defer message HTTP ~250ms after paint.

### F9 — Chat fling on low-end (2026-10-28)
Mate/Vivo scroll still heavy after window grew to 80.
1. In-memory message window **40** again (disk cache can stay 80); soft older-page cap **60**
2. Coil low-RAM threshold ≤256MB heap; 16-bar voice waveform

### F9.1 — Less logic, more like WhatsApp (2026-10-28)
Over-engineering was the jank: mid-fling UI mode flips + new lambdas every row every frame.
1. **Removed** scroll CompositionLocal / image placeholders (caused hitch + recompose storm)
2. **Stable** list handlers (`remember { fun … }`) so Lazy items can skip
3. Auto-scroll only on new msg / IME — **not** on typing
4. Voice seek gestures only while playing

### F10 — Chat fling cut (2026-10-28)
Mate gfxinfo still ~75% janky after F9.1 — bubbles were still too heavy + list never skipped.
1. `@Immutable` on `LocalMessage` (+ nested types) so list rows can skip
2. **Plain-text fast path** (no clip / combinedClickable / MaterialTheme / vector ticks)
3. Typing indicator **outside** list + **static** dots (no InfiniteTransition)
4. Voice: Canvas waveform → flat progress track
5. In-memory / API window **28** (soft older cap +12)
6. **Column + verticalScroll** instead of LazyColumn — window is tiny; recycle was the hitch
7. No overscroll glow; older-page load only after fling settles

### F11 — App-wide scroll parity (2026-10-28)
Same Mate lessons applied outside chat:
1. Shared `AivoraNoOverscroll` / `AivoraScrollColumn`
2. Home chats / updates / calls — no overscroll; slim `HomeChatRow`
3. SettingsList + short pickers → Column scroll
4. PeoplePicker, New call, Scheduled, Channels, Message info, Starred, Media grid, Status viewers, Invite, Login countries

### F12 — History + open/close + calls (2026-10-28)
1. Chat list back to **LazyColumn reverseLayout** (fast open; only visible rows)
2. In-memory window **60** (soft +40); API page 60 / older 40 — top history loads again
3. Defer WS join / meta / mark-read so open paints first
4. Calls tab → **Column scroll** + slim `CallHistoryRow`

### F13 — WhatsApp-style native scroll (2026-10-28)
Compose LazyColumn cannot match WA fling on Mate-class GPUs.
1. Hot lists → **RecyclerView** via `AndroidView` (`RecyclerHost`)
2. Chat: plain bubbles = Views; rich/system = rare `ComposeView`
3. Home chats + Calls: View row adapters + DiffUtil; locked-chats footer via `ConcatAdapter`
4. `itemAnimator = null`, overscroll never, fixed-size + view pool
5. Mate + Vivo debug install OK. Warm Mate gfxinfo: **chat ~17% janky** (was ~75% LazyColumn); calls ~49%; home ~80% (Compose chrome above list still costly). Cold/first-open numbers higher — prefer warm fling for compare.
6. Chat list = **Views only** (no nested ComposeView in RV — that painted into the header). Chronological + `stackFromEnd`; open snaps to latest; WA-style ProgressBar via `ChatHistoryLoaderAdapter` when paging older.

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
