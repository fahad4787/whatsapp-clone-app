# Wave 43 — Proxy + privacy checkup

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 43.1 | `ProxyPrefs` — SOCKS5 / HTTP host+port, SharedPreferences + StateFlow |
| 43.2 | OkHttp `proxySelector` on shared + refresh clients; RealtimeClient inherits it |
| 43.3 | Settings → Storage → Proxy — enable, type, host, port, Save, Test `/health` |
| 43.4 | Privacy checkup shows live last-seen / status / groups / app lock / receipts / links |

## Notes

- Selector re-reads prefs on each connection — no client rebuild required after Save.
- Auth-less `/health` probe is enough to verify the proxy path; leave Use proxy Off for normal use.
- Call media (LiveKit) still uses its own transport — not forced through this proxy.

## Deploy

APK only (no backend migrate).

## Parked (later)

FIDO2, SMTP, E2EE, drag-and-drop list reorder, LiveKit force-relay. (Live camera QR → Wave 44.)
