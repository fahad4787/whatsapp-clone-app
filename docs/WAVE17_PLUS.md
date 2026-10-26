# Waves 17+ — What’s left after Calls

Date: 2026-10-27  
Status: Waves **17–18 done**; 19+ planned

## Done so far (short)

| Track | Waves | Status |
|-------|-------|--------|
| Backend MVP + admin moderation | 0–8 | Done |
| Full chat | 9–15 | Done |
| Perf foundation | F0–F8 | Done |
| LiveKit calls | 16.1–16.4 | Done |
| Export chat | 17 | Done — see [WAVE17_EXPORT.md](WAVE17_EXPORT.md) |
| Status / Updates | 18 | Done — see [WAVE18_STATUS.md](WAVE18_STATUS.md) |

## Gaps found in audit (2026-10-27)

### Product (Android)

| Gap | Evidence | Priority |
|-----|----------|----------|
| **Export chat** | Settings UI exists; actions are no-ops; API stub `GET conversations/{id}/export` | High (Bolta parity) |
| **Updates / Status** | Main tab uses `InMemoryStatusStore` only; client API stubs (`/api/status*`) have **no backend routes** | High (empty product surface) |
| **Call links** | ⋮ “Create call link” + dialog are stubs; APK stubs `POST /api/call-links` | Medium (finish calls) |
| **Settings chrome** | Many screens (font size, storage numbers, two-step, backup, clear-all) are local UI only | Medium |
| **Channels / communities** | Nav + API stubs; no backend | Lower |
| **Stickers / live location / passkeys** | Client stubs; deferred in BACKEND_PLAN | Lower |
| **APK size** | ~75MB vs Bolta ~30MB (LiveKit + native) | Medium (PERF) |
| **Real SMS OTP** | Dev OTP + admin codes only | Later (production identity) |

### Admin

| Live (API) | Still mock (`ResourcePage` / `useMockStore`) |
|------------|-----------------------------------------------|
| Users, OTPs, Reports, Conversations, Metrics, Live calls, Call history | Settings, Countries, Channels, Stickers, Admins, Audit, Android, Firewall, Security, Legal |

### Backend

Shipped routes: auth, me, conversations, messages, media, devices, people, blocks, reports, calls, scheduled-calls, admin moderation + admin calls.  
**Missing for next product waves:** status, export, call-links, channels/communities, stickers, etc.

---

## Recommended next waves

### Wave 17 — Export chat *(Bolta parity)* — **DONE**
See [WAVE17_EXPORT.md](WAVE17_EXPORT.md).

---

### Wave 18 — Status / Updates *(main tab)* — **DONE**
See [WAVE18_STATUS.md](WAVE18_STATUS.md).

---

### Wave 19 — Call links *(finish calls)*
**Goal:** Shareable link joins a LiveKit room without prior chat.

| # | Slice | Exit |
|---|-------|------|
| 19.1 | `POST /api/call-links`, `POST /api/call-links/:code` join | Token + room |
| 19.2 | Replace stub dialog with create → copy/share | Link works |
| 19.3 | Deep link / in-app join screen | Second device joins |
| 19.4 | Admin: show link-created calls in history (reuse 16.4) | Visible |

---

### Wave 20 — Admin ops *(mock → live)*
**Goal:** Remaining admin sidebar pages talk to the API.

| # | Slice | Exit |
|---|-------|------|
| 20.1 | App config / Android version gates (`/api/app/config`) | Android page live |
| 20.2 | Countries allowlist | Signup respects enabled list |
| 20.3 | Settings map (limits, ring timeout, group caps) | Persist + app reads |
| 20.4 | Admins + audit log | Invite / role / trail |
| 20.5 | Channels/stickers admin **only if** Wave 21 ships those features | Skip otherwise |

Auto-update pattern: reuse SSE/poll where it matters (already on calls).

---

### Wave 21 — Channels & communities *(thin v1)*
**Goal:** Broadcast channels + optional communities shell.

| # | Slice | Exit |
|---|-------|------|
| 21.1 | Channel follow + posts feed | Updates-adjacent or Channels screen |
| 21.2 | Community list + linked groups | Join works |
| 21.3 | Admin moderate channel posts | Hide / ban author |

Defer if Status + Export are more urgent.

---

### Wave 22 — Account & identity
**Goal:** Production-ready account surfaces.

| # | Slice | Exit |
|---|-------|------|
| 22.1 | Real SMS OTP provider (Twilio/etc.) + kill switch | Dev OTP still for staging |
| 22.2 | Two-step PIN (API stubs exist) | Enforce on login |
| 22.3 | Linked devices list (me/devices already partial) | Revoke works |
| 22.4 | Passkeys (optional) | Login without OTP |
| 22.5 | Change number / delete account real flows | Not toast-only |

---

### Wave 23 — Perf & APK size
**Goal:** Closer to Bolta install footprint and low-end feel.

| # | Slice | Exit |
|---|-------|------|
| 23.1 | Measure APK breakdown (LiveKit, Coil, icons) | Report MB by module |
| 23.2 | Trim unused resources / icon set; ABI splits | Smaller download |
| 23.3 | Optional: dynamic feature or thinner call module | Tradeoff documented |
| 23.4 | Keep F8 discipline (no debug R8 minify) | Mate/Vivo jank OK |

---

### Wave 24 — Later / park
Stickers store, live location, multi-account, chat backup to cloud, E2EE, full firewall/security admin pages.

---

## Suggested order

```
17 Export chat  →  18 Status/Updates  →  19 Call links
→  20 Admin ops  →  23 APK size (anytime parallel)
→  21 Channels   →  22 Identity/SMS
```

**Fastest user-visible wins:** 17 then 18 (parity + main tab).  
**Finish calls product:** 19.  
**Ops:** 20.  
**Ship store-ready:** 22 + 23.

---

## Kickoff

Say **go Wave 19** (call links), **go Wave 20** (admin ops), or **go Wave 23** (APK size).
