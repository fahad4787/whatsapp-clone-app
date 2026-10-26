# Bolta Punjab parity notes

Reference app on devices: `com.boltapunjab.app` **v1.12.13** (both Mate + Vivo).  
Aivora is the clone of this product (same shell: Chats / Updates / Calls / My Profile).

## Observed in Bolta (Mate, 2026-10-26)

### Chats (deep)
- Home row preview shows call outcomes: `Missed video call`, etc.
- In-thread **system notices** (centered chips):
  - `Missed voice call` / `Missed video call`
  - `Voice call cancelled` / `Video call cancelled`
  - `Video call declined` / voice declined
  - `Voice call · busy`
  - `Voice call · 0:00` (duration after connect)
- Chat header: last seen, Voice + Video call, More actions
- Chat ⋮: Clear chat, Contact info, Delete chat, Export chat, Mute, Schedule call, Search
- Voice bubbles: SeekBar + “Play voice message” + duration `0:00 / 0:04`
- Composer: Attach, Camera, Record voice note

### Already in Aivora
- Block / unblock, disappearing API, mute/archive/pin, calls LiveKit, Calls tab history labels

### Gaps to close (priority)
1. **In-chat call system messages** — ship notices when call ends (backend + UI) ← in progress
2. Home preview bump from those notices (follows last message)
3. Contact info: disappearing timer UX polish (API exists)
4. Perf: Bolta APK ~30MB vs Aivora ~75MB (LiveKit/native); keep list isolation + no duplicate keys

## Performance note
Bolta feels smoother on Mate — smaller APK, mature scroll. Continue PERF F6–F8 discipline; do not re-enable debug R8 minify.
