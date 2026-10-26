# Bolta Punjab parity notes

Reference app on devices: `com.boltapunjab.app` **v1.12.13** (both Mate + Vivo).  
Aivora is the clone of this product (same shell: Chats / Updates / Calls / My Profile).

## Shipped (through Wave 17)

- Chat Waves 9–15; Perf F0–F8  
- LiveKit 1:1 / group / scheduled; admin live calls + force-end  
- Contact info, disappearing, block; voice bubbles; schedule call  
- Export chat (Settings + ⋮ → share .txt / .zip) — [WAVE17_EXPORT.md](WAVE17_EXPORT.md)  

## Remaining gaps → see [WAVE17_PLUS.md](WAVE17_PLUS.md)

| Priority | Gap | Wave |
|----------|-----|------|
| High | Updates/Status still local-only | **18** |
| Medium | Call links stub | **19** |
| Medium | Admin pages still mock | **20** |
| Medium | APK ~75MB vs Bolta ~30MB | **23** |
| Lower | Channels / communities | **21** |
| Later | Real SMS, two-step, passkeys, backup | **22 / 24** |

## Performance note
Continue PERF F6–F8 discipline; do not re-enable debug R8 minify.
