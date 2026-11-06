# AGENTS.md — WishApp («Хотелки»)

Краткая инструкция для агентов. Полная карта проекта и детали — в
`docs/ARCHITECTURE.md`.

## Что за проект

**WishApp / «Хотелки»** — Kotlin Multiplatform-приложение для списка желаний:
теги, ссылки, комментарии, шаринг (Android: текст/PDF), изображения (Android),
локальная SQLite, бэкап в Google Drive (Android) / iCloud (iOS).

- Android: `1.9.0` (`versionCode 25`), Google Play `ru.vitaliy.belyaev.wishapp`
- iOS: `1.1.0` (`CURRENT_PROJECT_VERSION 7`), ранее App Store `id6450624836`

## Статус платформ

- **Android** — активная разработка и поддержка.
- **iOS** — активная разработка и поддержка приостановлены; приложение сейчас
  отсутствует в App Store, дальнейшее развитие на паузе.
- При этом `shared` используется обеими платформами: изменения в общем коде
  **не должны ломать компиляцию модуля под iOS-таргеты**. Само приложение в Xcode
  на каждое изменение не собираем, если его поднимут, сломанное чиним тогда.
  Новые iOS-only фичи не приоритет.

## Модули

- `shared` — KMP: домен, репозитории, SQLDelight, SQLite. Здесь бизнес-логика.
- `app` — Android: Compose/Material3, Navigation3, Hilt, DataStore,
  Google Drive, Firebase/Amplitude.
- `iosApp` — iOS: SwiftUI, Combine, Koin, iCloud, Amplitude.
- `gradle/libs.versions.toml` — версии зависимостей.

## С чего начать

1. `shared/.../data/WishAppSdk.kt`
2. `shared/.../data/repository/DatabaseRepository.kt`
3. `app/.../ui/AppActivity.kt`
4. `app/.../navigation/old/Navigation.kt`
5. `iosApp/WishApp/WishApp.swift`
6. `iosApp/WishApp/View/Main/Components/MainContentView.swift`

## Архитектура в двух строках

- Android: Compose Screen → ViewModel (Hilt) → интерфейсы репозиториев `shared`
  → `DatabaseRepository` → SQLDelight/SQLite.
- iOS: SwiftUI View → ObservableObject → KMP-NativeCoroutines → `WishAppSdk`
  → `DatabaseRepository` → SQLDelight/SQLite.

## Жёсткие правила

1. **Бизнес-логику и SQL держи в `shared`.** Не дублируй правила на платформах.
2. **Публичные методы `shared`, нужные из Swift, помечай `@NativeCoroutines`.**
3. **Меняешь схему БД** — добавь `.sq` + `.sqm`, обнови snapshot в
   `shared/src/commonMain/sqldelight/databases/`, проверь мапперы.
   В `shared/build.gradle.kts` включён `verifyMigrations = true`.
4. **Ссылки — только через `LinksAdapter`.** `Wish.link` хранится строкой с
   разделителем `|`, `WishEntity.links` — производное поле.
5. **`position`:** активные `>= 0`, выполненные `-1`, default sort — `position DESC`;
   новые желания получают `count(valid positions)`.
6. **Android DI — Hilt; Koin — только `shared`/iOS.** Android не вызывает `initKoin()`.
7. **Не редактируй** `build/`, `shared/build/`, `Pods/`.
8. **Не коммить** `local.properties`, `apikey.properties`, keystore; не свети ключи.
9. **Android-навигация — только Navigation3** (`Routes.kt` / `Navigation.kt`).
10. **iOS на паузе, но `shared` — общий.** После изменений в `shared` проверяй,
    что модуль компилируется и линкуется под iOS из Gradle (команда в разделе
    «Сборка и проверки»). Приложение `iosApp` через Xcode и симулятор не собирай
    и Swift-сигнатуры не проверяй, если об этом не попросили.

## Карта экранов (кратко)

- Android: WishList (`ui/old/screens/wish_list`), WishDetailed (`wishdetailed`),
  WishTags (`wishtags`), EditTags (`edittags`), WishImagesViewer (`wish_images`),
  Settings, Backup, AboutApp, PrivacyPolicy.
- iOS (сейчас на паузе, карта — справочно): Main (`View/Main`), WishList
  (`View/WishList`), WishDetailed (`View/WishDetailed`), UpdateWishTags
  (`View/UdpateWishTags`), Settings, Backup.

Подробные описания, ViewModel'и и особенности — в `docs/ARCHITECTURE.md`, разделы 6–7.

## Где что искать

