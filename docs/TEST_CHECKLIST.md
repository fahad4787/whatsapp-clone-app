# Aivora — unified manual test checklist

One pass after backend deploy includes Waves **16–52** (skip anything already verified this build if unchanged).  
Use staging OTP **`000000`** (or admin OTP log) while SMS is off.

**Order tip:** identity change-number before delete; delete last on a throwaway account.

Docs: [WAVE17_PLUS.md](WAVE17_PLUS.md) · per-wave notes under `docs/WAVE*.md`

---

## 0. Deploy smoke

1. `https://whatsapp-clone-backend-hep1.onrender.com/health` → OK  
2. Optional: `GET /api/app/config` shows `otp_dev_bypass` / `sms_enabled` / version gates  
3. Sign in on phone with test number + `000000`

---

## Wave 16 — Calls

### W. Voice / video
1. 1:1 audio call → ring → answer → talk → end (both sides end cleanly)  
2. 1:1 video call → same  
3. Calls tab shows history; system notice in chat  
4. Optional: group / scheduled call still starts  

---

## Wave 17 — Export chat

### T. Export
1. Chat ⋮ → Export chat → Without media → share sheet opens; `.txt` has messages  
2. Export → Include media → `.zip` shares  
3. Settings → Chats → Export → pick a chat → same flows work  

---

## Wave 18 — Status / Updates

### U. Status
1. Updates tab → post a text status → appears for you  
2. Peer with a DIRECT chat sees it; stranger / blocked does not  
3. Open your status → viewers list updates when peer opens it  
4. Settings → Privacy → Status → change audience → persists  

---

## Wave 19 — Call links

### V. Call links
1. Calls tab ⋮ → Create call link → voice or video → Copy / Share  
2. Second signed-in device opens link → joins same room  
3. Bad / expired code → error toast (does not crash)  

---

## Wave 20 — Admin ops *(browser)*

### X. Admin live pages
1. Admin → Settings / Android / Countries / Admins / Audit load from API (not mock empty shells)  
2. Toggle a country or Android min version → app config reflects it after refresh  
3. Optional: Live calls page shows ringing/active; Force end works  

---

## Wave 27 — Admin channels / stickers / firewall *(browser)*

### Z. Channels / stickers / IP gate
1. Admin → Channels → list loads; create a channel → appears  
2. Toggle **listed** off → channel disappears from app Channels list; toggle on → returns  
3. Open posts → **Hide** a post → gone from app channel feed  
4. Admin → Stickers → seeded packs listed (read-only note OK)  
5. Admin → Firewall → empty allowlist → login still works  
6. Optional: add your IP as only entry → login OK; wrong CIDR → `ip_blocked` (clear allowlist after)

---

## Wave 28 — Admin Legal / Security + Help docs

### AA. Legal / Security / Help
1. Admin → Legal → edit Privacy / Terms → Save → reopen shows saved text  
2. App → Help and feedback → Terms of service / Privacy policy → shows same copy  
3. Admin → Security → change session hours → Save → new login still works  
4. Optional: enable lock-after-fail, fail login several times → `login_locked` then wait / turn off lock  

---

## Wave 29 — Chat backup

### AB. Backup zip
1. Chats settings → Chat backup → **Last successful** = Never (or prior real time) — not a fake date  
2. Toggle Include media / Wi-Fi only → survives reopen  
3. **Back up** → share sheet opens with `.zip`; last/size update after success  
4. Optional: Wi-Fi only on + cellular → toast blocks backup  
5. Encrypted backup stays “not yet”; Open backup browses zips (Wave 50); frequency schedules WorkManager (Wave 48)

---

## Wave 30 — Account email & recovery

### AC. Email
1. Account → Email address → enter real email → Send verification → `000000` → Verified  
2. Two-step → Recovery email shows that address  
3. Sign out → Login → **Use email instead** → same email + `000000` → signed in (PIN if two-step on)  
4. Optional: two-step reset request/confirm with new PIN via email OTP  
5. Remove email → status clears  

---

## Wave 31 — Companion device link

### AD. Link device
1. Device B (signed out) → Login → **Link this device** → note 8-char code  
2. Device A (signed in) → Linked devices → Link a device → enter code → Link device  
3. Device B finishes into home / signed-in state  
4. Device A Linked devices list shows the new device; Sign out removes it  
5. Bad / expired code → error (does not crash)

---

## Wave 32 — Account info & security prefs

### AE. Account report / security notif
1. Account → Request account info → Request report → share sheet with `.json`  
2. Open JSON → has profile / privacy / devices; **no** message bodies  
3. Account → Security notifications → toggle off → leave → reopen = still off  

