#!/usr/bin/env bash
#
# Быстрая проверка, что приложение собирается, запускается и основные экраны
# открываются. Удобно запускать после обновления зависимостей.
#
#   scripts/run_smoke_test.sh
#
# По умолчанию работает на отдельном AVD (WishApp_Screenshots, его создаёт
# scripts/take_screenshots.sh), потому что тест стирает данные debug-приложения.
#
# Другое устройство выбирается явно, его debug-приложение тоже будет очищено:
#
#   DEVICE_SERIAL=R5CT1234 scripts/run_smoke_test.sh

set -euo pipefail

AVD_NAME="WishApp_Screenshots"
EMULATOR_PORT="${EMULATOR_PORT:-5554}"
DEVICE_SERIAL="${DEVICE_SERIAL:-}"
APP_ID="ru.vitaliy.belyaev.wishapp.debug"
TEST_CLASS="ru.vitaliy.belyaev.wishapp.smoke.SmokeTest"
TEST_RUNNER="$APP_ID.test/androidx.test.runner.AndroidJUnitRunner"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SDK_DIR="${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' "$ROOT_DIR/local.properties")}"
ADB="$SDK_DIR/platform-tools/adb"
EMULATOR="$SDK_DIR/emulator/emulator"
BOOT_TIMEOUT_SECONDS=300

emulator_started_here=false

find_running_avd() {
    local serial
    for serial in $("$ADB" devices | awk '/^emulator-/ { print $1 }'); do
        if [ "$("$ADB" -s "$serial" emu avd name | sed -n 1p | tr -d '\r')" = "$AVD_NAME" ]; then
            echo "$serial"
            return
        fi
    done
}

SERIAL="$DEVICE_SERIAL"
[ -n "$SERIAL" ] || SERIAL="$(find_running_avd)"

if [ -z "$SERIAL" ]; then
    if ! "$EMULATOR" -list-avds | grep -qx "$AVD_NAME"; then
        echo "AVD $AVD_NAME не найден, его создаёт scripts/take_screenshots.sh" >&2
        exit 1
    fi
    echo "Запускаю $AVD_NAME"
    SERIAL="emulator-$EMULATOR_PORT"
    "$EMULATOR" -avd "$AVD_NAME" -port "$EMULATOR_PORT" \
        -no-window -no-audio -no-boot-anim -no-snapshot-save >/dev/null 2>&1 &
    emulator_started_here=true
fi

device_adb() {
    "$ADB" -s "$SERIAL" "$@"
}

cleanup() {
    if [ "$emulator_started_here" = true ]; then
        device_adb emu kill >/dev/null 2>&1 || true
    fi
}
trap cleanup EXIT

started_at="$SECONDS"
until [ "$(device_adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    if [ $((SECONDS - started_at)) -ge "$BOOT_TIMEOUT_SECONDS" ]; then
        echo "$SERIAL не загрузился за $BOOT_TIMEOUT_SECONDS с" >&2
        exit 1
    fi
    sleep 2
done

for scale in window_animation_scale transition_animation_scale animator_duration_scale; do
    device_adb shell settings put global "$scale" 0
done

install_apks() {
    ANDROID_SERIAL="$SERIAL" "$ROOT_DIR/gradlew" -p "$ROOT_DIR" :app:installDebug :app:installDebugAndroidTest
}

# Gradle иногда не успевает прочитать свойства занятого эмулятора и пропускает его
install_apks || install_apks

device_adb shell pm clear "$APP_ID" >/dev/null
result="$(device_adb shell am instrument -w -e smoke true -e class "$TEST_CLASS" "$TEST_RUNNER" | tr -d '\r')"
echo "$result"
echo "$result" | grep -q '^OK ('
