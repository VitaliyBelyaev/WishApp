# Стиль кода и статический анализ

Оформление Kotlin проверяет и исправляет **ktlint**, смысл и сложность проверяет
**detekt** с правилами для Compose. Стиль подобран по коду, написанному до
редизайна (снимок `b0a80fd`, март 2026): так новый код выглядит как старый.

## Команды

```bash
./gradlew ktlintFormat detekt   # отформатировать и проверить, после каждой правки
./gradlew detektTyped           # detekt с проверкой типов, перед коммитом и вливанием
./gradlew ktlintCheck           # только проверить оформление, без правок
./gradlew detektBaseline        # пересобрать baseline без типов (только осознанно)
./gradlew detektTypedBaseline   # пересобрать baseline с типами (только осознанно)
scripts/install_git_hooks.sh    # подключить git pre-commit хук, один раз на репозиторий
```

`ktlintFormat`, `ktlintCheck` и `detekt` есть в `app` и `shared`, запуск из корня
проходит оба модуля. `ui/old` и `navigation/old` не проверяются: прежний вид не
меняется.

**Когда что запускать:**

- После правок Kotlin: `./gradlew ktlintFormat detekt`, несколько секунд.
  В конце хода агента это делает хук Claude Code.
- Перед коммитом: `ktlintCheck detekt detektTyped`, до пары минут, нужна
  компиляция. Это делает git pre-commit хук, если в коммите есть Kotlin.
- Перед вливанием ветки в `dev` (PR или прямой merge) обязательно
  `./gradlew ktlintCheck detekt detektTyped`, даже если хук не подключён или
  коммиты делались с `--no-verify`.

## Где настройки

| Что | Где |
|---|---|
| Версии ktlint, detekt, compose-rules | `gradle/libs.versions.toml` |
| Подключение к модулям, исключения | `build.gradle.kts` (корневой) |
| Правила ktlint | `.editorconfig`, секция `[{*.kt,*.kts}]` |
| Правила detekt (только отличия от встроенных) | `config/detekt/detekt.yml` |
| Принятые старые замечания detekt | `config/detekt/baseline-{app,shared}.xml` |
| Хук для агентов | `.claude/settings.json`, `scripts/hooks/static_analysis_on_stop.sh` |
| git pre-commit хук | `scripts/hooks/pre-commit`, подключение `scripts/install_git_hooks.sh` |
| Принятые замечания detekt с типами | `config/detekt/baseline-{app-debug,app-debugAndroidTest,shared-main}.xml` |
| Коммиты-переформатирования для `git blame` | `.git-blame-ignore-revs` |

## Как выглядит код

- Стиль ktlint `intellij_idea`, отступ 4, строка до 120 символов.
- Если параметры не влезают в одну строку, каждый на своей строке, `(` и `)` на
  отдельных строках. Короткие сигнатуры, написанные в столбик, ktlint не склеивает.
- Висячая запятая в многострочных списках параметров и аргументов.
- Файл заканчивается переводом строки.
- Тело функции блоком допустимо, ktlint не переделывает его в `= выражение`.
- Пустая строка после заголовка класса и в начале тела функции допустима.
- Ветки `when` идут без пустых строк между ними.
- Импорты одним списком по алфавиту, без `*` и без неиспользуемых: `ktlintFormat`
  сортирует их и удаляет лишние сам.
- `const val` в SCREAMING_SNAKE_CASE. Обычные `val` в Compose (`CardShape`) можно
  называть с заглавной буквы.
- `@Composable`-функции с заглавной буквы, колбэки в прошедшем времени
  (`onBackPressed`, `onSearchOpened`) — так принято в проекте, правило Compose про
  настоящее время выключено.
- Пакеты с подчёркиванием (`wish_list`) остаются, новые лучше без него.
- Имя файла с группой классов (`screenShowEvents.kt`) ktlint не проверяет;
  если в файле один класс, detekt требует назвать файл по нему.

## detekt

