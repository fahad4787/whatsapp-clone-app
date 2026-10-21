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

## Gap inventory (final check vs ~294 Figma mobile frames)

Plan primaries for Waves 2–9 are covered. Full Design canvas still has many state/dialog frames not built as 1:1 screens.

### High-value missing or thin vs Figma

| Area | Figma | App today | Priority |
|------|-------|-----------|----------|
| Lists hub | `Lists · Overview` `412:7283` | Routes exist; UI not Figma-aligned | High |
| Friend / contact profile | `Friend profile · Mobile` | `Routes.ProfileId` stub | High |
| Media, links and docs | 3 frames | No screen | High |
| Network usage / Proxy | Overview frames | Rows only on Storage hub | Medium |
| Group audio/video call | Group call frames | 1:1 call shells only | Medium |
| Move and scale (crop) | `160:11715` | Folded into photo edit | Medium |
| Chat history / Privacy checkup | Overview frames | Generic settings lists | Medium |
| Status / story viewer polish | My status viewers | Partial `StatusId` / `NewStatus` | Medium |
| Notifications children | Device on/off, reset | Hub only | Low |
| Report contact | Report frames | Missing | Low |
| Multi-step change number / delete / backup | Many children | Intro shells only | Low |
| Login / onboarding | (other pages) | Custom Aivora flow — not in Wave map | N/A |

### Hygiene re-check

- Zero `Bolta` in `app/src`
- Zero Figma CDN URLs in `app/src`
- `assembleDebug` PASS

## Sign-off

Frontend Figma parity plan Tasks 0–12 complete for **core waves (2–6)** + hygiene. Ready for **backend phase** per plan. Remaining gaps above are polish / deep children, not blockers for starting backend.
