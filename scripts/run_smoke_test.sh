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
#
# Перед релизом тот же тест гоняется на сборке autotest: она как release, с R8,
# и ловит то, что R8 вырезал или переименовал:
#
#   BUILD_TYPE=autotest scripts/run_smoke_test.sh

set -euo pipefail

AVD_NAME="WishApp_Screenshots"
EMULATOR_PORT="${EMULATOR_PORT:-5554}"
DEVICE_SERIAL="${DEVICE_SERIAL:-}"
BUILD_TYPE="${BUILD_TYPE:-debug}"
case "$BUILD_TYPE" in
    debug|autotest) ;;
    *) echo "BUILD_TYPE может быть debug или autotest, а не $BUILD_TYPE" >&2; exit 1 ;;
esac
BUILD_TYPE_TASK="$(printf '%s' "${BUILD_TYPE:0:1}" | tr '[:lower:]' '[:upper:]')${BUILD_TYPE:1}"
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
    # Порт может занимать другой эмулятор, например рабочий: тогда новый падает, а адрес
    # emulator-<порт> указывает на чужой, и тест стирал бы и выключал его. Берём свободный порт
    while "$ADB" devices | grep -q "^emulator-$EMULATOR_PORT[[:space:]]"; do
        EMULATOR_PORT=$((EMULATOR_PORT + 2))
    done
    echo "Запускаю $AVD_NAME на порту $EMULATOR_PORT"
    SERIAL="emulator-$EMULATOR_PORT"
    "$EMULATOR" -avd "$AVD_NAME" -port "$EMULATOR_PORT" \
        -no-window -no-audio -no-boot-anim -no-snapshot-save >/dev/null 2>&1 &
    emulator_started_here=true
fi

device_adb() {
    "$ADB" -s "$SERIAL" "$@"
}

running_avd_name() {
    device_adb emu avd name 2>/dev/null | sed -n 1p | tr -d '\r'
}

cleanup() {
    # Выключаем только свой эмулятор
    if [ "$emulator_started_here" = true ] && [ "$(running_avd_name)" = "$AVD_NAME" ]; then
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

# Последняя страховка: на чужом эмуляторе ничего не стираем
if [ -z "$DEVICE_SERIAL" ] && [ "$(running_avd_name)" != "$AVD_NAME" ]; then
    echo "$SERIAL — это не $AVD_NAME, тест не запускаю" >&2
    exit 1
fi

for scale in window_animation_scale transition_animation_scale animator_duration_scale; do
    device_adb shell settings put global "$scale" 0
done

install_apks() {
    ANDROID_SERIAL="$SERIAL" "$ROOT_DIR/gradlew" -p "$ROOT_DIR" -PtestBuildType="$BUILD_TYPE" \
        ":app:install$BUILD_TYPE_TASK" ":app:install${BUILD_TYPE_TASK}AndroidTest"
}

# Gradle иногда не успевает прочитать свойства занятого эмулятора и пропускает его
install_apks || install_apks

device_adb shell pm clear "$APP_ID" >/dev/null
result="$(device_adb shell am instrument -w -e smoke true -e class "$TEST_CLASS" "$TEST_RUNNER" | tr -d '\r')"
echo "$result"
echo "$result" | grep -q '^OK ('