---

## Wave 33 — App lock & permissions

### AF. App lock / permissions
1. Privacy → App lock → enable → confirm biometric/PIN → toggle stays on  
2. Leave app (home) → reopen → unlock gate → biometric unlock → home visible  
3. Disable app lock → background/foreground no longer gates  
4. Settings → Permissions → rows show Allowed / Not allowed; tap opens system app settings  
5. Passkeys screen says coming later (no fake create)

---

## Wave 21 — Channels & communities

### O. Channels
1. Open Channels → seeded channels appear (e.g. Aivora News)  
2. Search filters the list  
3. FAB creates a channel → opens it  
4. Open a channel → posts load  
5. Follow / Unfollow toggles  
6. FAB in channel → post text → appears in feed  
7. React → reaction count updates  
8. Report → toast “Report sent”

### P. Communities
1. Settings → Communities → list (or Create community)  
2. Open a community → description + groups (if any)  
3. Join a non-joined group → opens that chat  

---

## Wave 22 — Account & identity

### A. Linked devices
1. Settings → Linked devices  
2. Live list (not mock “Chrome on macOS”)  
3. This device marked; **Sign out** on another row removes it  

### B–E. Two-step
1. Account → Two-step → Turn on → set PIN `123456` → Account shows **On**  
2. Log out → OTP → **PIN step** → wrong PIN fails → correct PIN → home  
3. Change PIN → re-login with new PIN  
4. Disable with PIN → next login is OTP-only  

### F. Change number
1. Account → Change number → new digits → Send code → `000000`  
2. Session / next login uses **new** number  

### G. Delete account
1. Delete account → wrong number errors → correct number → signed out permanently  

### H. SMS (optional)
Admin `sms_enabled` + Twilio env → real SMS; no `000000`; no OTP in logs  

---

## Wave 23 — APK size *(optional)*

### Y. Install / size
1. Release (or arm64 debug) installs on device  
2. Optional: confirm APK ~18MB arm64-only era (not ~75MB dual-ABI)  
3. Smoke after install: open chat + place/receive a call  

---

## Wave 24 — Chat actions & settings chrome

### I. Poll vote
1. Open a chat with a poll (or send a poll)  
2. Tap an option → counts / checkmark update  
3. Peer sees updated votes after refresh / realtime  

### J. Event RSVP
1. Event bubble → Going / Maybe / Can’t → counts + selection update  
2. Event details screen → same RSVP actions work  

### K. Privacy sub-screens
1. Privacy → Profile photo / About / Groups → pick Everyone / Contacts / Nobody (or groups variants) → reopen = persisted  
2. Privacy → Calls → toggle Silence unknown callers → survives reopen  

### L. Font size
1. Chats settings → Font size → Small / Medium / Large  
2. UI text scales; selection still selected after leaving the screen  

### M. Clear / delete all chats
1. Chat history → **Clear all** → confirm → messages emptied, chats remain  
2. Chat history → **Delete all** → confirm → chats leave the home list  

### N. Storage
1. Storage and data → Manage storage shows a **real** used size (not fake 1.2 GB)  
2. Chat rows (if any media with sizes) list largest first  

---

## Wave 25 — Stickers & chat lock

### Q. Stickers
1. Open a chat → Share (+) → Sticker  
2. Pick an emoji sticker → appears large in the thread  
3. Peer sees the same sticker after refresh / realtime  

### R. Chat lock
1. Privacy → Chat lock → Set secret code (4–8 digits) → Save  
2. Open a chat ⋮ / long-press → **Lock chat**  
3. Chat leaves home list; Locked chats footer still there  
4. Locked chats → enter code → list shows locked chat → open it  
5. Unlock chat from menu → returns to home list  

---

## Wave 26 — Live location

### S. Live location
1. Chat → Share → Location → **Share live location** → pick 15 minutes  
2. Bubble shows **Live location**; peer sees updates while app is open  
3. Privacy → Live location → lists active share → **Stop all** ends it  
4. Static **Send current location** still works  

---

## Wave 34 — Storage & data prefs

### AG. Auto-download / data
1. Settings → Storage and data → change **When using mobile data** → leave & reopen = same trailing value  
2. Change Wi-Fi / Roaming / Photo upload quality → each persists after reopen  
3. Toggle **Use less data for calls** → survives reopen  
4. Optional: Request account info JSON includes `media_prefs`  

---

## Wave 35 — Appearance

