#!/usr/bin/env bash
# Measure jank while tapping through Home tabs + open/back chat.
# Usage: ./scripts/perf-nav-jank.sh [serial]
set -euo pipefail
PKG=com.aivora.app.dev
ACT=com.aivora.app.ui.MainActivity
SERIAL="${1:-}"
ADB=(adb)
if [[ -n "$SERIAL" ]]; then ADB=(adb -s "$SERIAL"); fi

echo "== device =="
"${ADB[@]}" shell getprop ro.product.model
"${ADB[@]}" shell "cat /proc/meminfo | head -2"

"${ADB[@]}" shell am force-stop "$PKG" || true
sleep 0.4
"${ADB[@]}" shell am start -W -n "$PKG/$ACT" >/dev/null
# Let cold start settle BEFORE measuring — otherwise 99th% is dominated by TTI.
sleep 6
"${ADB[@]}" shell dumpsys gfxinfo "$PKG" reset >/dev/null || true
sleep 0.3

# Swipe / tap around home chrome (approx coords for mid phones 1080x2340-ish)
# Tab bar roughly y=2200 on tall phones — use pct via input tap with wm size
size=$("${ADB[@]}" shell wm size | tr -d '\r' | awk '{print $3}')
W=${size%x*}; H=${size#*x}
echo "wm=$W x $H"

tap() {
  local xp="$1" yp="$2"
  local x=$(( W * xp / 100 ))
  local y=$(( H * yp / 100 ))
  "${ADB[@]}" shell input tap "$x" "$y"
  sleep 0.55
}

# Chats → Updates → Calls → Profile → Chats
tap 20 92
tap 45 92
tap 70 92
tap 88 92
tap 20 92
# Open first chat row (~ below search)
tap 50 28
sleep 0.8
# Back (system)
"${ADB[@]}" shell input keyevent 4
sleep 0.6
# Open settings via profile tab overflow-ish: profile tab then scroll
tap 88 92
sleep 0.4

echo "== gfxinfo (jank) =="
"${ADB[@]}" shell dumpsys gfxinfo "$PKG" | tr -d '\r' | awk '
  /Janky frames:/ {print; j=1}
  /Total frames rendered:/ {print}
  /50th percentile:/ {print}
  /90th percentile:/ {print}
  /95th percentile:/ {print}
  /99th percentile:/ {print}
  /Number Missed Vsync:/ {print}
  /Number High input latency:/ {print}
  /HISTOGRAM/ {exit}
'
