#!/usr/bin/env bash
#
# Снимает скриншоты всех экранов Android-приложения в
# docs/screenshots/<устройство>/<locale>-<theme>/, например
# emulator_pixel_7_api36/ru-light или device_google_pixel_8_api35/ru-light.
#
#   scripts/take_screenshots.sh                  # ru, светлая тема
#   scripts/take_screenshots.sh ru,en light,dark # все сочетания
#
# По умолчанию работает на отдельном AVD (WishApp_Screenshots), потому что тест
# стирает данные debug-приложения. Рабочие эмуляторы при этом не затрагиваются.
#
# Другое устройство выбирается явно, его debug-приложение тоже будет очищено:
#
#   DEVICE_SERIAL=R5CT1234 scripts/take_screenshots.sh

set -euo pipefail

LOCALES="${1:-ru}"
THEMES="${2:-light}"

AVD_NAME="WishApp_Screenshots"
EMULATOR_PORT="${EMULATOR_PORT:-5580}"
DEVICE_SERIAL="${DEVICE_SERIAL:-}"
SERIAL="${DEVICE_SERIAL:-emulator-$EMULATOR_PORT}"
APP_ID="ru.vitaliy.belyaev.wishapp.debug"
TEST_CLASS="ru.vitaliy.belyaev.wishapp.screenshots.ScreenshotsTest"
TEST_RUNNER="$APP_ID.test/androidx.test.runner.AndroidJUnitRunner"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SDK_DIR="${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' "$ROOT_DIR/local.properties")}"
ADB="$SDK_DIR/platform-tools/adb"
EMULATOR="$SDK_DIR/emulator/emulator"
AVDMANAGER="$SDK_DIR/cmdline-tools/latest/bin/avdmanager"
AVD_HOME="${ANDROID_AVD_HOME:-$HOME/.android/avd}"
AVD_CONFIG="$AVD_HOME/$AVD_NAME.avd/config.ini"
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

# Экранная клавиатура нужна на скринах: важно, как экран подстраивается под неё
disable_hardware_keyboard() {
    grep -v '^hw\.keyboard=' "$AVD_CONFIG" >"$AVD_CONFIG.tmp" || true
    echo "hw.keyboard=no" >>"$AVD_CONFIG.tmp"
    mv "$AVD_CONFIG.tmp" "$AVD_CONFIG"
}

emulator_started_here=false
emulator_pid=""
emulator_log=""

cleanup() {
    if [ "$emulator_started_here" = true ]; then
        device_adb emu kill >/dev/null 2>&1 || true
    fi
    restore_real_device
}
trap cleanup EXIT

is_emulator() {
    [[ "$SERIAL" == emulator-* ]]
}

device_prop() {
    device_adb shell getprop "$1" | tr -d '\r'
}

folder_safe() {
    tr '[:upper:]' '[:lower:]' | tr -c 'a-z0-9\n' '_'
}

# Скрины с разных устройств не должны смешиваться: это имя становится папкой
device_folder_name() {
    local api
    api="$(device_prop ro.build.version.sdk)"
    if is_emulator; then
        local avd skin
        avd="$(device_adb emu avd name | sed -n 1p | tr -d '\r')"
        skin="$(sed -n 's/^hw\.device\.name=//p' "$AVD_HOME/$avd.avd/config.ini" 2>/dev/null || true)"
        echo "emulator_${skin:-$avd}_api$api" | folder_safe
    else
        echo "device_$(device_prop ro.product.manufacturer)_$(device_prop ro.product.model)_api$api" | folder_safe
    fi
}

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
    disable_hardware_keyboard
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

real_device_settings_saved=false
animation_scale_before=""
night_mode_before=""

# Настоящему устройству возвращаем настройки, которые скрипт менял на время съёмки
save_real_device_settings() {
    is_emulator && return
    animation_scale_before="$(device_adb shell settings get global animator_duration_scale | tr -d '\r')"
    [ "$animation_scale_before" = "null" ] && animation_scale_before="1"
    night_mode_before="$(device_adb shell cmd uimode night | tr -d '\r' | sed 's/^Night mode: //')"
    real_device_settings_saved=true
}

restore_real_device() {
    [ "$real_device_settings_saved" = true ] || return 0
    local scale
    for scale in window_animation_scale transition_animation_scale animator_duration_scale; do
        device_adb shell settings put global "$scale" "$animation_scale_before" || true
    done
    device_adb shell cmd uimode night "$night_mode_before" >/dev/null || true
    device_adb shell am broadcast -a com.android.systemui.demo -e command exit >/dev/null || true
    device_adb shell settings put global sysui_demo_allowed 0 || true
}

prepare_device() {
    save_real_device_settings
    device_adb shell settings put global window_animation_scale 0
    device_adb shell settings put global transition_animation_scale 0
    device_adb shell settings put global animator_duration_scale 0

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
    local output_dir="$SCREENSHOTS_DIR/$locale-$theme"
    local night="no"
    [ "$theme" = "dark" ] && night="yes"

    echo "Снимаю $locale / $theme"
    device_adb shell pm clear "$APP_ID" >/dev/null
    device_adb shell cmd locale set-app-locales "$APP_ID" --locales "$locale"
    device_adb shell cmd uimode night "$night" >/dev/null

    local result
    local emulator_flag="false"
    is_emulator && emulator_flag="true"
    result="$(device_adb shell am instrument -w -e screenshots true -e locale "$locale" -e emulator "$emulator_flag" -e class "$TEST_CLASS" "$TEST_RUNNER" | tr -d '\r')"

    rm -rf "$output_dir"
    mkdir -p "$output_dir"
    device_adb pull "/sdcard/Android/data/$APP_ID/files/screenshots/." "$output_dir" >/dev/null

    echo "$result" | tail -n 3
    if ! echo "$result" | grep -q '^OK ('; then
        echo "$result"
        return 1
    fi
}

if [ -n "$DEVICE_SERIAL" ]; then
    if ! device_adb get-state >/dev/null 2>&1; then
        echo "Устройство $SERIAL не найдено" >&2
        exit 1
    fi
    wait_for_boot
else
    create_avd_if_needed
    start_emulator_if_needed
fi
SCREENSHOTS_DIR="$ROOT_DIR/docs/screenshots/$(device_folder_name)"
prepare_device

install_apks() {
    ANDROID_SERIAL="$SERIAL" "$ROOT_DIR/gradlew" -p "$ROOT_DIR" :app:installDebug :app:installDebugAndroidTest
}

# Gradle иногда не успевает прочитать свойства занятого эмулятора и пропускает его
install_apks || install_apks

exit_code=0
for locale in ${LOCALES//,/ }; do
    for theme in ${THEMES//,/ }; do
        run_tests "$locale" "$theme" || exit_code=1
    done
done

echo "Скриншоты: $SCREENSHOTS_DIR"
exit "$exit_code"
