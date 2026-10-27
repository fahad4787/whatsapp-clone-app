# Bolta Punjab parity notes

Reference app on devices: `com.boltapunjab.app` **v1.12.13** (both Mate + Vivo).  
Aivora is the clone of this product (same shell: Chats / Updates / Calls / My Profile).

## Shipped (through Wave 20)

- Chat Waves 9–15; Perf F0–F8  
- LiveKit 1:1 / group / scheduled; admin live calls + force-end  
- Contact info, disappearing, block; voice bubbles; schedule call  
- Export chat (Settings + ⋮ → share .txt / .zip) — [WAVE17_EXPORT.md](WAVE17_EXPORT.md)  
- Status / Updates server-backed — [WAVE18_STATUS.md](WAVE18_STATUS.md)  
- Call links create / share / deep-link join — [WAVE19_CALL_LINKS.md](WAVE19_CALL_LINKS.md)  
- Admin ops live (settings, Android gates, countries, admins, audit) — [WAVE20_ADMIN_OPS.md](WAVE20_ADMIN_OPS.md)  

## Remaining gaps → see [WAVE17_PLUS.md](WAVE17_PLUS.md)

| Priority | Gap | Wave |
|----------|-----|------|
| Medium | APK ~75MB vs Bolta ~30MB | **23** |
| Lower | Channels / communities | **21** |
| Later | Real SMS, two-step, passkeys, backup | **22 / 24** |

## Performance note
Continue PERF F6–F8 discipline; do not re-enable debug R8 minify.
