#!/usr/bin/env bash
# Fast dev loop: build the debug app and install + open it on one phone.
# Debug skips R8 shrinking, so a warm rebuild takes seconds instead of minutes.
# Usage:
#   ./scripts/dev-install.sh            # Vivo (or the only connected phone)
#   ./scripts/dev-install.sh Mate10     # any part of another phone's adb serial
#   ./scripts/dev-install.sh all        # every connected phone
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
WANT="${1:-10FF6S07560006H}"   # Vivo's serial
PKG=com.aivora.app.dev
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"

start=$(date +%s)
./gradlew :app:assembleDebug -q 2>&1 | grep -v '^w:' || true
[ -f "$APK" ] || { echo "Build failed: no APK" >&2; exit 1; }
built=$(date +%s)

SERIALS=$(adb devices | awk 'NR>1 && $NF=="device" {sub(/[[:space:]]+device[[:space:]]*$/, ""); print}')
[ -n "$SERIALS" ] || { echo "No phone connected (adb devices is empty)." >&2; exit 1; }
if [ "$WANT" != all ]; then
  PICK=$(printf '%s\n' "$SERIALS" | grep -F -- "$WANT" || true)
  [ -n "$PICK" ] || { [ "$(printf '%s\n' "$SERIALS" | wc -l)" -eq 1 ] && PICK="$SERIALS"; }
  [ -n "$PICK" ] || { echo "Phone '$WANT' not connected. Connected:" >&2; echo "$SERIALS" >&2; exit 1; }
  SERIALS="$PICK"
fi

while IFS= read -r s; do
  [ -z "$s" ] && continue
  # --incremental (Android 11+) lets the app start while the APK streams; fall back to a normal install.
  # </dev/null: adb otherwise reads the rest of the phone list and only the first phone gets it.
  if ! adb -s "$s" install -r --incremental "$APK" </dev/null >/dev/null 2>&1; then
    adb -s "$s" install -r "$APK" </dev/null >/dev/null
  fi
  adb -s "$s" shell am start -n "$PKG/com.aivora.app.ui.MainActivity" </dev/null >/dev/null 2>&1 || true
  echo "Installed on $s"
done <<< "$SERIALS"

done_t=$(date +%s)
echo "Build $((built - start))s, install $((done_t - built))s"
