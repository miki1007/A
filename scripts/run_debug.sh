#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

JAVA_17="${JAVA_17_HOME:-/root/.local/share/mise/installs/java/17.0.2}"
if [[ -d "$JAVA_17" ]]; then
  export JAVA_HOME="$JAVA_17"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "Using Java: $(java -version 2>&1 | head -n 1)"

echo "Building debug APK..."
if [[ -f "gradle/wrapper/gradle-wrapper.jar" ]]; then
  ./gradlew --no-daemon :app:assembleDebug
else
  gradle --no-daemon :app:assembleDebug
fi

echo "APK built at app/build/outputs/apk/debug/app-debug.apk"
if command -v adb >/dev/null 2>&1; then
  if adb get-state >/dev/null 2>&1; then
    echo "Installing on connected device/emulator..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    adb shell am start -n com.mikix/.MainActivity
  else
    echo "No adb device connected; skipping install/start."
  fi
else
  echo "adb not installed; skipping install/start."
fi
