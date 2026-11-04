# Waves 17+ — What’s left after Calls

Date: 2026-10-27 (updated 2026-10-28)  
Status: Waves **17–20 + 23 done**; 21 deferred; 22 planned

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
| **APK size** | Was ~75MB; Wave 23 → release **~18MB** arm64 — [WAVE23_APK_SIZE.md](WAVE23_APK_SIZE.md) | Done |
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

### Wave 19 — Call links *(finish calls)* ✅
**Goal:** Shareable link joins a LiveKit room without prior chat.  
**Doc:** [WAVE19_CALL_LINKS.md](WAVE19_CALL_LINKS.md)

| # | Slice | Exit |
|---|-------|------|
| 19.1 | `POST /api/call-links`, `POST /api/call-links/:code` join | Token + room ✅ |
| 19.2 | Replace stub dialog with create → copy/share | Link works ✅ |
| 19.3 | Deep link / in-app join screen | Second device joins ✅ |
| 19.4 | Admin: show link-created calls in history (reuse 16.4) | Visible (existing call list) |

---

### Wave 20 — Admin ops *(mock → live)* ✅
**Goal:** Remaining admin sidebar pages talk to the API.  
**Doc:** [WAVE20_ADMIN_OPS.md](WAVE20_ADMIN_OPS.md)

| # | Slice | Exit |
|---|-------|------|
| 20.1 | App config / Android version gates (`/api/app/config`) | Android page live ✅ |
| 20.2 | Countries allowlist | Signup respects enabled list ✅ |
| 20.3 | Settings map (limits, ring timeout, group caps) | Persist + app reads ✅ |
| 20.4 | Admins + audit log | Invite / role / trail ✅ |
| 20.5 | Channels/stickers admin **only if** Wave 21 ships those features | Skip |

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

### Wave 22 — Account & identity — **DONE**
See [WAVE22_IDENTITY.md](WAVE22_IDENTITY.md). Devices revoke, two-step PIN + login enforce, delete/change number, Twilio SMS behind env + `sms_enabled`. Passkeys remain Wave 24.

---

### Wave 23 — Perf & APK size — **DONE**
See [WAVE23_APK_SIZE.md](WAVE23_APK_SIZE.md). Release **~18MB** arm64-only; LiveKit DF deferred; F8 kept.

---

### Wave 24 — Later / park
Stickers store, live location, multi-account, chat backup to cloud, E2EE, full firewall/security admin pages.

---

## Suggested order

```
17 → 18 → 19 → 20 → 23 APK → 22 Identity (done)
→  21 Channels (deferred)  →  24 park
```

---

## Kickoff

Say **go Wave 19** (call links), **go Wave 20** (admin ops), or **go Wave 23** (APK size).