Работает поверх встроенного конфига (`buildUponDefaultConfig`) в двух режимах:

- **`detekt`**, без проверки типов: один таск на модуль по всей папке `src`,
  быстрый.
- **`detektTyped`**, с проверкой типов: `:app:detektDebug`,
  `:app:detektDebugUnitTest`, `:app:detektDebugAndroidTest`,
  `:shared:detektMainAndroid`. В `app` берётся только debug: в нём все папки
  исходников, а `detektMain` прогнал бы каждый вариант. `commonTest` и `iosMain`
  с типами не проверяются. Дополнительно ловит неиспользуемые импорты (в том числе
  из того же пакета), `Dispatchers` без `DispatcherProvider`, лишний
  `LaunchedEffect`, нехватку `@ReadOnlyComposable` и т. п. Сгенерированный код из
  `build/` (SQLDelight, BuildConfig) исключён.

Отличия от встроенного конфига:

- `LongMethod` до 100 строк, Compose-экраны длиннее обычных функций.
- `MagicNumber` не действует в `@Composable`/`@Preview`, в `ui/theme` и в тестах.
- `TooManyFunctions` не считает приватные функции и превью.
- `LongParameterList` не действует на `@Composable`: modifier и колбэки — норма.
- `UnusedPrivateFunction` не считает превью.
- `UnusedImport` включён, работает только в `detektTyped`.
- `Dispatchers` напрямую можно брать только в `DefaultDispatcherProvider`.
- Правила Compose (`io.nlopez.compose.rules`) включены все, кроме `ParameterNaming`;
  свои `CompositionLocal` перечислены в `CompositionLocalAllowlist`.

**Baseline.** Замечания, которые были на момент подключения, лежат в baseline,
detekt падает только на новых. У задач с типами свои файлы baseline по
вариантам, в них есть и замечания режима без типов. Новые замечания не добавляй в baseline, а исправляй.
Если правило мешает по делу, лучше `@Suppress("ИмяПравила")` с комментарием или
правка `detekt.yml`.

## Чего инструменты не ловят

- Импорт класса из того же пакета ловит только `detektTyped`, ktlint его не видит.
- Swift-код `iosApp` не проверяется.

## Агенты

В конце хода агента Claude Code запускает `scripts/hooks/static_analysis_on_stop.sh`.
Если в рабочей копии изменены `.kt`/`.kts` (кроме `ui/old`), он выполняет
`./gradlew ktlintFormat detekt`. При ошибках агент получает их список и правит
код. Второй заход подряд хук не блокирует, чтобы не зациклиться. Хук смотрит
только незакоммиченные изменения, `detektTyped` в нём нет: он долгий и работает
в git pre-commit хуке и перед вливанием.

## Грабли

- `core.hooksPath` общий для всех worktree репозитория. В ветках, где ещё нет
  `scripts/hooks/pre-commit`, git-хуков нет вообще.

- `no-unused-imports` с ktlint 1.7 выключено по умолчанию (будет удалено в 2.0),
  в `.editorconfig` оно включено явно. При переходе на ktlint 2.0 нужна замена,
  например `UnusedImport` в detekt с проверкой типов.
- `UnusedImport` в detekt без проверки типов (таск `detekt`) не срабатывает.

- ktlint-gradle при инкрементальном запуске помнит ошибки удалённых файлов,
  поэтому в корневом `build.gradle.kts` задачи ktlint каждый раз проверяют все
  файлы целиком.
- detekt 2.0 пока в альфе, compose-rules собраны под конкретную альфу.
  Поднимай `detekt` и `composeRules` вместе, сверяясь с POM compose-rules.
- Android Studio берёт отступы и запятые из того же `.editorconfig`, но
  её форматтер не во всём совпадает с ktlint. Последнее слово за `ktlintFormat`.
- После переформатирования всего проекта добавь хеш коммита в
  `.git-blame-ignore-revs`. Включить локально:
  `git config blame.ignoreRevsFile .git-blame-ignore-revs`.
