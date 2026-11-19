# Bolta Punjab parity notes

Reference app on devices: `com.boltapunjab.app` **v1.12.13** (both Mate + Vivo).  
Aivora is the clone of this product (same shell: Chats / Updates / Calls / My Profile).

## Shipped (through Wave 52)

- Chat Waves 9–15; Perf F0–F8  
- LiveKit 1:1 / group / scheduled; admin live calls + force-end  
- Contact info, disappearing, block; voice bubbles; schedule call  
- Export chat (Settings + ⋮ → share .txt / .zip) — [WAVE17_EXPORT.md](WAVE17_EXPORT.md)  
- Status / Updates server-backed — [WAVE18_STATUS.md](WAVE18_STATUS.md)  
- Call links create / share / deep-link join — [WAVE19_CALL_LINKS.md](WAVE19_CALL_LINKS.md)  
- Admin ops live (settings, Android gates, countries, admins, audit) — [WAVE20_ADMIN_OPS.md](WAVE20_ADMIN_OPS.md)  
- APK size: arm64-only release **~18MB** (was ~75MB dual-ABI era) — [WAVE23_APK_SIZE.md](WAVE23_APK_SIZE.md)  
- Account identity: devices revoke, two-step PIN, delete/change number, optional Twilio SMS — [WAVE22_IDENTITY.md](WAVE22_IDENTITY.md)  
- Chat chrome: poll vote / RSVP, privacy extras, font size, clear-all, storage — [WAVE24_CHROME.md](WAVE24_CHROME.md)  
- Channels / communities — [WAVE21_CHANNELS.md](WAVE21_CHANNELS.md)  
- Stickers + chat lock — [WAVE25_STICKERS_LOCK.md](WAVE25_STICKERS_LOCK.md)  
- Live location — [WAVE26_LIVE_LOCATION.md](WAVE26_LIVE_LOCATION.md)  
- Admin channels / stickers / IP firewall — [WAVE27_ADMIN.md](WAVE27_ADMIN.md)  
- Admin Legal / Security + Help Terms/Privacy — [WAVE28_LEGAL_SECURITY.md](WAVE28_LEGAL_SECURITY.md)  
- Chat backup (portable zip / share sheet) — [WAVE29_BACKUP.md](WAVE29_BACKUP.md)  
- Account email verify / email sign-in / two-step email reset — [WAVE30_EMAIL.md](WAVE30_EMAIL.md)  
- Companion device link (pairing code) — [WAVE31_DEVICE_LINK.md](WAVE31_DEVICE_LINK.md)  
- Account info report + security notif prefs — [WAVE32_ACCOUNT_INFO.md](WAVE32_ACCOUNT_INFO.md)  
- App lock (biometric) + live Permissions — [WAVE33_APP_LOCK.md](WAVE33_APP_LOCK.md)  
- Media auto-download / data prefs — [WAVE34_AUTO_DOWNLOAD.md](WAVE34_AUTO_DOWNLOAD.md)  
- Appearance theme + default wallpaper — [WAVE35_APPEARANCE.md](WAVE35_APPEARANCE.md)  
- Notification prefs — [WAVE36_NOTIFICATIONS.md](WAVE36_NOTIFICATIONS.md)  
- Chat prefs / archive all — [WAVE37_CHAT_PREFS.md](WAVE37_CHAT_PREFS.md)  
- Profile QR (my code / gallery scan / deep link) — [WAVE38_QR.md](WAVE38_QR.md)  
- Links privacy + read receipts — [WAVE39_PRIVACY.md](WAVE39_PRIVACY.md)  
- Help center & invite — [WAVE40_HELP_INVITE.md](WAVE40_HELP_INVITE.md)  
- Default timer & advanced privacy — [WAVE41_DEFAULTS.md](WAVE41_DEFAULTS.md)  
- Custom chat lists — [WAVE42_LISTS.md](WAVE42_LISTS.md)  
- Proxy + live privacy checkup — [WAVE43_PROXY.md](WAVE43_PROXY.md)  
- Live camera QR scan — [WAVE44_QR_CAMERA.md](WAVE44_QR_CAMERA.md)  
- Protect IP → LiveKit force-relay — [WAVE45_CALL_RELAY.md](WAVE45_CALL_RELAY.md)  
- Drag-reorder chat lists — [WAVE46_LIST_DRAG.md](WAVE46_LIST_DRAG.md)  
- Notification tone picker — [WAVE47_NOTIF_TONE.md](WAVE47_NOTIF_TONE.md)  
- Scheduled chat backup — [WAVE48_BACKUP_SCHEDULE.md](WAVE48_BACKUP_SCHEDULE.md)  
- Block unknown messages — [WAVE49_BLOCK_UNKNOWN.md](WAVE49_BLOCK_UNKNOWN.md)  
- Open backup browser — [WAVE50_OPEN_BACKUP.md](WAVE50_OPEN_BACKUP.md)  
- Generate chat transcripts — [WAVE51_TRANSCRIPTS.md](WAVE51_TRANSCRIPTS.md)  
- Silence unknown callers — [WAVE52_SILENCE_CALLERS.md](WAVE52_SILENCE_CALLERS.md)  

## Remaining gaps → see [WAVE17_PLUS.md](WAVE17_PLUS.md)

| Priority | Gap | Wave |
|----------|-----|------|
| Later | FIDO2 passkeys; encrypted cloud backup; E2EE | park |
| 55 | SMTP email OTP (optional env) | done |

## Performance note
Continue PERF F6–F8 discipline; do not re-enable debug R8 minify.
