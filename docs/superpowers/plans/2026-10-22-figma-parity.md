# Figma Visual Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Aivora Compose app visually match the Figma Design page (~300 mobile frames) with clear module structure, exported icons, and top-tier motion/scroll performance — local mocks only; product name stays **Aivora**.

**Architecture:** Keep `ui/feature/*` + `Routes` / `AivoraNavHost`. Introduce a Figma-derived design system (`theme` tokens + `ui/icons` + shared chrome). Implement in waves against named Figma frames via MCP. After each wave, refresh `graphify-out/` so structure stays queryable. Backend deferred until Waves 2–6 pass audit.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, Material3 (token-skinned), Coil, graphify CLI, Figma MCP (`fileKey=EWBSJebF5KWRv6BIGzq1VO`)

**Spec:** `docs/superpowers/specs/2026-10-22-figma-parity.md`

## Global Constraints

- Figma file: [Bolta Punjab (Copy)](https://www.figma.com/design/EWBSJebF5KWRv6BIGzq1VO/Bolta-Punjab--Copy-?node-id=0-1) — key `EWBSJebF5KWRv6BIGzq1VO`, page Design `0:1`
- **Branding:** UI strings, launcher label, overflow titles, empty states, invite copy → **Aivora** only. Never ship “Bolta Punjab” in the app. Match Figma layout/icons/spacing; replace that name wherever it appears in frames.
- Before every `get_design_context`: load `figma-design-to-code`; pass `skillNames=figma-design-to-code`
- `create_session` once per work session; reuse `sessionId`
- No backend / Firebase / realtime wiring in this plan
- Package `com.aivora.app`; commits one wave (or batch) at a time
- After each wave: `cd aivora && graphify . --update` (use `--code-only` if non-code extraction needs a key)
- Performance: home chrome uses `settledPage` only; nav pill tracks `page + offset`; chat open = slide only (no shared-element fight); no Figma CDN URLs left in source

## Review Focus

- Tab swipe jank if Scaffold listens to `currentPage` mid-swipe — settled chrome + offset-driven pill only  
- Chat open jank if shared-element / list animateScroll / slide run together — slide only + deferred list scroll  
- Material icons sneaking back where Figma vectors exist — icon checklist per wave  
- Stub settings lists after hub polish — deep screens must use same card chrome  
- Remote Figma asset URLs committed by mistake — all icons/images local under `res/`  

## File map (target)

| Area | Path | Responsibility |
|------|------|----------------|
| Tokens | `ui/theme/Color.kt`, `Type.kt`, `Theme.kt`, `Dimens.kt` | Figma → semantic tokens |
| Icons | `ui/icons/AivoraIcons.kt` + `res/drawable/ic_*.xml` | Exported vectors |
| Motion | `ui/navigation/NavTransitions.kt`, `ui/motion/Motion.kt` | Slide / fade / snap kit |
| Chrome | `ui/components/*` | Scaffold, menus, chips, dialogs, search, FAB, bottom nav |
| Home | `ui/feature/home/*` (split tabs if `HomeScreens.kt` > ~800 LOC) | 4 tabs + pager |
| Chat | `ui/feature/chat/*` | Conversation + composer |
| Calls | `ui/feature/calls/*` | Tab + schedule + in-call shells |
| Status | `ui/feature/status/*` | Updates / stories |
| Settings | `ui/feature/settings/*` (split hubs if file bloated) | Hubs + deep flows |
| Mapping | `docs/FIGMA_SCREEN_MAP.md` | Frame → route → file → wave → status |
| Graph | `graphify-out/` | Architecture after each wave |

**Graphify communities to respect (do not dump everything into one file):**  
Home tabs · Scaffold & settings · Chat · Theme · Navigation — when a wave grows a file past ~800–1000 LOC, split by feature before the next wave.

---

### Task 0: Figma inventory + screen map

**Files:**
- Create: `docs/FIGMA_SCREEN_MAP.md`

**Interfaces:**
- Produces: table `Frame | nodeId | Route | Feature path | Wave | Status`

- [ ] **Step 1:** Group ~300 Design frames into Waves 2–9 (spec order)
- [ ] **Step 2:** Fill Waves 2–3 completely (ids + routes); list Waves 4–9 by name + id
- [ ] **Step 3:** Commit `docs: map Figma frames to Aivora routes and waves`

---

### Task 1: Design tokens from Figma

**Files:**
- Create: `ui/theme/Dimens.kt`
- Modify: `ui/theme/Color.kt`, `Type.kt`, `Theme.kt`

**Interfaces:**
- Produces: `AivoraColors`, `AivoraDimens`, `AivoraTypography`, `AivoraTheme` ColorScheme

- [ ] **Step 1:** `get_design_context` + screenshot — `Chats · Mobile` (`15:885`), `My Profile · Mobile` (`160:11428`); extract green, surfaces, ink, danger, radii, type
- [ ] **Step 2:** Update token files (semantic names; Aivora brand)
- [ ] **Step 3:** `./gradlew :app:compileDebugKotlin` PASS
- [ ] **Step 4:** Commit `feat(theme): align tokens with Figma Design page`
- [ ] **Step 5:** `graphify . --update`

---

### Task 2: Icon pack export

**Files:**
- Create: `res/drawable/ic_*.xml`, `ui/icons/AivoraIcons.kt`
- Modify: `AivoraBottomNav.kt`, overflow menus, home FABs

**Interfaces:**
- Produces: `AivoraIcons.*` ImageVector accessors
- Consumes: Figma instances from Chats / Calls / Profile frames

- [ ] **Step 1:** Export chrome icons from `15:885`, `55:6764`, `160:11428` into local drawables (no remote URLs)
- [ ] **Step 2:** Wire `AivoraIcons`; replace Material defaults in bottom nav + overflow
- [ ] **Step 3:** Compile PASS; commit `feat(icons): export Figma chrome icons`
- [ ] **Step 4:** `graphify . --update`

---

### Task 3: Motion + performance lock

**Files:**
- Modify: `NavTransitions.kt`, `NavExtensions.kt`, `HomeScreens.kt`, `AivoraBottomNav.kt`, `ChatScreens.kt`

**Interfaces:**
- Produces: slide enter/popExit; pager `selectionProgress`; no shared-element on chat open

- [ ] **Step 1:** Tie motion constants to `Motion.kt` / Dimens; emphasized slide for push routes
- [ ] **Step 2:** Verify settled-only chrome + offset-driven nav pill (no spring fight)
- [ ] **Step 3:** Manual: swipe tabs 20×; open/close chat 10× — smooth
- [ ] **Step 4:** Commit `perf(nav): lock Figma-grade slide and tab swipe`
- [ ] **Step 5:** Grep source for `Bolta` / `bolta` — zero hits in `app/` UI strings

---

### Task 4: Wave — Home shells

**Figma:** Chats `15:885`, Updates `22:7920`, Calls `55:6764`, My Profile `160:11428`

**Files:** `ui/feature/home/*`, `AivoraBottomNav.kt`, `AivoraMenus.kt`  
Split into `ChatsTab.kt` / `UpdatesTab.kt` / `CallsTab.kt` / `ProfileTab.kt` if needed.

- [ ] **Step 1:** Design context + screenshot each primary; gap list
- [ ] **Step 2:** Chats parity (search, chips, rows, pencil FAB, overflow) — title **Aivora**
- [ ] **Step 3:** Updates parity (My status, Gallery/Text, Recent)
- [ ] **Step 4:** Calls parity (actions, filters, list, FAB, Create call link / Clear log dialogs)
- [ ] **Step 5:** My Profile parity (search, QR/edit, avatar, card groups)
- [ ] **Step 6:** Compile + visual check; commit `feat(home): Figma parity for four main tabs`
- [ ] **Step 7:** `graphify . --update`

---

### Task 5: Wave — Conversation

**Figma:** Conversation `33:3379` / `106:9508`, Group `106:9702`

**Files:** `ui/feature/chat/*` (split composer/bubbles if large)

- [ ] **Step 1:** Design context + screenshot 1:1 and group
- [ ] **Step 2:** Header, bubbles, composer, attach entry (local)
- [ ] **Step 3:** Slide-only open; deferred list scroll after enter
- [ ] **Step 4:** Compile; commit `feat(chat): Figma conversation and composer parity`
- [ ] **Step 5:** `graphify . --update`

---

### Task 6: Wave — Calls deep

**Figma:** New call / keypad / Schedule (`213:12906`, `213:12847`, …), Audio `49:2801`, Video `49:2883`

**Files:** `ui/feature/calls/*`, `Routes` + `AivoraNavHost` as needed

- [ ] **Step 1:** Add any missing slide routes
- [ ] **Step 2:** New call + keypad + link dialog
- [ ] **Step 3:** Schedule empty / form / upcoming / past
- [ ] **Step 4:** In-call audio/video shells (mock controls)
- [ ] **Step 5:** Compile; commit `feat(calls): Figma call and schedule flows`
- [ ] **Step 6:** `graphify . --update`

---

### Task 7: Wave — Profile edit

**Figma:** Profile details `160:11871`, Edit photo `160:11747`, Move/scale `160:11715`

**Files:** `ui/feature/profile/*`

- [ ] **Step 1:** Design context for details + edit photo
- [ ] **Step 2:** Details cards + edit/crop shell (local pick); labels say Aivora where needed
- [ ] **Step 3:** Compile; commit `feat(profile): Figma profile details and photo edit`
- [ ] **Step 4:** `graphify . --update`

---

### Task 8: Wave — Settings hubs

**Figma:** Appearance `412:11908`, Notifications `412:13285`, Account `412:8851`, Privacy `412:10040`, Chats settings `412:10147`, Storage `412:14733`

**Files:** `ui/feature/settings/*` (split hubs if bloated)

- [ ] **Step 1:** Screenshot + gaps per hub
- [ ] **Step 2:** Card lists, green toggles/radios, order/copy (Aivora)
- [ ] **Step 3:** Profile tab → hubs wired
- [ ] **Step 4:** Compile; commit `feat(settings): Figma hub screens parity`
- [ ] **Step 5:** `graphify . --update`

---

### Task 9: Wave — Settings deep flows

**Scope:** Privacy/Account/Backup/Transfer/Storage/Lists/Starred children per `FIGMA_SCREEN_MAP.md`

- [ ] **Step 1:** Batches of 5–8 screens: context → UI → compile
- [ ] **Step 2:** Local dialogs/confirmations as in Figma
- [ ] **Step 3:** Commit per batch `feat(settings): Figma parity batch <name>`
- [ ] **Step 4:** `graphify . --update` after each batch

---

### Task 10: Wave — Attachments & create

**Figma:** Camera `22:8320`, Photos `22:8455`, Location `412:6357`, Poll `412:5262`, Event `412:4930`, Status create

**Files:** `camera/*`, `status/*`, `events/*`, chat attach sheets

- [ ] **Step 1:** Camera/gallery/status shells
- [ ] **Step 2:** Poll/event/location shells (local state)
- [ ] **Step 3:** Compile; commit `feat(create): Figma camera gallery status attach flows`
- [ ] **Step 4:** `graphify . --update`

---

### Task 11: Wave — Help / Invite / leftovers

**Figma:** Help `412:15259`, Invite `412:14994`, remaining map rows

- [ ] **Step 1:** Implement or mark deferred in map with reason
- [ ] **Step 2:** Compile; commit `feat(misc): Figma help invite and remaining shells`
- [ ] **Step 3:** Full `graphify .` refresh

---

### Task 12: Final parity audit

**Files:** `docs/FIGMA_SCREEN_MAP.md`, optional `docs/PARITY_AUDIT.md`

- [ ] **Step 1:** Waves 2–6 primaries: Figma vs emulator Pass/Fail
- [ ] **Step 2:** Fix until Waves 2–6 Pass
- [ ] **Step 3:** No Figma CDN URLs; no “Bolta” in app UI strings
- [ ] **Step 4:** `./gradlew :app:assembleDebug` PASS
- [ ] **Step 5:** Commit `docs: Figma parity audit for core waves`
- [ ] **Step 6:** Frontend signed off → backend phase

---

## Execution notes

1. Prefer **subagent-driven-development**: one wave/batch per agent + review before next.  
2. Always pull Figma **screenshot** before editing a screen.  
3. Sparse `get_design_context` → fetch child nodes in parallel.  
4. Split oversized Kotlin files when graphify hubs get crowded.  
5. Do not start backend until Task 12 Waves 2–6 Pass.  

## Recommended approach

**Subagent-driven** — waves have hard interfaces (tokens → icons → home → chat → …); a mistake in tokens pollutes every later screen. Fresh implementer + reviewer per wave is worth the cost for a ~300-frame parity job.
