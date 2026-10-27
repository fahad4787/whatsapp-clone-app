# Wave 19 — Call links

Shareable LiveKit join without a prior chat.

**Staging share URL (for now):** `https://whatsapp-clone-backend-hep1.onrender.com/call/{code}`  
(also accepts `aivora://call/{code}`). Not `wa.zirvik.com` / admin.

## Backend

| Endpoint | Auth | Behavior |
|----------|------|----------|
| `POST /api/call-links` | yes | Create link (`media`: audio\|video, optional `title`). TTL **7 days**. |
| `GET /api/call-links/:code` | yes | Preview metadata |
| `POST /api/call-links/:code` | yes | Join / create active `Call` (`kind=link`), mint LiveKit token |

Prisma: `CallLink` (+ migration `20261028010000_wave19_call_links`). Deploy migrate on the live DB before relying on create in production.

## Android

- Calls tab ⋮ → **Create call link** → voice/video → **Copy** / **Share**
- Deep link (staging Render API host or `aivora://call/{code}`) → `JoinCallLinkScreen` → LiveKit
- Route: `call-link/{code}`

## Verify

1. Device A: create link → share/copy  
2. Device B (signed in): open link → joins same room  
3. Expired / unknown code → toast + error text  