| Что нужно | Куда смотреть |
|---|---|
| Схема БД | `shared/src/commonMain/sqldelight/.../*.sq` |
| Миграции | `shared/src/commonMain/sqldelight/.../migrations/*.sqm` |
| Работа с БД | `shared/.../data/repository/DatabaseRepository.kt` |
| Сущности | `shared/.../domain/entity/` |
| Интерфейсы репозиториев | `shared/.../domain/repository/` |
| Инициализация shared | `shared/.../data/WishAppSdk.kt` |
| Навигация Android | `app/.../navigation/old/`, новый вид `app/.../navigation/` |
| Навигация iOS | `iosApp/WishApp/Navigation/` |
| DI Android | `app/.../di/` |
| DI shared/iOS | `shared/.../di/`, `shared/src/iosMain/.../di/` |
| Бэкап Android | `app/.../data/repository/backup/`, `app/.../domain/use_case/` |
| Бэкап iOS | `iosApp/WishApp/View/Backup/` |
| Аналитика Android | `app/.../domain/model/analytics/`, `app/.../data/repository/analytics/` |
| Аналитика iOS | `iosApp/WishApp/Domain/Analytics/` |
| Строки Android | `app/src/main/res/values*/strings.xml` |
| Строки iOS | `iosApp/WishApp/Resources/{en,ru}.lproj/Localizable.strings` |
| Тема Android | `app/.../ui/old/theme/WishAppTheme.kt`, новый вид `app/.../ui/theme/` |
| Переключение старого и нового вида | `app/.../domain/model/UiLook.kt`, `app/.../ui/AppRoot.kt` |
| Техдолг, идеи, просьбы пользователей (сюда же записывай новые находки) | `docs/BACKLOG.md` |
| Поведение экранов для редизайна | `docs/UX_NOTES.md` |
| Релиз: ветки, теги, проверки, раскатка | `docs/RELEASE.md` |
| Новые долгоживущие md-файлы | `docs/` (в корне только `README.md` и `AGENTS.md`) |

## Сборка и проверки

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :shared:allTests
```

Smoke-тест на эмуляторе (успешный сценарий по всем экранам),
удобно после обновления зависимостей. Стирает данные debug-приложения:

```bash
scripts/run_smoke_test.sh
```

Тот же smoke-тест на сборке `autotest` (как release, с R8; эмулятор API 26+),
перед релизом обязательно:

```bash
BUILD_TYPE=autotest scripts/run_smoke_test.sh
```

Проверка `shared` под iOS, без Xcode-проекта и симулятора. Обязательна после
изменений в `shared`. Swift-код `iosApp` она не проверяет:

```bash
./gradlew :shared:compileKotlinIosArm64 :shared:linkDebugFrameworkIosSimulatorArm64
```

`:shared:allTests` запускает тесты через iOS-симулятор: другого способа прогнать
`commonTest` сейчас нет, Android host-тесты в `shared` не включены.

Приложение iOS (разработка на паузе, собирать только по просьбе):

```bash
open iosApp/WishApp.xcodeproj
```

Перед сборкой Xcode сам запускает
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`.

CI в репозитории **нет** — проверяй вручную.

## Типовой цикл работы

1. Прочитай затронутые файлы и найди существующий паттерн.
2. Определи слой: `shared`, Android, iOS или всё сразу.
3. Внеси изменения; для БД — миграция и snapshot.
4. Обнови строки и analytics-события, если появился новый экран/действие.
5. Прогони сборку и тесты.
6. Не коммить изменения без явной просьбы.

## Важные различия и грабли

- **Идёт редизайн Android.** Прежний вид живёт в `ui/old` и `navigation/old` и не меняется,
  новые экраны пишутся в `ui` и `navigation` со своей темой `ui/theme`. Вид выбирает флаг
  `ui_look_mode` и выбор пользователя, в debug новый вид открывается из старых настроек.
- **iOS на паузе и отсутствует в App Store**; после изменений в `shared` проверяем
  только компиляцию модуля под iOS (правило 10).
- Изображения, сортировка списка и выбор темы есть **только на Android**.
- Бэкап: **Google Drive** на Android, **iCloud** на iOS; общего кода нет.
- На iOS Firebase фактически отключён — используется только Amplitude.
- Тестов мало: несколько юнит-тестов, `DatabaseRepositoryDeleteTest`, `SmokeTest`.
- Release на Android подписан тестовым keystore — это не production-подпись.
- Легаси: `iosApp/WishApp/ui/MainScreen/` не входит в Xcode-таргет.
- `WishDetailedViewModel` сразу создаёт пустое желание; при выходе пустая запись
  удаляется (`AppActivityViewModel.onWishScreenExit`).
- Изменение публичного API `shared` может сломать Swift-код в `iosApp`, проверка
  из Gradle этого не поймает. Это принятый риск, пока iOS на паузе.
