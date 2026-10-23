# Backend build plan (feature-by-feature)

Date: 2026-10-22  
Status: **Wave 7 in progress** — FCM push (needs your Firebase project keys). Waves 0–6 done on staging.

## Locked constraints

| Item | Choice |
|------|--------|
| Stack | Fastify + Prisma + Postgres + Redis + object storage |
| Hosting (now) | **Render free / trial** (staging) |
| OTP | **Dev OTP** — code in admin panel + server logs (real SMS later) |
| Calls / LiveKit | **Deferred** |
| Also deferred | Communities, passkeys, backup/transfer, stickers, live location |
| Quality bar | Senior practice: tests + Semgrep CI + verify before next feature |

## One backend → app + admin

```
┌─────────────────┐         ┌──────────────────────────┐
│  aivora (Android)│ ──────► │                          │
└─────────────────┘         │   aivora-backend         │
                            │   /api/*  +  /ws         │
┌─────────────────┐         │   Postgres · Redis · S3  │
│  aivora-admin   │ ──────► │                          │
│  (Next.js)      │         └──────────────────────────┘
└─────────────────┘
```

- **Same server, same database** — admin is not a second backend.
- Mobile uses **user** auth; admin uses **admin** role on the same auth system.
- Every product feature we ship gets API support for the app **and** the admin controls needed to operate it.

### Admin “manage everything” (MVP waves)

| Area | What you can do in admin |
|------|---------------------------|
| Auth / OTP | See latest OTP codes (dev), invalidate sessions |
| Users | Search, view profile, ban / unban, delete |
| Conversations | Inspect chat metadata (not a full spy client in MVP — list/flag) |
| Messages | Soft-delete / hide abusive content (moderation) |
| Media | Flag / remove object references |
| Reports | Queue from app → resolve / action user |
| Push | Optional broadcast later; device tokens visible |
| Metrics | Users, messages/day, signups, active bans |
| Admins | Invite / role (superadmin vs moderator) |

### Admin later (when those app features ship)

| Area | Admin controls |
|------|----------------|
| Status / channels | Moderate posts, hide channel |
| Communities | Moderate / disable |
| Calls | Session / abuse logs (when LiveKit exists) |
| SMS | Provider config when real OTP is enabled |

## Repos

| Repo | Role |
|------|------|
| `aivora/` | Android app (wire to API after each backend slice) |
| `aivora-backend/` | **Shared** API + WebSocket for app and admin |
| `aivora-admin/` | Next.js — operate and moderate the whole product |

## Tools & skills (what we will / won’t use)

### Will use (engineering)

