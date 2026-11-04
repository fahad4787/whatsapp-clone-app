# Wave 23 — Perf & APK size

Date: 2026-10-28  
Status: **Done**

## Goal

Tighten install footprint; refresh stale “~75MB” docs. Keep PERF F8 (debug R8 off).

## 23.1 Baseline (dual ABI)

| Artifact | Size |
|----------|------|
| Debug APK | **47.3 MB** |
| Release unsigned | **26.5 MB** |
| Bolta reference | ~30 MB |

### Debug breakdown (before trim)

| Category | MB |
|----------|-----|
| DEX | 74.4 |
| `lib/arm64-v8a` | 14.1 |
| `lib/armeabi-v7a` | 8.2 |
| `res` | 1.2 |

Top native: `liblkjingle_peerconnection_so.so` 12.3 MB (arm64) + 6.8 MB (v7a).

### Release breakdown (before trim)

| Category | MB |
|----------|-----|
| `lib/arm64-v8a` | 14.1 |
| `lib/armeabi-v7a` | 8.2 |
| DEX (R8) | 8.2 |
| `res` | 0.9 |

## 23.2 Changes shipped

1. **`abiFilters = arm64-v8a` only** in [`app/build.gradle.kts`](../app/build.gradle.kts) — drops ~8 MB WebRTC `armeabi-v7a` from every sideload APK. Mate/Vivo are arm64. Emulator x86 needs a local abiFilters override (comment in gradle).
2. **`material-icons-extended` kept** — `material-icons-core` only ships ~50 Filled glyphs (no Mic / Videocam / CallEnd / …). AppIcons needs those. Release R8 already strips unused icon classes; further debug DEX cuts would require vendoring vectors (parked).

## After trim

| Artifact | Before | After | Delta |
|----------|--------|-------|-------|
| Debug | 47.3 MB | **38.6 MB** | **−8.7 MB** |
| Release | 26.5 MB | **18.3 MB** | **−8.2 MB** |

Release is **under** Bolta ~30 MB. Remaining weight is mostly LiveKit/WebRTC arm64 (~14 MB lib/).

## 23.3 LiveKit dynamic feature — deferred

A `:calls` on-demand module could remove ~14 MB native from the base install for users who never call, but:

- `LiveKitCallController` / `InCallScreens` / `AivoraNavHost` / `RealtimeClient` couple LiveKit into the base app (needs a facade + deferred nav).
- Play Feature Delivery + first-call install latency; Mate/Vivo sideload cannot use DF easily.

**Decision:** keep LiveKit in the base APK for Wave 16–19 call parity. Revisit for Play store packaging later.

## Play note

Prefer **AAB** for store (per-ABI delivery). Sideload APKs for device testing stay arm64-only.

## 23.4 F8

Debug `isMinifyEnabled = false` unchanged. Release minify + `shrinkResources` remain on.

## Verify

- [x] Vivo (V2440) install arm64 debug APK — Success  
- [ ] Mate — offline at install time; reconnect and `adb install -r` same APK  
- [ ] Smoke: open chat + place/receive call _(manual on device)_
