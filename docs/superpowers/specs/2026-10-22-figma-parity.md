# Aivora ↔ Figma Visual Parity Spec

**Figma:** [Bolta Punjab (Copy)](https://www.figma.com/design/EWBSJebF5KWRv6BIGzq1VO/Bolta-Punjab--Copy-?node-id=0-1)  
**File key:** `EWBSJebF5KWRv6BIGzq1VO`  
**Page:** Design (`0:1`) — ~300 mobile frames  
**App:** Compose Android `com.aivora.app` (local mock data; backend deferred)

## Goal

Ship a frontend that is **visually indistinguishable** from the Figma mobile frames for every in-scope screen: layout, typography, color, icons, spacing, elevation, and motion — while keeping **clear module structure** and **top-tier scroll/nav performance**.

**Branding:** Product name in the app is **Aivora** only (existing package/strings). Figma file may still be titled “Bolta Punjab”; replace that name in all shipped UI copy while matching the design’s structure and visuals.

## Non-goals (this phase)

- Real backend / Firebase / WebSocket wiring  
- Pixel-perfect status-bar/system chrome cloning (use edge-to-edge + insets)  
- Redesigning product UX beyond what Figma shows  
- Keeping “Bolta Punjab” anywhere in the app UI  

## Source of truth

| Layer | Source |
|-------|--------|
| Visual | Figma Design page frames (named `· Mobile` / feature flows) |
| Routes / feature map | Existing `Routes` + `docs/SCREENS.md` + graphify communities |
| Data | Local stores / seed only until frontend signed off |

## Design system requirements

1. **Tokens** — Extract primitive → semantic colors, type scale, spacing, radii from Figma; map into `AivoraColors` / `Theme.kt` / typography.  
2. **Icons** — Export Figma vector instances used in chrome (nav, overflow, call controls, settings rows) into `res/drawable` or Compose vector; no Material defaults where Figma has a custom glyph.  
3. **Components** — Prefer shared primitives (`AivoraScaffold`, `SettingsList`, overflow menu, FAB, search bar, chips, dialogs) over one-off screen markup.  
4. **Motion** — Chat/settings: slide from right / out to right; home tabs: smooth horizontal pager; dialogs: fade+scale; list content: no jank (no competing springs, settled chrome).  

## Performance requirements

- Home tab pager: indicator tracks `page + offset`; chrome uses settled page only  
- No shared-element fighting slide transitions  
- Lazy lists: stable keys, no heavy work in composition during swipe  
- Vector icons over large bitmaps where possible  
- After each wave: `graphify --update` on `aivora/` so architecture stays queryable  

## Acceptance (per screen)

- Side-by-side Figma screenshot vs emulator matches structure and spacing  
- All visible static icons present locally (no Figma CDN URLs)  
- Interactions work with local mocks (skip only true backend-only actions)  

## Wave scope (ordered)

1. Foundation — tokens, icon pack, motion kit, component chrome  
2. Home shells — Chats, Updates, Calls, My Profile  
3. Conversation — 1:1 + group + composer/menus  
4. Calls deep — New call, keypad, schedule, in-call UI  
5. Profile & edit photo  
6. Settings hubs (Account, Privacy, Chats, Appearance, Notifications, Storage)  
7. Privacy / Account / Backup / Transfer deep flows  
8. Attachments (camera, gallery, location, poll, event)  
9. Help / Invite / remaining edge screens  
10. Final audit + graphify refresh + sign-off  
