# Wave 20 — Admin ops (mock → live)

Persist ops settings, countries allowlist, admins, and audit — and expose public app config to Android.

## Backend

| Endpoint | Auth | Notes |
|----------|------|--------|
| `GET /api/app/config` | public | Android version gates + enabled countries + limits |
| `GET/PUT /api/admin/settings` | admin | Full settings map (OTP, calls, Android, …) |
| `GET /api/admin/countries` | admin | All countries |
| `PUT /api/admin/countries/:iso` | admin | Toggle / edit |
| `GET /api/admin/admins` | admin | List operators |
| `POST /api/admin/admins` | SUPER | Invite |
| `GET /api/admin/audit` | admin | Recent admin actions |

Prisma: `AppConfig` (singleton JSON), `Country`. Migration `20261028020000_wave20_app_config`.

OTP request rejects `country_disabled` and respects `maintenance_mode` / `otp_gap` / `otp_expiry` from settings.

## Admin UI

Live (no mock store): **Settings**, **Android**, **Countries**, **Admins**, **Audit**.

## Android

- Typed `GET /api/app/config` → country picker on login; force-update hint if `versionCode < min_android_version_code`.

## Deploy

Push backend + run migrate on Render; redeploy admin frontend.
