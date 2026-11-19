# Waves 17+ — What’s left after Calls

Date: 2026-10-27 (updated 2026-11-05)  
Status: Waves **17–57 done**; park = FIDO2 passkeys, encrypted cloud backup, E2EE, admin TOTP

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

| Live (API) | Still mock |
|------------|------------|
| Users, OTPs, Reports, Conversations, Metrics, Live calls, Call history, Settings, Countries, Admins, Audit, Android, Channels, Stickers, Firewall, Legal, Security | — (admin mock pages cleared through Wave 28) |

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
| 20.5 | Channels/stickers admin **only if** Wave 21 ships those features | → Wave **27** ✅ |

Auto-update pattern: reuse SSE/poll where it matters (already on calls).

---

### Wave 21 — Channels & communities — **DONE**
See [WAVE21_CHANNELS.md](WAVE21_CHANNELS.md). List/create/follow/posts/react/report; communities list/create/detail/join. Admin moderate deferred.

---

### Wave 22 — Account & identity — **DONE**
See [WAVE22_IDENTITY.md](WAVE22_IDENTITY.md). Devices revoke, two-step PIN + login enforce, delete/change number, Twilio SMS behind env + `sms_enabled`. Passkeys remain Wave 24.

---

### Wave 23 — Perf & APK size — **DONE**
See [WAVE23_APK_SIZE.md](WAVE23_APK_SIZE.md). Release **~18MB** arm64-only; LiveKit DF deferred; F8 kept.

---

### Wave 24 — Chat actions & settings chrome — **DONE**
See [WAVE24_CHROME.md](WAVE24_CHROME.md). Vote/RSVP, privacy photo/about/groups/calls, font size, clear/delete all, storage numbers.

---

### Wave 25 — Stickers & chat lock — **DONE**
See [WAVE25_STICKERS_LOCK.md](WAVE25_STICKERS_LOCK.md). Emoji sticker packs + send; chat lock PIN + locked chats folder.

---

### Wave 26 — Live location — **DONE**
See [WAVE26_LIVE_LOCATION.md](WAVE26_LIVE_LOCATION.md). Live share 15m/1h/8h, foreground GPS pings, Privacy stop-all.

---

### Wave 27 — Admin channels / stickers / firewall — **DONE**
See [WAVE27_ADMIN.md](WAVE27_ADMIN.md). Closes Wave 20.5; IP allowlist on admin login.

---

### Wave 28 — Admin Legal / Security + Help docs — **DONE**
See [WAVE28_LEGAL_SECURITY.md](WAVE28_LEGAL_SECURITY.md). Last mock admin pages live; Help Terms/Privacy from API.

---

### Wave 29 — Chat backup (portable zip) — **DONE**
See [WAVE29_BACKUP.md](WAVE29_BACKUP.md). `GET /api/me/backup` + share sheet; prefs honest (no fake cloud account).

---

### Wave 30 — Account email & recovery — **DONE**
See [WAVE30_EMAIL.md](WAVE30_EMAIL.md). Verify recovery email; email sign-in; two-step PIN reset via email.

---

### Wave 31 — Companion device link — **DONE**
See [WAVE31_DEVICE_LINK.md](WAVE31_DEVICE_LINK.md). Pairing code: new device starts, phone approves.

---

### Wave 32 — Account info report & security prefs — **DONE**
See [WAVE32_ACCOUNT_INFO.md](WAVE32_ACCOUNT_INFO.md). `GET /api/me/data` JSON report; security-notif toggle persists.

---

### Wave 33 — App lock & permissions — **DONE**
See [WAVE33_APP_LOCK.md](WAVE33_APP_LOCK.md). Biometric app lock; live Permissions status.

---

### Wave 34 — Media auto-download & data prefs — **DONE**
See [WAVE34_AUTO_DOWNLOAD.md](WAVE34_AUTO_DOWNLOAD.md). Storage prefs sync via `/api/me/auto-download`.

---

