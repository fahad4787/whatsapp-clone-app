# Full chat plan (Wave 9+)

Date: 2026-10-24  
Status: **Wave 15 done** (Waves 9–15 + PERF F0–F5). Chat track complete. See `WAVE15_CHAT_POLISH.md`.

## Principle

Ship **chat depth first**. Same rules as before: one vertical slice → tests → Render smoke → you confirm → next.

Out of this track (still later): LiveKit calls, communities/channels depth, real SMS, E2EE, stickers pack store, passkeys, backup/transfer.

---

## What’s already solid

Text + images, live WS delivery, typing/presence, unread + FCM, groups create, report/ban.

Biggest gaps for “full chat”: message actions, conversation controls, voice/docs, real receipts UI, group admin, block.

---

## Recommended waves (chat-only)

### Wave 9 — Message truth & receipts
**Goal:** messages behave like a real chat log, not a flat list.

| # | Feature | Exit | Status |
|---|---------|------|--------|
| 9.1 | Persist message `status` to Android (`LocalMessage`) + ticks SENT / DELIVERED / READ | Second device shows ticks correctly | Done |
| 9.2 | WS + HTTP delivered when peer receives; read when chat open | Sender ticks update live | Done |
| 9.3 | Soft-delete: delete for me / delete for everyone | Both devices respect delete | Done |
| 9.4 | Clear chat (server-backed for me) | Reopen chat ≠ local-only wipe | Done |

**Why first:** everything else hangs off correct message identity + status.

---

### Wave 10 — Reply, react, star, forward
**Goal:** core message actions.

| # | Feature | Exit | Status |
|---|---------|------|--------|
| 10.1 | Reply / quote (`reply_to`) in schema + composer + bubble | Reply thread visible both sides | Done |
| 10.2 | Reactions (emoji per user) | Live via WS | Done |
| 10.3 | Star / unstar + Starred messages screen from API | Stars survive reinstall | Done |
| 10.4 | Forward message to another chat | Appears in target conversation | Done |

---

### Wave 11 — Conversation controls
**Goal:** home list + chat settings match WhatsApp basics.

| # | Feature | Exit | Status |
|---|---------|------|--------|
| 11.1 | Archive / unarchive (API + home Archived) | Survives restart | Done |
| 11.2 | Mute / unmute (`muted_until`) + no FCM while muted | Muted chat quiet | Done |
| 11.3 | Pin chat (max N) | Pinned section on home | Done |
| 11.4 | Block / unblock + blocked list; block stops messages | Blocked peer can’t message | Done |
| 11.5 | In-chat search (server `q` or local index v1) | Find old text | Done |

---

### Wave 12 — Rich media in chat
**Goal:** attach sheet is real, not fake text.

| # | Feature | Exit | Status |
|---|---------|------|--------|
| 12.1 | Voice notes (AUDIO type + upload + playback bubble) | Record → send → play on peer | Done |
| 12.2 | Documents (FILE type + open/download) | PDF/doc opens | Done |
| 12.3 | Video messages (VIDEO type, short clip + thumbnail) | Send/play on peer | Done |
| 12.4 | Shared media gallery in chat (images/videos/docs tabs) | Menu → Media works | Done |
| 12.5 | Link preview (unfurl on send or open) | URL shows card | Done |

---

### Wave 13 — Groups as real groups
**Goal:** group chat is operable, not create-only. See `WAVE13_GROUPS.md`.

| # | Feature | Exit | Status |
|---|---------|------|--------|
| 13.1 | Add / remove members | Membership updates live | Done |
| 13.2 | Promote / demote admin | Role enforced on actions | Done |
| 13.3 | Group info edit (title, icon, description) | Visible to members | Done |
| 13.4 | Leave group / dissolve (admin) | Correct list state | Done |
| 13.5 | “Only admins can send / edit info” flags | Non-admins blocked | Done |

---

### Wave 14 — Chat extras (composer pack)
**Goal:** remaining attach / privacy features that are still chat. See `WAVE14_CHAT_EXTRAS.md`.

| # | Feature | Exit | Status |
|---|---------|------|--------|
| 14.1 | Contact card message | Peer can tap to save/start chat | Done |
| 14.2 | Location pin (static map / coords message) | Shows on peer | Done |
| 14.3 | Polls (create, vote, live tallies) | Votes sync over WS | Done |
| 14.4 | Events (create + RSVP in chat) | Event bubble + details | Done |
| 14.5 | Disappearing messages (per-chat timer) | Old msgs vanish after TTL | Done |
| 14.6 | Last-seen + privacy settings wired | Respects privacy | Done |

---

### Wave 15 — Chat polish
**Goal:** production-feel edges. See `WAVE15_CHAT_POLISH.md`.

| # | Feature | Exit | Status |
|---|---------|------|--------|
| 15.1 | Edit message (within window) + “edited” label | Both sides | Done |
| 15.2 | Message info (real readers for groups) | Not mock | Done |
| 15.3 | Per-chat wallpaper / keep device theme | Persists | Done |
| 15.4 | Favourites filter from API | Toggle works | Done |
| 15.5 | View-once image (optional) | Opens once | Done |

---

## After full chat (separate tracks)

| Track | Waves (sketch) |
|-------|----------------|
| **Status** | Server stories, privacy, views |
| **Channels / communities** | Broadcast + follow |
| **Calls** | LiveKit 1:1 then group |
| **Identity** | Real SMS OTP, multi-device, passkeys |

---

## Suggested order if you want “WhatsApp chat ASAP”

```
9 receipts/delete  →  10 reply/react/star/forward  →  11 archive/mute/pin/block/search
→  12 voice/docs/video/gallery  →  13 group admin  →  14 polls/location/disappear
→  15 polish
```

Fastest path to “feels complete for daily texting”: **9 → 10 → 11 → 12.1–12.2 → 13**.  
Video, polls, events, disappearing can slip if you want speed.

---

## Definition of “full functional chat” (exit)

- [ ] Text, image, voice, document work 1:1 and in groups  
- [ ] Reply, react, star, forward, delete for me/everyone  
- [ ] Correct ticks + mute / archive / pin / block  
- [ ] In-chat search + media gallery  
- [ ] Group add/remove/admin/leave  
- [ ] Admin can still ban / see reports  

Polls, events, location, disappearing, edit = nice-to-have inside Wave 14–15.

---

## Kickoff

When you say **go Wave 9**, we start receipts + delete (schema → API → WS → Android ticks/UI).
