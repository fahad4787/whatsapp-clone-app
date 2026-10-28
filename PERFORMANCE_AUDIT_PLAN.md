# Aivora Android — Performance Audit Plan (Phase 1)

**Date:** 2026-10-27  
**Scope:** `aivora/` (Jetpack Compose app `com.aivora.app.dev`)  
**Phase:** Plan complete; **Phase 2 slices 1–8 shipped** (through M1 Figma icon swap pass 1) — see “Phase 2 progress” below  
**Figma (design contract):** [Bolta Punjab (Copy)](https://www.figma.com/design/EWBSJebF5KWRv6BIGzq1VO/Bolta-Punjab--Copy-?node-id=0-1) — file key `EWBSJebF5KWRv6BIGzq1VO`  
**Prior art:** [docs/PERF_FOUNDATION.md](docs/PERF_FOUNDATION.md) (F0–F8), [docs/BOLTA_PARITY.md](docs/BOLTA_PARITY.md)

---

## Executive summary

Aivora is a **Jetpack Compose** messaging client with a **single-Activity NavHost**, shared OkHttp/Retrofit, Coil 3, Firebase Messaging, LiveKit calls, and an in-memory + SharedPreferences cache layer. Substantial low-end work already shipped (PERF F0–F8): disk-first home/chat paint, message windowing, fade-free nav, Coil caps, release R8.

**Remaining high-impact risks** are less “missing caches” and more:

1. **Binary / install size** (~**75MB** debug APK vs Bolta ~**30MB**).
2. **Mega-screen composables** without ViewModels (`ChatScreens` ~2.6k LOC, `SettingsScreens` ~2.2k, `HomeScreens` ~1.4k) — full message/chat lists collected at screen top.
3. **Cold-start disk hydrate on Main** (chat list prefs + up to 20 message files in `LaunchedEffect`).
4. **Always-on HTTP logging** and **1.5s own-status polling**.
5. **Unconstrained Coil** on status viewer / media grid (chat bubbles already size-capped).
6. **Design-system drift risk**: Material Icons Extended + 280+ `Icons.*` call sites vs Figma custom icons.

Deep-dive corroboration (read-only): [Compose UI audit](3c16bf03-7c3f-47eb-8bb8-5fc6e8299361), [data-layer audit](985ff5a4-ec49-4687-a62e-7609db55dac9).

This phase establishes a prioritized roadmap. **No fixes are applied until you explicitly approve.**

---

## Tooling & baseline

| Tool | Result |
|------|--------|
| Stack | Kotlin 17, Compose BOM `2024.10.01`, minSdk 26, target/compile 35 |
| Graphify | **Refreshed 2026-10-27** via `graphify update .` → **1892 nodes / 5947 edges / 280 communities** (was ~1085 nodes / Oct 21). Backup under `graphify-out/2026-10-27/`. Code AST only (no LLM docs pass). |
| Graphify queries | Hubs include Chat/Home/Settings screens, `AivoraApi`, stores, `NetworkModule`; post-update also covers Status / LiveKit / Realtime paths |
| Build | `:app:assembleDebug` succeeds |
| APK size | Debug APK **~75MB** (`app/build/outputs/apk/debug/app-debug.apk`) |
| Device gfxinfo | Attempted via `scripts/perf-nav-jank.sh`; package reported **frozen** mid-run on Vivo — **no reliable jank percentiles this session**. Re-run required after unfreeze/foreground. |
| ViewModels | **Zero** `ViewModel` usages under `app/src/main/java` |
| Routes | ~**84** route constants; NavHost ~**83** `composable(` destinations |
| Kotlin surface | **218** `.kt` files; ~**24.6k** LOC under `app/src/main/java` |

---

## Graphify findings (aid, not proof)

| Observation | Implication |
|-------------|-------------|
| Post-refresh hubs: **HomeScreens**, **AivoraApi**, **SettingsScreens**, **ChatScreens**, **InMemoryStatusStore**, LiveKit / Realtime | Phase 2 changes in these files still have wide blast radius — stay surgical |
| Status + LiveKit now present (`StatusRepository`, `StatusViewerScreen`, `LiveKitCallController`, `RealtimeClient`, status API methods) | Confirms audit targets are first-class in the current graph |
| Report date **2026-10-27**; built from commit `bdd7bd17` — re-run `graphify update .` after large Phase 2 diffs | Keep graph fresh during implementation |
| Optional: `graphify label` (needs Gemini) to rename “Community N” hubs | Cosmetics only; not required for Phase 2 |

---

## Findings by severity

### Critical

#### C1 — Debug APK ~75MB vs production target ~30MB
- **Files:** `app/build.gradle.kts` (deps: `material-icons-extended`, LiveKit, Firebase, Compose), packaging
- **Evidence:** Measured debug APK **75MB**; [BOLTA_PARITY.md](docs/BOLTA_PARITY.md) cites Bolta ~30MB / Wave **23**. Release minify is on, but size gap likely remains (icons + LiveKit + unused resources). Broad R8 `-keep` for LiveKit/WebRTC/Firebase further limits shrink.
- **Confirmed vs potential:** **Confirmed** size delta for debug; release size **not measured this session** (needs `:app:assembleRelease` + `apkanalyzer`).
- **Impact:** Install time, Play download, low-end storage, cold dex/mmap pressure.
- **Fix (proposed):** After approval — `apkanalyzer` breakdown; replace/trim `material-icons-extended`; ABI splits / bundle; audit LiveKit transitive; enable resource shrink verification.
- **Risk:** Icon visual regressions if Material icons swapped carelessly (must match Figma).

#### C2 — Cold-start hydrate on Main thread
- **Files:** `ui/feature/home/HomeScreens.kt` (~L166–170); `data/ChatRepository.kt` `hydrateRecentFromDisk`; `ChatListCache.load`; up to **20** `MessageCache.load` file reads
- **Evidence:** Disk hydrate invoked from Compose `LaunchedEffect` (Main) without `Dispatchers.IO` wrapper. Data-layer audit confirmed sync prefs + multi-file reads on the UI path.
- **Confirmed vs potential:** **Confirmed** Main-thread sync IO pattern; jank magnitude needs Systrace/`StrictMode` timing on Mate.
- **Impact:** Cold/warm TTI, first-frame stalls on low-RAM devices.
- **Fix:** Move hydrate to `Dispatchers.IO`; paint from empty/partial then apply; keep existing disk-first UX.
- **Risk:** Brief empty flash if ordering wrong — match current paint-first semantics carefully.

---

### High

#### H1 — Mega composable screens / no ViewModel layer + wide `collectAsState`
- **Files:**  
  - `ui/feature/chat/ChatScreens.kt` (~**2615** lines) — `messages by messageFlow.collectAsState()` at ~L205–207 in same composable as menus/search/attach  
  - `ui/feature/settings/SettingsScreens.kt` (~**2236**)  
  - `ui/feature/home/HomeScreens.kt` (~**1415**) — `allChats.collectAsState()` in ChatsTab (~L478); statuses in StatusTab (~L792)  
  - Also large: `ScheduledCallsScreens.kt` (~934), `StatusViewerScreen.kt` (~605), `AivoraApi.kt` (~743)
- **Evidence:** LOC inventory; Graphify hubs; **zero** ViewModel usages; `lifecycle-viewmodel-compose` on classpath unused. Mitigations already present: Home unread projection, `ChatComposer` draft isolation (PERF F7).
- **Confirmed vs potential:** **Confirmed** size + full-list collect at screen top; recomposition cost **needs** Layout Inspector counts.
- **Impact:** Message emits can recompose a huge chat parent; slower compile; higher regression risk.
- **Fix:** Incremental extract (list / composer / bubble) **without UI change**; hoist list state into a smaller subtree or ViewModel only where ownership is clear — not a wholesale MVVM rewrite.
- **Risk:** Behavior drift if state hoisting changes timing of network loads.

#### H2 — HttpLoggingInterceptor always `Level.BASIC`
- **Files:** `data/api/NetworkModule.kt` (~L51–52)
- **Evidence:** Source shows interceptor always added; **not** gated on `BuildConfig.DEBUG`.
- **Confirmed:** Yes for presence in shared client used by all builds that use that module.
- **Impact:** Main-thread-adjacent logging overhead, I/O, battery; noisy logcat on devices.
- **Fix:** `if (BuildConfig.DEBUG) Level.BASIC else Level.NONE` (or BODY only in debug).
- **Risk:** Low if release currently relied on logs for support (unlikely).

#### H3 — Own-status view-count poll every 1.5s
- **Files:** `ui/feature/status/StatusViewerScreen.kt` (~L195–209)
- **Evidence:** `while (true) { refreshViewCount(...); delay(1_500) }` while viewing **own** status; also re-fetches viewers list when sheet open.
- **Confirmed:** Code path exists.
- **Impact:** Radio wakeups, battery, Render free-tier load; can feel “busy” on low-end.
- **Fix:** Prefer WS `status.viewed` only; poll at ≥10–15s or on sheet open / resume; cancel when paused/background.
- **Risk:** Slightly slower view-count UI if WS drops (mitigate with resume refresh).

#### H4 — Status Updates tab refreshes on every `RESUMED`
- **Files:** `ui/feature/home/HomeScreens.kt` `StatusTab` (~L788–791)
- **Evidence:** `repeatOnLifecycle(RESUMED) { StatusRepository.refresh(...) }`
- **Confirmed:** Code path.
- **Impact:** Full status feed pull whenever activity resumes (even returning from unrelated screens if StatusTab stays composed under pager).
- **Fix:** Refresh on tab selected / pull-to-refresh / WS events; debounce resume.
- **Risk:** Stale rings until next event — acceptable with WS `status.new` / `status.deleted`.

#### H5 — Unconstrained Coil on status / media grids
- **Files:** `StatusViewerScreen.kt` (~L242–247); `NewStatusScreens.kt` (~L110–115); `ChatMediaScreen.kt` (~L177–182)
- **Evidence:** Raw URI + `fillMaxSize` / grid cells **without** `ImageRequest` size caps. Contrast: chat bubbles use `BubbleImageModel` decode caps (**good**).
- **Confirmed:** Missing size constraints in those call sites.
- **Impact:** Peak memory / GC jank on image statuses and media galleries (Mate-class).
- **Fix:** Apply same Coil size/decode pattern as bubbles; reuse shared helper.
- **Risk:** Slight quality change if size too aggressive — tune to viewport.

#### H6 — `StatusCache.save` on WebSocket reader thread
- **Files:** `data/realtime/RealtimeClient.kt` status.`new` / `viewed` / `deleted` handlers (~L381–414)
- **Evidence:** JSON encode + SharedPreferences write inline on OkHttp reader after frame parse.
- **Confirmed:** Code path (not Main, but blocks WS reader).
- **Impact:** Delayed realtime under bursty status traffic; potential ANR-adjacent stalls if prefs lock contended.
- **Fix:** Debounce status cache writes on `Dispatchers.IO` (mirror chat persist coalesce).
- **Risk:** Short window of process-kill data loss — acceptable with in-memory store as source of truth.

---

### Medium

#### M1 — `material-icons-extended` + ~281 Material icon call sites
- **Files:** `app/build.gradle.kts` L61; widespread `Icons.Default|Filled|Outlined|…`
- **Evidence:** Dependency present; count of icon refs ~**281**; PERF_FOUNDATION F6 notes trim **deferred**.
- **Impact:** APK method/resource size; some icons may not match Figma custom vectors.
- **Fix:** Prefer existing `AivoraIcons` / drawable set from Figma; remove extended artifact when unused icons gone.
- **Risk:** Visual mismatch if replacements wrong — **requires Figma check before swap**.

#### M2 — SharedPreferences JSON caches on hot paths
- **Files:** `ChatListCache.kt`, `StatusCache.kt`, `MessageCache` (if prefs-backed), `SessionStore.kt`
- **Evidence:** Prefs read/write in cache objects; Status delete/save calls `StatusCache.save` after mutations.
- **Confirmed:** Prefs usage; **potential** main-thread JSON encode cost (needs tracing whether always on `Dispatchers.IO`).
- **Impact:** Jank if large payloads encoded on Main.
- **Fix:** Ensure all encode/decode on `Dispatchers.IO`; consider DataStore or size caps (already partially done for messages).
- **Risk:** Async save races — keep existing coalesce patterns (`InMemoryChatStore` already debounces persist).

#### M3 — Navigation surface area (~84 routes)
- **Files:** `ui/navigation/Routes.kt`, `AivoraNavHost.kt` (~522 lines)
- **Evidence:** Route/composable counts.
- **Impact:** Startup class loading, cognitive load; many settings screens may be rarely used.
- **Fix:** Lazy destinations / feature modules later; not first priority for frame time.
- **Risk:** Over-modularization without size win.

#### M4 — Retrofit `Map<String, Any?>` bodies remain elsewhere
- **Files:** Many `AivoraApi` endpoints still use `Map<String, Any?>`
- **Evidence:** Historical silent failure on status view mark (`emptyMap` + kotlinx) — fixed for status with `EmptyRequestBody` / OkHttp; **other** endpoints may still fail the same way.
- **Impact:** Correctness first; wasted retries / user-visible “could not …” toasts.
- **Fix:** Typed `@Serializable` bodies / `EmptyRequestBody` sweep.
- **Risk:** Need endpoint-by-endpoint verification.

#### M5 — LiveKit / call stack weight
- **Files:** `data/call/LiveKitCallController.kt`, `ui/feature/calls/InCallScreens.kt`, dependency `io.livekit:livekit-android:2.29.1`
- **Evidence:** Dependency + non-trivial controller; Figma has rich “Video call · Mobile” / “Audio call · Mobile” frames.
- **Impact:** Native libs dominate APK size; CPU/thermal during calls (expected).
- **Fix:** ABI filters; ensure disconnect/cleanup paths (code review after approval); don’t load LiveKit until first call if feasible.
- **Risk:** Call regressions.

#### M6 — Home pager keeps all tabs composed
- **Files:** `HomeScreens.kt` HorizontalPager
- **Evidence:** `beyondViewportPageCount = 0` already set (mitigation); StatusTab resume refresh implies tab content can still run collectors when activity resumes.
- **Potential:** Offscreen tabs still collect flows / refresh.
- **Fix:** Gate collectors on `pagerState.currentPage == Updates`.
- **Measurement needed:** Recomposition counts when switching tabs.

#### M7 — Unstable / missing Lazy list keys (non-chat)
- **Files / evidence:**  
  - `ScheduledCallsScreens.kt` ~L209 — `key = { it.id ?: it.hashCode().toString() }`  
  - `PeoplePicker.kt` / `NewCallScreens.kt` / `SettingsScreens.kt` — `key = { it.id.orEmpty() }` (blank-id collisions)  
  - Missing keys: `MessageInfoScreens.kt` receipts; `ChannelScreens.kt` / `ChannelsScreens.kt`; storage list in Settings  
- **Chat/home lists:** keys present (`ChatScreens` message keys; Home chat `key = { it.id }`) — **good**.
- **Impact:** Incorrect item reuse, scroll jumps, rare Lazy crash.
- **Fix:** Stable non-blank keys (`id` required or composite unique).
- **Risk:** Low if data always has ids.

#### M8 — Fixed 1s WebSocket reconnect + separate OkHttp client
- **Files:** `RealtimeClient.kt` (~L82–85, L208–216)
- **Evidence:** Fixed delay, no backoff/jitter; WS client not sharing `NetworkModule` pool.
- **Impact:** Reconnect storms on flaky mobile networks; duplicate connection pools.
- **Fix:** Exponential backoff + jitter; optionally share dispatcher/pool carefully (ping/readTimeout differ).
- **Risk:** Slower reconnect UX if backoff too aggressive.

#### M9 — DataStore dependency unused
- **Files:** `app/build.gradle.kts` (`datastore-preferences`); no Kotlin usages
- **Evidence:** Dependency present, zero references.
- **Impact:** Minor APK / dex weight.
- **Fix:** Remove unused dep **or** migrate SessionStore (separate decision).
- **Risk:** None if removed; migration is larger scope.

---

### Low

#### L1 — Stale Graphify graph
- **Evidence:** graph dated Oct 21; references removed files.
- **Fix:** Optional `graphify --update` after Phase 2 starts (approval).
- **Impact:** Audit tooling accuracy only.

#### L2 — `gradle.properties` lacks parallel/config cache
- **Evidence:** Only JVM args + AndroidX flags.
- **Impact:** Developer build speed, not runtime.
- **Fix:** Optional `org.gradle.parallel` / configuration-cache (dev machine only).

#### L3 — Duplicate empty/error/loading patterns across settings
- **Files:** `SettingsScreens.kt`, `HelpInviteScreens.kt`
- **Potential:** Maintainability; consolidate only when visually identical to Figma.

#### L4 — Compose compiler metrics not enabled
- **Evidence:** No `composeCompiler` reports in repo.
- **Fix:** Enable stability/metrics reports in a **debug** build type for Phase 2 measurement (config change — needs approval).

---

## Already mitigated (do not re-litigate without new evidence)

From [PERF_FOUNDATION.md](docs/PERF_FOUNDATION.md): disk-first chat list/messages; 40-msg window; shared OkHttp; polls pause when backgrounded (home/chat); Coil low-RAM caps + no crossfade; nav transitions reduced; composer-owned draft text; LazyColumn keys + `distinctBy` for chat crash; reverseLayout chat open.

---

## Design inconsistencies requiring Figma review (no auto-fix)

Figma canvas **“Design”** contains **282** top-level mobile frames (sample: Chats/Calls/Account/Appearance/Chat backup/Video call · Mobile/…). Spot-check needed — **not** pixel-audited this phase:

| Area | Suspected mismatch | Why review first |
|------|--------------------|------------------|
| Icons | App uses Material Icons Extended heavily; Figma uses named instances (e.g. Microphone, Camera, End call) | Visual + APK |
| In-call UI | Figma “Video call · Mobile” / “Audio call · Mobile” dense chrome | Compare `InCallScreens.kt` |
| Status / Updates | Recent WhatsApp-like text status / viewers sheet | Confirm against Bolta Updates frames if present |
| Settings density | Huge `SettingsScreens` surface vs Figma Account/Appearance/Backup sets | Parity vs stub screens |
| Bottom nav | App custom `AivoraBottomNav` | Compare to Figma shell frames |
| Chat bubbles | `RichMessageBubbles.kt` | Media/voice vs Figma chat frames |

**Rule:** Document diffs; **ask approval** before any visual change.

---

## Prioritized implementation roadmap (Phase 2 — after approval)

| Order | Item | Effort | Expected win |
|------:|------|--------|--------------|
| 1 | Gate HTTP logging (H2) | XS | CPU/log spam |
| 2 | Soften status poll + StatusTab resume refresh (H3, H4) | S | Battery/network |
| 3 | Move cold-start hydrate off Main (C2) | S | TTI / first-frame |
| 4 | Coil size caps for status + media grid (H5) | S | Memory / scroll |
| 5 | Debounce `StatusCache.save` off WS reader (H6) | S | Realtime stability |
| 6 | Release + debug APK analysis; icon trim plan (C1, M1, M9) | M | Size |
| 7 | Compose metrics + tab/chat recomposition confirm (H1, M6) | S | Evidence for next cuts |
| 8 | Extract chat list/composer boundaries (no visual change) (H1) | M | Maintainability / recompose |
| 9 | Lazy key hygiene (M7); typed empty bodies (M4) | S | Correctness |
| 10 | WS reconnect backoff (M8); LiveKit ABI (M5) | M | Network / size |
| 11 | Figma icon swap pass (approved screens only) | L | Size + parity |

---

## Testing & benchmarking plan

1. **Build:** `assembleDebug` / `assembleRelease` (size).  
2. **Size:** `apkanalyzer` dex/res/lib breakdown before/after.  
3. **Jank:** `scripts/perf-nav-jank.sh` on Mate (low-RAM) + Vivo; keep app foregrounded; record Total / Janky / 90th–99th.  
4. **Chat scroll:** 200+ messages, gfxinfo while flinging.  
5. **Status:** view from peer → owner count; confirm no 1.5s poll after fix.  
6. **Calls:** connect/disconnect; no leak (strict mode / profiler).  
7. **Regression:** login, home paint from cache, open chat to latest, send text/media, settings smoke.  
8. **Compose:** Layout Inspector recomposition counts on Home + Chat (before/after extractions).  
9. **Honest gaps:** If device frozen / emulator-only, state that in the Phase 2 report.

---

## Definition of done (Phase 2)

- Approved findings fixed or explicitly deferred.  
- No unapproved visual/Figma deviations.  
- Tests/benchmarks that ran are listed with numbers; failures documented.  
- Diff limited to approved scope; Graphify updated only if requested.

---

## Ask before Phase 2

Please approve:

1. **Scope for first implementation slice** (recommended: **H2 + H3 + H4 + C2 + H5**, then size analysis).  
2. Whether to **refresh Graphify** (`--update`) before coding.  
3. Whether **Compose compiler metrics** may be enabled in debug Gradle (measurement only).  
4. Any **Figma screens** that are out of scope for icon/visual work.

---

## Phase 2 progress

### Slice 1 (2026-10-27) — **Done**

| ID | Change | Files |
|----|--------|-------|
| H2 | OkHttp `BASIC` logging only when `BuildConfig.DEBUG` | `NetworkModule.kt` |
| H3 | Own-status view poll **1.5s → 15s**; viewers sheet refresh **8s** while open | `StatusViewerScreen.kt` |
| H4 | Status feed refresh on **Updates tab selected**, not every Activity `RESUMED` | `HomeScreens.kt` `StatusTab` |
| C2 | Chat list + recent message disk hydrate on `Dispatchers.IO` | `HomeScreens.kt`, `ChatRepository.kt` |
| H5 | Coil decode size caps for status viewer/compose + media grid | `BubbleImageModel.kt`, `StatusViewerScreen.kt`, `NewStatusScreens.kt`, `ChatMediaSgcreen.kt` |

**Validation:** run `:app:assembleDebug` after slice (see agent session log).

### Slice 2 (2026-10-27) — **Done**

| ID | Change | Files |
|----|--------|-------|
| H6 | Debounced `StatusCache.save` on `Dispatchers.IO` (350ms coalesce); WS + repo writes via `InMemoryStatusStore.persist` | `InMemoryStatusStore.kt`, `RealtimeClient.kt`, `StatusRepository.kt` |

**Validation:** run `:app:assembleDebug` after slice.

### Slice 3 (2026-10-27) — **Done** (C1 analysis + ABI cut, M9, M1 plan)

**Measured (unzip + du; no `apkanalyzer` on SDK):**

| Build | Before | After ABI filter |
|-------|--------|------------------|
| Debug APK | **75MB** | **44MB** |
| Release unsigned APK | **55MB** | **25MB** |

| Release contents (after) | Uncompressed |
|--------------------------|---------------|
| `lib/` (arm64 + v7a only) | ~21MB |
| `classes.dex` (R8) | ~7.6MB |
| `res/` | ~672KB |

**Root cause:** LiveKit/WebRTC `.so` packed for **7 ABIs** (incl. x86/x86_64/mips) ≈ **51MB** uncompressed libs — dwarfed dex/icons. R8 already shrinks release dex; `material-icons-extended` AAR is ~34MB on disk but tree-shaken in release (`res` tiny).

| ID | Change | Files |
|----|--------|-------|
| C1 | `ndk.abiFilters` = `armeabi-v7a`, `arm64-v8a` (device ABIs; Mate/Vivo) | `app/build.gradle.kts` |
| M9 | Removed unused `datastore-preferences` | `app/build.gradle.kts` |
| M1 | **Plan only** (no visual swap) — see below | — |

**M1 icon trim plan (needs Figma approval before code):**
1. **~88** unique `Icons.*` symbols; heaviest files: `SettingsScreens` (~106), `ChatScreens`, `HomeScreens`.
2. Prefer existing `AivoraIcons` drawables for chrome already covered (nav, search, mic, ticks, …).
3. Per-screen Figma pass (Settings → Chat → Calls): replace Material with Figma vectors; do **not** mass-swap without review.
4. After Settings + shell are on `AivoraIcons` / local drawables, try dropping `material-icons-extended` (keep `material-icons-core` or none) and fix remaining compile errors with drawables.
5. Expected size win of icon drop is **modest on release** (already R8-shrunk); main win is **parity + debug dex**. Further size → Play App Bundle / per-ABI splits; arm64-only would cut ~8MB more v7a but drops 32-bit phones.

**Note:** x86 emulator installs need a local Gradle override or temporary filter removal.

**Validation:** `assembleRelease` + `assembleDebug`; debug installed on Vivo.

### Slice 4 (2026-10-27) — **Done** (Compose metrics + M6; H1 evidence)

| ID | Change | Files |
|----|--------|-------|
| L4 / metrics | Opt-in Compose compiler reports: `-PcomposeCompilerReports=true` → `app/build/compose_compiler/` | `app/build.gradle.kts` |
| M6 | Home tabs collect/fetch only when that pager page is selected (`ChatsTab` / `StatusTab` / `CallsTab`) | `HomeScreens.kt` |

**Compiler metrics (clean `compileDebugKotlin` + reports):**

| Metric | Value |
|--------|------:|
| Total composables | 934 |
| Skippable | 526 (~56%) |
| Known unstable args | 192 / 13220 |
| Strong skipping | **on** |

Home/chat surfaces checked (`HomeScreen`, `ChatsTab`, `StatusTab`, `CallsTab`, `ChatIdScreen`, `ChatHeader`, `ChatComposer`) are **restartable + skippable**. Notable unstable params: `ChatComposer.voiceRecorder`, `ChatHeader.menuItems` / message refs. Non-skippable CSV rows are mostly `remember*` helpers (expected).

**H1 extract targets for slice 5 (no UI change):** *(done below)*

**Validation:** reports generated; `assembleDebug` + Vivo install.

### Slice 5 (2026-10-27) — **Done** (H1 message-list boundary)

| ID | Change | Files |
|----|--------|-------|
| H1 | New `ChatMessageList` owns `messages.collectAsState()`, mark-read, older-page load, stick-to-bottom, and LazyColumn; `ChatIdScreen` keeps header/composer/sheets | `ChatScreens.kt` |

No visual change. Message WS upserts recompose the list subtree only (not header/menus/composer).

**Validation:** `assembleDebug` + device install.

### Slice 6 (2026-10-27) — **Done** (M7 Lazy keys + M4 delivered body)

| ID | Change | Files |
|----|--------|-------|
| M7 | Stable non-blank Lazy keys; drop blank-id rows from people/blocked lists | `ScheduledCallsScreens`, `PeoplePicker`, `NewCallScreens`, `SettingsScreens` (storage + blocked), `MessageInfoScreens`, `ChannelScreens`, `ChannelsScreens` |
| M4 | Live `mapOf` → typed `DeliveredBody` for `postMessagesDelivered` | `AivoraApi.kt`, `RealtimeClient.kt` |

**M4 note:** Remaining unused `Map<String, Any?>` API stubs left as-is (no call sites with `emptyMap`/`mapOf` bodies). Convert when those endpoints are wired.

**Validation:** `assembleDebug` + device install.

### Slice 7 (2026-10-27) — **Done** (M8 WS reconnect)

| ID | Change | Files |
|----|--------|-------|
| M8 | Exponential reconnect backoff + jitter (1s→30s); reset on `onOpen` / `stop`; WS OkHttp shares `NetworkModule` dispatcher + connection pool | `RealtimeClient.kt` |

**M5 note:** LiveKit ABI filters already shipped in Slice 3 (`armeabi-v7a` + `arm64-v8a`).

**Validation:** `assembleDebug` + device install.

### Slice 8 (2026-10-27) — **Done** (M1 / #11 Figma icon pass 1)

| ID | Change | Files |
|----|--------|-------|
| M1 | Swap Settings / Help / Home FAB / scaffold Back to existing `AivoraIcons` drawables (`iconRes`) where Figma chrome already shipped locally | `SettingsScreens.kt`, `HelpInviteScreens.kt`, `HomeScreens.kt`, `Components.kt` (`AivoraScaffold` back) |

**Figma check:** Bolta file `EWBSJebF5KWRv6BIGzq1VO` — icon instances (Call, Camera, Microphone, Attach, Base_Icon, …) already covered by `res/drawable/ic_aivora_*`. No new vectors invented.

**Deferred (need more Figma assets or API support):** remaining Material icons (Person, Delete, Block, Devices, menus, call sheets, etc.); dropping `material-icons-extended` until those are replaced. Pass 2 can extend `AivoraSheetAction` / chat overflow.

### Slice 8b (2026-10-27) — **Done** (Figma-locked icon chrome, library-friendly glyphs)

| ID | Change | Files |
|----|--------|-------|
| M1+ | `AivoraIconSlot` + `AivoraIconChrome` — plate/tint/size/radius from Figma; glyph = Material **or** `AivoraIcons` drawable | `AivoraIconSlot.kt`, `SettingsRow`, `AivoraSheets`, `SettingsHeroIntro` |

**Figma chrome (measured):** Settings Action icon = **24×24**, **no plate**, glyph **#141414**. Attach grid keeps SoftMint **56 / r16 / Primary**. Sheet list keeps soft circle **36**.

**Rule going forward:** new screens use `AivoraIconSlot(chrome=…)` — style stays Figma-consistent; swap glyph library without redesigning containers.

### Slice 8c (2026-10-27) — **Done** (AppIcons Outlined catalog)

| ID | Change | Files |
|----|--------|-------|
| M1 | `AppIcons` — all glyphs Material **Outlined** (Figma Fill=No); replaced ~230 `Icons.Default/Filled` call sites | `AppIcons.kt` + 31 feature files |

**Dependency:** keep `material-icons-extended` (Outlined set lives there; R8 tree-shakes unused). Brand chrome still prefers `AivoraIcons` drawables + `AivoraIconSlot` chrome.

**Validation:** `assembleDebug` + device install.

### Slice 9 (2026-10-27) — **Done** (chat/call flash-open + list jank)

| ID | Change | Files |
|----|--------|-------|
| H7 | Skip Main-thread disk hydrate when memory already warm | `ChatRepository` |
| H8 | `applyRemotePage` merge (keep older local rows; no wipe on reopen) | `InMemoryChatStore`, `ChatRepository.pullMessages` |
| H9 | Memory window **40 → 80** (match disk); MessageCache **v3** persists poll/contact/location/event | `InMemoryChatStore`, `MessageCache` |
| H10 | Warm chat open: defer catch-up 600ms; defer meta HTTP 800ms when home row known | `ChatScreens` |
| H11 | Calls tab: `InMemoryCallHistoryStore` — paint last list; Loading only when empty | `InMemoryCallHistoryStore`, `HomeScreens.CallsTab` |
| H12 | List: mark-read on newest-id; `rememberUpdatedState` for press handlers; drop Photo text underlay | `ChatScreens` |

**Goal:** reopen conversation / Calls tab like WhatsApp — cached paint first frame, silent background merge, no Loading wipe.

**Validation:** `assembleDebug` + device install.

### Slice 10 (2026-10-27) — **Done** (cache-first across modules)

| ID | Change | Files |
|----|--------|-------|
| Rule | Always-apply `.cursor/rules/cache-first-perf.mdc` — paint memory first; Loading only if empty; merge don’t wipe | `.cursor/rules/` |
| H13 | Status: skip warm disk; `applyRemoteFeed` (keep viewed); defer HTTP when warm | `StatusRepository`, `InMemoryStatusStore`, `StatusTab` |
| H14 | People directory cache + debounce (New chat/call/group/privacy) | `InMemoryPeopleStore`, `PeoplePicker` |
| H15 | Scheduled calls / Media gallery / Starred / Blocked / Your Details — process stores; Loading iff empty | `InMemoryListStores`, screens |

**Deferred (lower traffic):** Group info members cache, Message info receipts, privacy DTO snap, status viewers sheet.

### Slice 11 (2026-10-27) — **Done** (cold start / launch time)

| ID | Change | Files |
|----|--------|-------|
| L1 | Remove `FirebaseInitProvider`; manual `FirebaseApp.initializeApp` in `PushRegistrar` | Manifest, `PushRegistrar` |
| L2 | Remove unused `EmojiCompatInitializer` from App Startup | Manifest |
| L3 | Gate WS until after first frame (`PresenceLifecycle.allowConnect`) | `PresenceLifecycle`, `MainActivity`, Home |
| L4 | `StartupWarm` — parallel chat-list disk hydrate from `Application.onCreate` | `StartupWarm`, `AivoraApp` |
| L5 | SplashScreen API + keep until `StartupWarm.chatListReady` | `MainActivity`, `themes.xml` |

**Deferred:** Baseline Profile / Macrobenchmark; NavHost route split.

### Slice 12 (2026-10-28) — **Done** (deep leftover perf)

| ID | Change | Files |
|----|--------|-------|
| H16 | Chat disk hydrate **async only** (no Main JSON in composition) | `ChatRepository`, `ChatScreens` |
| H17 | Warm+WS open: skip message pull; mark-read single path (no refreshMessages POST) | `ChatScreens`, `ChatRepository` |
| H18 | `key(msg.id)` around MessageRow for skip on WS emits | `ChatScreens` |
| H19 | Group info / Message info / Privacy caches — no spinner/radio wipe | `InMemoryListStores`, screens |
| H20 | Status viewers: skip 15s poll when WS up; sheet refresh 20s | `StatusViewerScreen` |

**Still deferred (L):** Baseline Profile; NavHost nested split.

**Not shipped yet:** unused Map API stubs; Wave 19+ product.
