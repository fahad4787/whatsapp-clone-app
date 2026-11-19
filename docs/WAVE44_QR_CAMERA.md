# Wave 44 — Live camera QR scan

**Status:** Done (2026-11-05)  
**Manual tests:** [TEST_CHECKLIST.md](TEST_CHECKLIST.md)

## Shipped

| Slice | What |
|-------|------|
| 44.1 | CameraX preview + ImageAnalysis on Scan QR |
| 44.2 | `QrCodec.decodeLuma` for YUV frames (zxing core) |
| 44.3 | CAMERA permission prompt; gallery “Choose image” kept |
| 44.4 | Debounced once-per-code open DM (same privacy / self checks) |

## Notes

- CameraX **1.4.2** (camera2 + lifecycle + view). No ML Kit.
- Live frames ignore non-Aivora codes silently; gallery still shows an error toast/text.
- Companion device-link QR added in Wave 56 (same camera scanner).

## Deploy

APK only (CameraX deps).

## Parked (later)

FIDO2, SMTP, E2EE, drag-and-drop list reorder, scheduled/encrypted backup. (Protect IP relay → Wave 45.)