| Tool | Why |
|------|-----|
| **Semgrep** ([semgrep/semgrep](https://github.com/semgrep/semgrep)) | Static analysis in CI — secrets + OWASP-ish JS rules on every PR (`semgrep scan --config auto` / `p/owasp-top-ten` + `p/secrets`) |
| GitHub Actions | test + lint + Semgrep on `aivora-backend` / `aivora-admin` |
| Prisma migrate | schema changes per feature |
| Render | Postgres + Redis + web service for staging |

### Cursor skills we will lean on (project)

| Skill | When |
|-------|------|
| `writing-plans` / `planning-and-task-breakdown` | slice work |
| `incremental-implementation` | one feature at a time |
| `tdd` / `verification-before-completion` | tests before claiming done |
| `api-and-interface-design` | stable `/api/*` matching `AivoraApi` |
| `security-and-hardening` | auth, OTP, uploads |
| `observability-and-instrumentation` | logs for OTP + request IDs |
| `systematic-debugging` | when something fails |

### Not using for this backend

| Thing | Why |
|-------|-----|
| Firebase Auth / Firestore | Stack is custom API (`DECISIONS.md`) |
| Firebase skills (`firebase-firestore`, etc.) | Only FCM later for push — not the data backend |
| Semgrep as a “Cursor skill” | Semgrep is a **CLI/CI tool**, not an agent skill file |

**Nothing from Semgrep has been run or installed yet** — it will be added in Wave 0 CI when repos are created.

---

## Working rules (senior bar)

1. **One feature = one vertical slice** (schema → API → tests → admin if needed → Android wire if needed).  
2. **Definition of Done** before moving on:  
   - Unit/integration tests green  
   - Manual smoke on Render staging  
   - Semgrep CI green (no new ERROR findings)  
   - Short note in PR / docs what was verified  
3. **No drive-by refactors** outside the feature.  
4. **Secrets** only in Render env / GitHub secrets — never committed.  
5. **Fake OTP** until SMS wave: request OTP → read code in admin or logs → verify in app.

---

## Waves & features (test gate after each)

### Wave 0 — Foundation  
**Goal:** empty but professional pipeline on Render.

| # | Feature | Test gate | Status |
|---|---------|-----------|--------|
| 0.1 | Scaffold `aivora-backend` (Fastify, TS, Prisma, env) | `npm test` / health `GET /health` locally | Done |
| 0.2 | Postgres + Redis; migrate; health DB ping | Local + Render `/health` ok | Done — staging `https://whatsapp-clone-backend-hep1.onrender.com/health` → `ok: true` |
| 0.3 | CI: lint, typecheck, tests, **Semgrep** | PR workflow green | Workflows pushed (`whatsapp-clone-backend`, admin frontend) |
| 0.4 | Scaffold `aivora-admin` (Next.js) + deploy | Admin loads on Render | Done — `https://whatsapp-clone-admin-frontend.onrender.com` |

**Exit:** you can open API health + empty admin on Render.

---

### Wave 1 — Auth (dev OTP) + Admin OTP viewer  
| # | Feature | Test gate |
|---|---------|-----------|
| 1.1 | `POST /api/auth/otp/request` — create user stub, store hashed OTP, **log code** | Log shows code; rate-limit smoke |
| 1.2 | `POST /api/auth/otp/verify` — JWT access + refresh | Tokens returned; invalid code rejected |
| 1.3 | Refresh + logout | Refresh rotates; logout invalidates |
| 1.4 | Admin login + **OTP codes list** (phone, code, expiry) | You copy code from admin into app |
| 1.5 | Wire Android login to staging API | End-to-end: request → admin → verify → home |

**Exit:** real login on device against Render (no SMS).

---

### Wave 2 — Profile  
| # | Feature | Test gate |
|---|---------|-----------|
| 2.1 | `GET/PUT /api/me` | Auth required; update name/about |
| 2.2 | Avatar upload path (presign or direct) | Image URL stored; app shows avatar |
| 2.3 | Wire Android profile / your-details | Matches UI flows |

**Exit:** profile persists across reinstall (same account).

---

### Wave 3 — Conversations  
| # | Feature | Test gate |
|---|---------|-----------|
| 3.1 | `GET /api/conversations`, create direct | Two test users see each other |
| 3.2 | Create group + members | Group appears for members |
| 3.3 | Wire Android chat list / new chat / new group | List matches API |

**Exit:** empty chats open correctly on two devices/accounts.

---

### Wave 4 — Messaging (text)  
| # | Feature | Test gate |
|---|---------|-----------|
| 4.1 | `POST/GET .../messages` (text) | Persist + pagination |
| 4.2 | Delivered / read receipts (HTTP) | Status updates in DB |
| 4.3 | Wire Android composer + bubble list | Two users exchange text |

**Exit:** reliable text chat on staging.

---

### Wave 5 — Realtime WebSocket  
| # | Feature | Test gate |
|---|---------|-----------|
| 5.1 | WS auth + join conversation rooms | Connect with JWT |
| 5.2 | Typing + presence | Visible on second client |
| 5.3 | Push new message events + receipts over WS | No refresh needed for new msgs |
| 5.4 | Wire Android socket client | Matches mock behavior |

**Exit:** live typing + instant message delivery.

---

### Wave 6 — Media  
| # | Feature | Test gate |
|---|---------|-----------|
| 6.1 | `POST /api/media/presign` + upload to storage | File reachable by URL |
| 6.2 | Image message type | Appears in conversation |
| 6.3 | Wire Android gallery/camera send | System picker → message |

**Exit:** photo messages work on staging.

---

### Wave 7 — Push (FCM)  
| # | Feature | Test gate |
|---|---------|-----------|
| 7.1 | Register device push token | Token stored |
| 7.2 | Notify on new message when offline | Notification on device |
| 7.3 | Needs your Firebase/FCM project | Keys in Render env |

**Exit:** kill app → still get message notification.

---

### Wave 8 — Thin admin (moderation)  
| # | Feature | Test gate |
|---|---------|-----------|
| 8.1 | Users list / search / ban | Banned user can’t auth |
| 8.2 | Reports queue (`POST /api/reports` + admin UI) | Report → resolve |
| 8.3 | Basic metrics (users, messages/day) | Dashboard numbers match DB |

**Exit:** you can moderate staging users without SQL.

---

### Later (explicitly out of this plan)
- Real SMS OTP  
- LiveKit calls / scheduled calls  
- Communities, channels depth, passkeys, backup, stickers, live location  
- Status / polls / events — can insert as Wave 9+ after MVP exit if you want  

---

## Suggested cadence

- **1 feature slice per working session** (or per day)  
- Always: implement → automated tests → Render smoke → Semgrep CI → **you** confirm → next  
- Do not start Wave N+1 until Wave N exit criteria are met  

## Kickoff checklist

- [x] Render account created  
- [x] Wave 0 kickoff (“go Wave 0”)  
- [x] GitHub: `fahad4787/whatsapp-clone-backend` + admin frontend pushed  
- [x] Docker Desktop installed; local Postgres/Redis up; migrate applied; `/health` → `ok: true`  
- [x] Render API staging live: `https://whatsapp-clone-backend-hep1.onrender.com` (`/health` ok)  
- [x] Admin staging live: `https://whatsapp-clone-admin-frontend.onrender.com`  
- [x] API `CORS_ORIGIN` includes admin URL — dashboard shows API healthy  
- [ ] (Later) Firebase for FCM when we reach Wave 7  