### AH. Theme / wallpaper
1. Settings → Chats → Theme (or Appearance) → pick **Dark** → UI switches to dark; survives cold start  
2. Pick **Light** / **System default** → matches choice  
3. Default chat theme → Mint (or Dusk/Sand) → open a chat **without** its own wallpaper → canvas uses that color  
4. Per-chat wallpaper still overrides default  
5. **Reset appearance** → System theme + Default wallpaper + Medium font  

---

## Wave 36 — Notifications

### AI. Notification prefs
1. Settings → Notifications → toggle Conversation tones / High priority / Reaction / Preview → reopen = persisted  
2. Messages / Groups / Calls → Vibrate Default / Long / Off → trailing updates and survives reopen  
3. Settings home Notifications toggle Off → no message banners; On restores  
4. Preview Off → notification body is “New message” (not chat text)  
5. **Reset notification settings** restores defaults  
6. Messages → Notification tone (Wave 47); Calls → Ringtone (Wave 53)

---

## Wave 37 — Chat prefs / archive all

### AJ. Enter send · archive all
1. Chats settings → **Enter is send** On → keyboard Send sends the message; Off → Enter inserts a newline  
2. Toggle survives reopen  
3. Keep chats archived Off → archive a chat → peer sends → chat returns to main list  
4. Keep On → archived chat stays archived when peer messages  
5. Archived chats → **Archive all** → confirm → home list empty of active chats; Archived folder has them  

---

## Wave 38 — Profile QR

### AK. My QR / scan
1. Settings → QR code → My QR code → QR image + name (if toggle on)  
2. Share link → opens share sheet with `https://…/u/{id}`  
3. On second account: Scan QR → Choose image of that QR → DM opens  
4. Optional: open shared HTTPS / `aivora://user/…` link while signed in → same DM  
5. Scanning your own QR → friendly error (not a crash)  

---

## Wave 39 — Links & read receipts

### AL. Privacy links / receipts
1. Privacy → Links → Everyone → stranger can open your QR and start a DM  
2. Links → Nobody → stranger scan / link → blocked with privacy message  
3. Toggle **Read receipts** Off → peer does not get blue ticks when you open their messages  
4. Both On again → blue ticks resume  
5. Privacy hub trailing labels match saved Links / Last seen / etc.  

---

## Wave 40 — Help & invite

### AM. Help / invite
1. Help and feedback → Help Center → open **Get started** (or any topic) → article body shows  
2. Contact us → email composer opens with version in the draft  
3. App info → Version matches APK; Terms / Privacy open  
4. Invite a friend → Share invitation → Copy link / Messages works  
5. Allow contacts → list loads → **Invite** opens SMS with the invite text  

---

## Wave 41 — Default timer & advanced

### AN. Defaults / advanced
1. Privacy → Disappearing messages → **7 days** → reopen = selected  
2. Start a **new** direct chat → chat uses 7-day disappearing (existing chats unchanged)  
3. Privacy → Advanced → toggle Disable link previews On → send a URL → no link card  
4. Toggle Off → send URL → preview returns  
5. Protect IP / Block unknown toggles survive reopen (Block unknown enforcement → Wave 49)  

---

## Wave 42 — Custom chat lists

### AO. Lists
1. Settings → New list → name + pick ≥1 chat → Create → opens edit  
2. Home Chats filter bar shows the new list chip → tap → only those chats  
3. Edit list → Rename / Add chats / Delete list  
4. Lists → long-press + drag (or prior Move up/down) → order persists after reopen  
5. Delete list → chip gone from Home  

---

## Wave 43 — Proxy + privacy checkup

### AP. Proxy / checkup
1. Settings → Storage and data → Proxy → enable + host/port (SOCKS5 or HTTP) → Save → row shows On  
2. Test connection → “Proxy reachable” (or clear failure toast)  
3. Turn Use proxy Off → Save → On becomes Off; open chats still works  
4. Privacy → Privacy checkup → Last seen / Status / Groups / App lock / receipts / links match current Privacy settings  

---

## Wave 44 — Live camera QR

### AQ. Camera scan
1. Settings → QR code → Scan QR code → allow Camera → live preview  
2. Point at another account’s My QR → DM opens (once)  
3. Scan your own QR → friendly error (not a crash)  
4. Deny camera → Allow camera / Choose image still usable  

---

## Wave 45 — Protect IP call relay

### AR. Force-relay
1. Privacy → Advanced → **Protect IP address in calls** On → place/answer 1:1 call → media connects  
2. Toggle Off → call again → still connects  
3. Toggle survives app reopen; next call follows the saved setting  

---

## Wave 46 — Drag-reorder lists