### Wave 35 — Appearance (theme + default wallpaper) — **DONE**
See [WAVE35_APPEARANCE.md](WAVE35_APPEARANCE.md). System/Light/Dark; default wallpaper; reset.

---

### Wave 36 — Notification prefs — **DONE**
See [WAVE36_NOTIFICATIONS.md](WAVE36_NOTIFICATIONS.md). Persist tones / priority / preview / vibrate; notifier honors prefs.

---

### Wave 37 — Chat prefs & archive all — **DONE**
See [WAVE37_CHAT_PREFS.md](WAVE37_CHAT_PREFS.md). Enter-is-send, keep-archived, archive-all.

---

### Wave 38 — Profile QR — **DONE**
See [WAVE38_QR.md](WAVE38_QR.md). My QR + gallery scan + deep link open DM.

---

### Wave 39 — Links privacy & read receipts — **DONE**
See [WAVE39_PRIVACY.md](WAVE39_PRIVACY.md). Links who-can + reciprocal read receipts.

---

### Wave 40 — Help center & invite — **DONE**
See [WAVE40_HELP_INVITE.md](WAVE40_HELP_INVITE.md). Articles, contact us, app info, SMS invite.

---

### Wave 41 — Default timer & advanced privacy — **DONE**
See [WAVE41_DEFAULTS.md](WAVE41_DEFAULTS.md). Default disappearing; advanced prefs; link-preview off.

---

### Wave 42 — Custom chat lists — **DONE**
See [WAVE42_LISTS.md](WAVE42_LISTS.md). Create/edit/reorder lists; Home filter chips.

---

### Wave 43 — Proxy + privacy checkup — **DONE**
See [WAVE43_PROXY.md](WAVE43_PROXY.md). SOCKS/HTTP proxy; live Privacy checkup statuses.

---

### Wave 44 — Live camera QR — **DONE**
See [WAVE44_QR_CAMERA.md](WAVE44_QR_CAMERA.md). CameraX live scan + gallery fallback.

---

### Wave 45 — Protect IP call relay — **DONE**
See [WAVE45_CALL_RELAY.md](WAVE45_CALL_RELAY.md). Advanced Protect IP → LiveKit ICE RELAY.

---

### Wave 46 — Drag-reorder lists — **DONE**
See [WAVE46_LIST_DRAG.md](WAVE46_LIST_DRAG.md). Long-press drag on Settings → Lists.

---

### Wave 47 — Notification tone — **DONE**
See [WAVE47_NOTIF_TONE.md](WAVE47_NOTIF_TONE.md). System ringtone picker for message notifications.

---

### Wave 48 — Scheduled backup — **DONE**
See [WAVE48_BACKUP_SCHEDULE.md](WAVE48_BACKUP_SCHEDULE.md). WorkManager periodic zip to app storage.

---

### Wave 49 — Block unknown — **DONE**
See [WAVE49_BLOCK_UNKNOWN.md](WAVE49_BLOCK_UNKNOWN.md). Advanced toggle blocks new inbound DMs.

---

### Wave 50 — Open backup — **DONE**
See [WAVE50_OPEN_BACKUP.md](WAVE50_OPEN_BACKUP.md). Browse backup zip; share per-chat transcripts.

---

### Wave 51 — Generate transcripts — **DONE**
See [WAVE51_TRANSCRIPTS.md](WAVE51_TRANSCRIPTS.md). Multi-select chats → text-only zip.

---

### Wave 52 — Silence unknown callers — **DONE**
See [WAVE52_SILENCE_CALLERS.md](WAVE52_SILENCE_CALLERS.md). Privacy Calls toggle skips ring for strangers.

---

## Suggested order

```
17 → 18 → 19 → 20 → 23 APK → 22 Identity → 24 Chrome → 21 Channels → 25 Stickers/lock → 26 Live location
→ 27–52 prefs/chrome → park (FIDO2 passkeys / encrypted cloud backup / E2EE / admin TOTP / SMTP / …)
```
