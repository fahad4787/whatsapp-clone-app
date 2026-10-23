# Wave 7 — FCM push setup

Backend and Android wiring are in place. Notifications only leave the server when Firebase credentials are configured.

## 1. Firebase Console (you)

1. Create a Firebase project (or use an existing one).
2. Android app package: `com.aivora.app.dev` — `google-services.json` is already in `aivora/app/`.
3. Project settings → **Service accounts** → Generate new private key (JSON) for the **API** (Render).

## 2. Render (API)

Add env var on the backend service:

- **Key:** `FCM_SERVICE_ACCOUNT_JSON`  
- **Value:** the entire service-account JSON as **one line** (minified).

Redeploy the API after saving.

Local optional alternative:

```bash
FCM_SERVICE_ACCOUNT_PATH=./secrets/firebase-adminsdk.json
```

## 3. Verify

1. Rebuild the Android app (with `google-services.json` present).
2. Log in on device A; grant notification permission if asked.
3. Force-stop / kill the app on device A.
4. Send a message from device B into that chat.
5. Device A should show an **Aivora** notification.

Without Firebase config, the API still stores push tokens and logs `[fcm] skip send (not configured)` — chat/WS keep working.

## Notes

- Online WebSocket peers are **not** pushed (live message already arrives).
- Release builds will use OTP + profile setup; debug still auto-skips those.
- Do **not** commit the service-account JSON or `google-services.json` if it contains secrets you care about (add to `.gitignore` if needed).