### AS. List drag
1. Settings → Lists → long-press a custom list → drag above/below another → release  
2. Order matches Home filter chip order after reopen  
3. Tap a list name → edit screen still opens  

---

## Wave 47 — Notification tone

### AT. Custom tone
1. Settings → Notifications → **Notification tone** → pick a system sound → trailing name updates  
2. With app on Home / background, receive a message → custom tone plays  
3. Reset notification settings → tone = Default again  

---

## Wave 48 — Scheduled backup

### AU. Auto backup
1. Chats → Chat backup → Automatic backups → **Daily** (or Weekly) → toast confirms  
2. Wi-Fi only On → work requires unmetered; Off → any network  
3. Set **Off** → automatic backups cancelled  
4. After a worker run: Last successful / size update (notification “Chat backup saved” optional)  

---

## Wave 49 — Block unknown

### AV. Unknown senders
1. Account A → Privacy → Advanced → **Block messages from unknown accounts** On  
2. Account B with no prior DM → scan A’s QR / New chat → blocked with clear message  
3. Existing A↔B chat (if any) still works; A can still start a chat with B  
4. A toggles Off → B can create a new DM  

---

## Wave 50 — Open backup

### AW. Browse zip
1. Chat backup → **Open backup** → Choose a backup zip (or pick an automatic backup on device)  
2. Chat list + format/date shown  
3. Tap a chat → share sheet with `.txt` transcript  

---

## Wave 51 — Transcripts

### AX. Multi-chat zip
1. Settings → Chats → **Chat transcripts** (or Chat history) → select ≥2 chats → Generate transcripts  
2. Share sheet opens with `.zip`  
3. Zip contains `manifest.json` + `chats/*/\_chat.txt`  

---

## Wave 52 — Silence unknown callers

### AY. Call silence
1. Account A → Privacy → Calls → **Silence unknown callers** On  
2. Account B with no prior DM calls A → A does not get ringing UI / loud call notify  
3. Call may appear as missed in A’s Calls after timeout  
4. Existing A↔B chat → call still rings normally  

---

## Wave 53 — Call ringtone

### AZ. Call ringtone
1. Settings → Notifications → Calls → **Ringtone** → pick a system ringtone  
2. Trailing label shows title; reopen = persisted  
3. Incoming call notification uses the picked tone  
4. **Reset notification settings** → Ringtone = Default  

---

## Wave 54 — Transfer / Add-account honesty

### BA. Honest transfer · switch number
1. Chats → **Transfer chats** → copy says peer QR transfer is not available  
2. **How to transfer** → steps point at Chat backup / Open backup; CTA opens Chat backup  
3. Account → **Add account** → “one account per install”; Passkeys / Email stay on this account  
4. **Sign in with another number** → confirm → Login; Back does not reopen a signed-in Home  

---

## Wave 55 — SMTP email OTP

### BB. SMTP delivery
1. Without SMTP env → email verify / sign-in still accept `000000` (or log code) when `OTP_DEV_BYPASS=1`  
2. With `SMTP_HOST` + `SMTP_FROM` (+ auth if needed) → request code → inbox receives mail; `000000` fails  
3. Bad SMTP credentials → send fails with error (not silent success)  
4. Settings → Email → SMTP live → helper says check inbox (no staging bypass line)  

---

## Wave 56 — Device link QR

### BC. Link QR
1. Device B → Login → **Link this device** → QR + code shown  
2. Device A (signed in) → Linked devices → Link a device → **Scan QR** → B finishes sign-in  
3. Typed pairing code still links  
4. Profile QR scan still opens a DM  

---

## Wave 57 — Media visibility gallery

### BD. Gallery publish
1. Chats → **Media visibility** On → send a photo → shows in Pictures/Aivora  
2. Visibility Off → send photo → not added to gallery  
3. Visibility On → open received photo → saved; Save icon still works when Off  
4. View-once never appears in gallery  

---

## Suggested single-session order

```
0 → W → T → U → V → O → P → Q → R → S → AB → AC → AD → AE → AF → AG → AH → AI → AJ → AK → AL → AM → AN → AO → AP → AQ → AR → AS → AT → AU → AV → AW → AX → AY → AZ → BA → BB → BC → BD → A → B–E → I → J → K → L → N → M → F → G
```

- `X` + `Z` + `AA` (admin) and `Y` (APK) when convenient  
- `H` only with Twilio  

Skip **G** until you are finished with that account.

---

## Coverage map

