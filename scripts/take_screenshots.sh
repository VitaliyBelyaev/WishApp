#!/usr/bin/env bash
#
# Снимает скриншоты всех экранов Android-приложения в docs/screenshots/<locale>-<theme>/.
#
#   scripts/take_screenshots.sh                  # ru, светлая тема
#   scripts/take_screenshots.sh ru,en light,dark # все сочетания
#
# Работает на отдельном AVD (WishApp_Screenshots), потому что тест стирает данные
# debug-приложения. Рабочие эмуляторы и данные на них не затрагиваются.

set -euo pipefail

LOCALES="${1:-ru}"
THEMES="${2:-light}"

AVD_NAME="WishApp_Screenshots"
EMULATOR_PORT=5580
SERIAL="emulator-$EMULATOR_PORT"
APP_ID="ru.vitaliy.belyaev.wishapp.debug"
TEST_CLASS="ru.vitaliy.belyaev.wishapp.screenshots.ScreenshotsTest"
TEST_RUNNER="$APP_ID.test/androidx.test.runner.AndroidJUnitRunner"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SDK_DIR="${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' "$ROOT_DIR/local.properties")}"
ADB="$SDK_DIR/platform-tools/adb"
EMULATOR="$SDK_DIR/emulator/emulator"
AVDMANAGER="$SDK_DIR/cmdline-tools/latest/bin/avdmanager"
AVD_CONFIG="${ANDROID_AVD_HOME:-$HOME/.android/avd}/$AVD_NAME.avd/config.ini"
BOOT_TIMEOUT_SECONDS=300

device_adb() {
    "$ADB" -s "$SERIAL" "$@"
}

create_avd_if_needed() {
    if "$EMULATOR" -list-avds | grep -qx "$AVD_NAME"; then
        return
    fi
    local abi="arm64-v8a"
    [ "$(uname -m)" = "arm64" ] || abi="x86_64"
    echo "Создаю AVD $AVD_NAME"
    echo no | "$AVDMANAGER" create avd \
        --name "$AVD_NAME" \
        --device pixel_7 \
        --package "system-images;android-36;google_apis_playstore;$abi"
}

# Аппаратная клавиатура: экранная не перекрывает экраны с полями ввода
enable_hardware_keyboard() {
    grep -v '^hw\.keyboard=' "$AVD_CONFIG" >"$AVD_CONFIG.tmp" || true
    echo "hw.keyboard=yes" >>"$AVD_CONFIG.tmp"
    mv "$AVD_CONFIG.tmp" "$AVD_CONFIG"
}

emulator_started_here=false
emulator_pid=""
emulator_log=""

stop_emulator() {
    if [ "$emulator_started_here" = true ]; then
        device_adb emu kill >/dev/null 2>&1 || true
    fi
}
trap stop_emulator EXIT

# Тест стирает данные приложения, поэтому чужой эмулятор на этом порту использовать нельзя
check_running_avd() {
    local running_avd
    running_avd="$(device_adb emu avd name | sed -n 1p | tr -d '\r')"
    if [ "$running_avd" != "$AVD_NAME" ]; then
        echo "На $SERIAL запущен AVD '$running_avd', а нужен $AVD_NAME" >&2
        exit 1
    fi
}

start_emulator_if_needed() {
    if device_adb get-state >/dev/null 2>&1; then
        check_running_avd
        wait_for_boot
        return
    fi
    echo "Запускаю $AVD_NAME"
    enable_hardware_keyboard
    emulator_log="$(mktemp)"
    "$EMULATOR" -avd "$AVD_NAME" -port "$EMULATOR_PORT" \
        -no-window -no-audio -no-boot-anim -no-snapshot-save >"$emulator_log" 2>&1 &
    emulator_pid=$!
    emulator_started_here=true
    wait_for_boot
}

wait_for_boot() {
    local started_at="$SECONDS"
    until [ "$(device_adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
        if [ -n "$emulator_pid" ] && ! kill -0 "$emulator_pid" 2>/dev/null; then
            echo "Эмулятор завершился при запуске. Возможно, $AVD_NAME уже открыт на другом порту:" >&2
            tail -n 20 "$emulator_log" >&2
            exit 1
        fi
        if [ $((SECONDS - started_at)) -ge "$BOOT_TIMEOUT_SECONDS" ]; then
            echo "$SERIAL не загрузился за $BOOT_TIMEOUT_SECONDS с" >&2
            exit 1
        fi
        sleep 2
    done
}

prepare_device() {
    device_adb shell settings put global window_animation_scale 0
    device_adb shell settings put global transition_animation_scale 0
    device_adb shell settings put global animator_duration_scale 0
    device_adb shell settings put secure show_ime_with_hard_keyboard 0

    # Статус-бар без случайных часов, значков и уведомлений
    device_adb shell settings put global sysui_demo_allowed 1
    demo_command enter
    demo_command clock -e hhmm 1200
    demo_command battery -e level 100 -e plugged false
    demo_command network -e wifi show -e level 4 -e fully true
    demo_command network -e mobile hide
    demo_command notifications -e visible false
}

demo_command() {
    local command="$1"
    shift
    device_adb shell am broadcast -a com.android.systemui.demo -e command "$command" "$@" >/dev/null
}

run_tests() {
    local locale="$1"
    local theme="$2"
    local output_dir="$ROOT_DIR/docs/screenshots/$locale-$theme"
    local night="no"
    [ "$theme" = "dark" ] && night="yes"

    echo "Снимаю $locale / $theme"
    device_adb shell pm clear "$APP_ID" >/dev/null
    device_adb shell cmd locale set-app-locales "$APP_ID" --locales "$locale"
    device_adb shell cmd uimode night "$night" >/dev/null

    local result
    result="$(device_adb shell am instrument -w -e screenshots true -e class "$TEST_CLASS" "$TEST_RUNNER" | tr -d '\r')"

    rm -rf "$output_dir"
    mkdir -p "$output_dir"
    device_adb pull "/sdcard/Android/data/$APP_ID/files/screenshots/." "$output_dir" >/dev/null

    echo "$result" | tail -n 3
    if ! echo "$result" | grep -q '^OK ('; then
        echo "$result"
        return 1
    fi
}

create_avd_if_needed
start_emulator_if_needed
prepare_device

ANDROID_SERIAL="$SERIAL" "$ROOT_DIR/gradlew" -p "$ROOT_DIR" :app:installDebug :app:installDebugAndroidTest

exit_code=0
for locale in ${LOCALES//,/ }; do
    for theme in ${THEMES//,/ }; do
        run_tests "$locale" "$theme" || exit_code=1
    done
done

echo "Скриншоты: $ROOT_DIR/docs/screenshots"
exit "$exit_code"
