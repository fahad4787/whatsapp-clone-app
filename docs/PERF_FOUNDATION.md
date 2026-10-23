# Performance & UX foundation (pre–Wave 13)

Date: 2026-10-23  
Status: **Active** — do this before more feature waves. Later refactors get much harder.

Goal: **WhatsApp-class feel on low-RAM phones** — cold start, chat open, scroll, icons, session, and data loading.

---

## Diagnosed issues (current code)

| # | Symptom | Root cause |
|---|---------|------------|
| 1 | Chat opens then visibly scrolls top → bottom | `animateScrollToItem` after nav slide + when messages hydrate from empty → full |
| 2 | Long-press sheet icons look black / different from ⋮ menu | Sheet uses flat `InkMuted` Material icons; overflow uses Primary-tinted circles |
| 3 | Slow / empty first paint | Home waits on network; no disk cache of conversations |
| 4 | Profile setup doesn’t stick | `ProfileSetupScreen` never calls `PUT /api/me` |
| 5 | OTP feels required often | Session path exists (`hasSession` → Home) but cold start still does heavy work; debug rebuilds / failed refresh can feel like “logged out” |
| 6 | Janky nav | Fade + slide on every push; chat waits `AivoraNavSlideMs` before settling list |
| 7 | Duplicate menu logic | Chat ⋮ rebuilds actions instead of sharing `ChatControls` with home long-press |

---

## Icon consistency (now vs later)

**Now:** same visual language for both menus — Primary accent circle + same Material `ImageVector`s.

**Later (Figma / drawable pack):** replace Material with brand drawables. Names to commission:

| Action | Material used today | Suggested drawable name |
|--------|---------------------|-------------------------|
| View contact / info | `Person` | `ic_aivora_person` |
| Search | `Search` | already `ic_aivora_search` |
| Media | `Photo` | `ic_aivora_media` |
| Mute / Unmute | `NotificationsOff` / `NotificationsActive` | `ic_aivora_mute` / `ic_aivora_unmute` |
| Pin / Unpin | `PushPin` | `ic_aivora_pin` |
| Archive / Unarchive | `Archive` / `Unarchive` | `ic_aivora_archive` / `ic_aivora_unarchive` |
| Block / Unblock | `Block` | `ic_aivora_block` |
| Report | `Flag` | `ic_aivora_flag` |
| Clear chat | `Delete` | `ic_aivora_delete` |

---

## Waves (foundation only)

### F0 — Critical UX bugs (this pass)
1. Chat open: **instant** jump to latest message; animate only for new msgs while near bottom  
2. Unify sheet + overflow icon chrome (circles + Primary)  
3. Wire **Profile setup** → `PUT /api/me`; skip setup if display name already set  
4. Session: keep Home on `hasSession`; only Login when tokens gone  

**Exit:** open long chat → already at bottom; long-press icons match ⋮; new user name saves and shows in Your Details.

### F1 — Instant home (disk cache)
1. Persist last conversation list (JSON in prefs / DataStore) after each successful fetch — **Done** (`ChatListCache`)  
2. On Home open: paint cache **first**, then refresh in background — **Done**  
3. Persist last ~N messages per open chat for instant reopen — **Done** (`MessageCache`, 80 msgs × 30 chats)

**Exit:** cold start with session shows chat list &lt; 300ms without waiting on network. — **Shipping**

### F2 — Startup budget
1. Defer FCM / notification permission until after first frame  
2. Single shared OkHttp/Retrofit; stop recreating clients in poll loops  
3. Splash / window background matches home so no white flash  
4. Optional: `baselineProfile` / R8 keep rules later  

**Exit:** TTI to Home chrome &lt; 1s on mid device with session.

### F3 — Smooth motion (lightweight)
1. Default nav = **short fade only** (drop slide on low-end / reduced motion)  
2. Respect animator duration scale  
3. No scroll animation fighting enter transition  

**Exit:** screen changes feel light; no slide+scroll fight in chat.

### F4 — Code reuse (waves 9–12 cleanup)
1. Chat ⋮ menu built from `ChatControls` (same as home long-press)  
2. Shared `MediaUploader` / message bubble helpers already started — finish  
3. One `ChatRepository` façade over store + API + cache  

**Exit:** one place to change mute/pin/archive/block UI + behavior.

### F5 — Memory & polling
1. Cap in-memory messages per chat; page older history  
2. Slow home poll when app backgrounded; WS is source of truth when connected  
3. Coil memory/disk cache tuned; recycle bitmaps  

**Exit:** stable on 2–3GB devices with 50+ chats.

---

## Auth / profile product rules

```
Cold start
  ├─ hasSession? ──yes──► Home (cache → refresh)
  │                        if me.display_name blank → optional soft prompt ProfileSetup
  └─ no ──► Login (OTP) ──► ProfileSetup (PUT /me) ──► Home

Your Details ──► PUT /me + avatar APIs (already) ──► reflected in home/header
```

OTP is **only** for no session / logout / banned / refresh dead — not every launch.

---

## Suggested order

```
F0 bugs → F1 cache → F2 startup → F3 motion → F4 reuse → F5 memory
```

Then resume **Wave 13** (groups).

---

## Success metrics

- [ ] Session user never sees OTP on normal relaunch  
- [ ] Chat open lands on latest message with no top→bottom animation  
- [ ] Home long-press and chat ⋮ icons look the same family  
- [ ] Profile name from setup appears in Your Details + peers  
- [ ] Home paints from cache before network returns  
