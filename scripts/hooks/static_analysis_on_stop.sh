#!/bin/bash
# Хук Claude Code (Stop): в конце хода агента форматирует Kotlin через ktlint
# и проверяет detekt, если в рабочей копии есть изменённые .kt/.kts.
# При ошибках возвращает код 2, и агент получает их и правит код.
# Подробности: docs/CODE_STYLE.md.

input=$(cat)
cd "${CLAUDE_PROJECT_DIR:-$(dirname "$0")/../..}" || exit 0

changed=$(git status --porcelain -- '*.kt' '*.kts' | grep -v -e '/ui/old/' -e '/navigation/old/')
[ -z "$changed" ] && exit 0

output=$(./gradlew -q --continue ktlintFormat detekt 2>&1)
[ $? -eq 0 ] && exit 0

# Второй заход подряд не блокируем, чтобы не зациклиться на том, что агент не может исправить
if echo "$input" | grep -q '"stop_hook_active": *true'; then
    exit 0
fi

{
    echo "ktlint или detekt нашли замечания. Исправь их (правила в docs/CODE_STYLE.md):"
    echo "$output" | grep -E '\.kts?:[0-9]+|What went wrong' | sed "s#$(pwd)/##" | head -60
} >&2
exit 2