| Wave | Section | Doc |
|------|---------|-----|
| 16 | W | [WAVE16_CALLS.md](WAVE16_CALLS.md) |
| 17 | T | [WAVE17_EXPORT.md](WAVE17_EXPORT.md) |
| 18 | U | [WAVE18_STATUS.md](WAVE18_STATUS.md) |
| 19 | V | [WAVE19_CALL_LINKS.md](WAVE19_CALL_LINKS.md) |
| 20 | X | [WAVE20_ADMIN_OPS.md](WAVE20_ADMIN_OPS.md) |
| 21 | O–P | [WAVE21_CHANNELS.md](WAVE21_CHANNELS.md) |
| 22 | A–H | [WAVE22_IDENTITY.md](WAVE22_IDENTITY.md) |
| 23 | Y | [WAVE23_APK_SIZE.md](WAVE23_APK_SIZE.md) |
| 24 | I–N | [WAVE24_CHROME.md](WAVE24_CHROME.md) |
| 25 | Q–R | [WAVE25_STICKERS_LOCK.md](WAVE25_STICKERS_LOCK.md) |
| 26 | S | [WAVE26_LIVE_LOCATION.md](WAVE26_LIVE_LOCATION.md) |
| 27 | Z | [WAVE27_ADMIN.md](WAVE27_ADMIN.md) |
| 28 | AA | [WAVE28_LEGAL_SECURITY.md](WAVE28_LEGAL_SECURITY.md) |
| 29 | AB | [WAVE29_BACKUP.md](WAVE29_BACKUP.md) |
| 30 | AC | [WAVE30_EMAIL.md](WAVE30_EMAIL.md) |
| 31 | AD | [WAVE31_DEVICE_LINK.md](WAVE31_DEVICE_LINK.md) |
| 32 | AE | [WAVE32_ACCOUNT_INFO.md](WAVE32_ACCOUNT_INFO.md) |
| 33 | AF | [WAVE33_APP_LOCK.md](WAVE33_APP_LOCK.md) |
| 34 | AG | [WAVE34_AUTO_DOWNLOAD.md](WAVE34_AUTO_DOWNLOAD.md) |
| 35 | AH | [WAVE35_APPEARANCE.md](WAVE35_APPEARANCE.md) |
| 36 | AI | [WAVE36_NOTIFICATIONS.md](WAVE36_NOTIFICATIONS.md) |
| 37 | AJ | [WAVE37_CHAT_PREFS.md](WAVE37_CHAT_PREFS.md) |
| 38 | AK | [WAVE38_QR.md](WAVE38_QR.md) |
| 39 | AL | [WAVE39_PRIVACY.md](WAVE39_PRIVACY.md) |
| 40 | AM | [WAVE40_HELP_INVITE.md](WAVE40_HELP_INVITE.md) |
| 41 | AN | [WAVE41_DEFAULTS.md](WAVE41_DEFAULTS.md) |
| 42 | AO | [WAVE42_LISTS.md](WAVE42_LISTS.md) |
| 43 | AP | [WAVE43_PROXY.md](WAVE43_PROXY.md) |
| 44 | AQ | [WAVE44_QR_CAMERA.md](WAVE44_QR_CAMERA.md) |
| 45 | AR | [WAVE45_CALL_RELAY.md](WAVE45_CALL_RELAY.md) |
| 46 | AS | [WAVE46_LIST_DRAG.md](WAVE46_LIST_DRAG.md) |
| 47 | AT | [WAVE47_NOTIF_TONE.md](WAVE47_NOTIF_TONE.md) |
| 48 | AU | [WAVE48_BACKUP_SCHEDULE.md](WAVE48_BACKUP_SCHEDULE.md) |
| 49 | AV | [WAVE49_BLOCK_UNKNOWN.md](WAVE49_BLOCK_UNKNOWN.md) |
| 50 | AW | [WAVE50_OPEN_BACKUP.md](WAVE50_OPEN_BACKUP.md) |
| 51 | AX | [WAVE51_TRANSCRIPTS.md](WAVE51_TRANSCRIPTS.md) |
| 52 | AY | [WAVE52_SILENCE_CALLERS.md](WAVE52_SILENCE_CALLERS.md) |
| 53 | AZ | [WAVE53_CALL_RINGTONE.md](WAVE53_CALL_RINGTONE.md) |
| 54 | BA | [WAVE54_TRANSFER_HONESTY.md](WAVE54_TRANSFER_HONESTY.md) |
| 55 | BB | [WAVE55_SMTP.md](WAVE55_SMTP.md) |
| 56 | BC | [WAVE56_DEVICE_LINK_QR.md](WAVE56_DEVICE_LINK_QR.md) |
| 57 | BD | [WAVE57_MEDIA_VISIBILITY.md](WAVE57_MEDIA_VISIBILITY.md) |
