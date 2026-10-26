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

# Serials can contain spaces (e.g. wireless "name (2)._adb-tls-connect._tcp").
# Parse with awk: first field(s) until the status column "device".
TMP=$(mktemp)
trap 'rm -f "$TMP"' EXIT
adb devices | awk 'NR>1 && $NF=="device" {
  line=$0
  sub(/[[:space:]]+device[[:space:]]*$/, "", line)
  gsub(/^[[:space:]]+|[[:space:]]+$/, "", line)
  if (length(line)) print line
}' > "$TMP"

if [ ! -s "$TMP" ]; then
  echo "No adb devices connected." >&2
  exit 1
fi

COUNT=$(wc -l < "$TMP" | tr -d ' ')
echo "==> Installing on $COUNT device(s)"
FAIL=0
while IFS= read -r s; do
  [ -z "$s" ] && continue
  echo "---- $s"
  if adb -s "$s" install -r "$APK"; then
    echo "OK  $s"
    adb -s "$s" shell am start -n com.aivora.app.dev/com.aivora.app.ui.MainActivity >/dev/null 2>&1 || true
  else
    echo "FAIL $s" >&2
    FAIL=1
  fi
done < "$TMP"

if [ "$FAIL" -ne 0 ]; then
  exit 1
fi
echo "==> Done"
