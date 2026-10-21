# Figma parity audit — 2026-10-22

**File:** `EWBSJebF5KWRv6BIGzq1VO` · Brand: **Aivora**  
**Scope:** Plan Task 12 — Waves 2–6 primaries + hygiene gates

## Gates

| Check | Result |
|-------|--------|
| `./gradlew :app:assembleDebug` | **PASS** |
| `bolta` / `Bolta` in `app/src` | **PASS** (zero hits) |
| Figma CDN / remote design asset URLs in `app/src` | **PASS** (zero hits) |
| Local icons (`AivoraIcons` / drawables) | **PASS** |

## Waves 2–6 primaries (code vs Figma)

Audited by implementing against Figma screenshots / design context (not a live emulator pixel diff in this session). Status = structural + copy + token parity for the mapped routes.

| Wave | Frame | Route | Verdict |
|------|-------|-------|---------|
| 2 | Chats · Mobile | `Routes.Home` | **Pass** |
| 2 | Updates · Browse | `Routes.Home` | **Pass** |
| 2 | Calls · Mobile | `Routes.Home` | **Pass** |
| 2 | My Profile · Mobile | `Routes.Home` | **Pass** |
| 3 | Conversation · Mobile | `Routes.ChatId` | **Pass** |
| 3 | Group Conversation · Mobile | `Routes.ChatId` | **Pass** |
| 4 | Schedule empty / new | `Routes.ScheduledCalls` | **Pass** |
| 4 | Keypad · Voice dialer | `Routes.NewCall` | **Pass** |
| 4 | Audio / Video call | `Routes.AudioCall` / `VideoCall` | **Pass** |
| 5 | Profile details / Edit photo | `Routes.YourDetails` | **Pass** |
| 6 | Appearance / Notifications / Account / Privacy / Chats / Storage hubs | settings hubs | **Pass** |

## Waves 7–9

Mapped and implemented as **wip** shells (settings deep batches, create/attach, help/invite). Not required for Task 12 Waves 2–6 sign-off. Follow-up: device QA + remaining Figma children (lists, backup multi-step, help articles).

## Residual notes

- Camera / gallery use local mock previews (no CameraX live feed) — intentional for local-mock phase.
- Some settings child screens still use Material icons where Figma uses custom glyphs; chrome icons for home/nav are local drawables.
- Emulator side-by-side pixel pass recommended before production polish.

## Sign-off

Frontend Figma parity plan Tasks 0–12 complete for **core waves (2–6)** + hygiene. Ready for **backend phase** per plan.
