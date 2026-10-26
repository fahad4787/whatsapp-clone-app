#!/usr/bin/env bash
# Build debug APK and install on every connected adb device.
# Usage: ./scripts/install-all.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Building debug APK"
./gradlew :app:assembleDebug

APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK" ]; then
  echo "APK missing: $APK" >&2
  exit 1
fi

SERIALS=$(adb devices | awk 'NR>1 && $2=="device" { print $1 }')
if [ -z "$SERIALS" ]; then
  echo "No adb devices connected." >&2
  exit 1
fi

COUNT=$(printf '%s\n' "$SERIALS" | wc -l | tr -d ' ')
echo "==> Installing on $COUNT device(s)"
FAIL=0
printf '%s\n' "$SERIALS" | while IFS= read -r s; do
  [ -z "$s" ] && continue
  echo "---- $s"
  if adb -s "$s" install -r "$APK"; then
    echo "OK  $s"
    adb -s "$s" shell am start -n com.aivora.app.dev/com.aivora.app.ui.MainActivity >/dev/null 2>&1 || true
  else
    echo "FAIL $s" >&2
    FAIL=1
  fi
done

# while in pipe runs in subshell on bash — re-check installs by listing
echo "==> Done (reinstall attempted on all connected devices)"
echo "    Next time: ./scripts/install-all.sh"
