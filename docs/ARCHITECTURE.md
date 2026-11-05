# Архитектура WishApp («Хотелки»)

Полная карта проекта: модули, экраны, модель данных, навигация, DI, бэкапы,
аналитика, сборка и правила работы. Используй этот документ, когда нужен
глубокий контекст; краткая инструкция для агентов лежит в `AGENTS.md`.

---

## 1. Что это за проект

**WishApp / «Хотелки»** — мобильное приложение для ведения списка желаний:

- желания можно группировать тегами (метками);
- списком можно делиться текстом или PDF (Android);
- к желанию можно прикреплять ссылки, комментарий и изображения (изображения сейчас только Android);
- данные хранятся локально в SQLite;
- Android умеет делать бэкап в Google Drive, iOS — в iCloud;
- проект — **Kotlin Multiplatform**: общая бизнес-логика и БД в `shared`, UI отдельно на Android и iOS.

Публикации:

- Google Play: `ru.vitaliy.belyaev.wishapp`
- App Store: `id6450624836` (приложение сейчас отсутствует в App Store)
- Сайт: https://wishapp.info/

Текущие версии в репозитории:

- Android: `versionName = "1.8.2"`, `versionCode = 24`
- iOS: `MARKETING_VERSION = 1.1.0`, `CURRENT_PROJECT_VERSION = 7`

### 1.1 Статус платформ

- **Android** — активная разработка и поддержка.
- **iOS** — активная разработка и поддержка приостановлены; приложение сейчас
  отсутствует в App Store, дальнейшее развитие на паузе.
- При этом `shared` используется обеими платформами. Изменения в общем коде
  **не должны ломать iOS-сборку и работоспособность iOS-приложения**.
- Новые iOS-only фичи не приоритет: достаточно поддерживать текущее состояние и,
  когда это не требует много времени, проверять, что iOS собирается и не
  регрессирует.

---

## 2. Быстрая карта репозитория

```text
WishApp/
├── app/                  # Android-приложение (Compose, Hilt, Navigation3)
├── shared/               # Kotlin Multiplatform: БД, домен, репозитории
├── iosApp/               # iOS-приложение (SwiftUI, Combine)
├── docs/                 # Архитектура, бэклог, UX-заметки, скриншоты
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

Если нужно понять проект за 5 минут, прочитай:

1. `shared/src/commonMain/kotlin/ru/vitaliy/belyaev/wishapp/shared/data/WishAppSdk.kt` — точка входа в общую БД.
2. `shared/src/commonMain/kotlin/ru/vitaliy/belyaev/wishapp/shared/data/repository/DatabaseRepository.kt` — вся работа с БД.
3. `app/src/main/java/ru/vitaliy/belyaev/wishapp/ui/AppActivity.kt` — Android-точка входа и навигация.
4. `app/src/main/java/ru/vitaliy/belyaev/wishapp/navigation/Navigation.kt` — карта Android-экранов.
5. `iosApp/WishApp/WishApp.swift` — iOS-точка входа.
6. `iosApp/WishApp/View/Main/Components/MainContentView.swift` — карта iOS-экранов.

---

## 3. Технологический стек

### shared

- Kotlin `2.4.21`
- Kotlin Coroutines `1.11.0`
- kotlinx-datetime `0.8.0`
- SQLDelight `2.4.1`
- Koin `4.2.2` (используется на iOS и как общий DI-модуль)
- KMP-NativeCoroutines `1.0.6`
- UUID (`com.benasher44:uuid`)
- Napier (логирование)

### Android

- Android Gradle Plugin `9.4.0`
- JVM target `11`, `minSdk 24`, `targetSdk 37`, `compileSdk 37`
- Jetpack Compose + Material 3 (Material You, dynamic colors на Android 12+)
- Navigation 3 (`androidx.navigation3`) — актуальная навигация
- Hilt `2.60.1` + KSP
- DataStore Preferences
- Google Drive API (бэкап), Google Sign-In, Google Play Review
- Firebase Analytics / Crashlytics / Performance
- Amplitude
- Coil + Telephoto (изображения)
- Timber, LeakCanary (debug)
- Kotlin Serialization

### iOS

- SwiftUI, Combine
- iOS deployment target `16.0`, Swift 5
- SPM-зависимости (см. `iosApp/WishApp.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved`):
  - KMP-NativeCoroutines
  - Amplitude iOS
  - DeviceKit
  - SwiftUIFlowLayout
  - SwiftUIIntrospect
  - SwiftUISnackbar
- Общий Kotlin-фреймворк `shared` собирается Gradle-скриптом из Xcode
- Firebase на iOS фактически отключён (код закомментирован), аналитика идёт в Amplitude

---

## 4. Общая архитектура

```text
┌────────────────────────────── Android ──────────────────────────────┐
│ Compose Screen → ViewModel (Hilt) → Repository interface (shared)    │
│                                      ↓                              │
│                        DatabaseRepository (shared)                   │
│                                      ↓                              │
│                              SQLDelight → SQLite                    │
│                                                                      │
│ Дополнительно: DataStore, Google Drive, Firebase, Amplitude          │
└──────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────── iOS ─────────────────────────────────┐
│ SwiftUI View → ObservableObject ViewModel → KMP-NativeCoroutines     │
│                                      ↓                              │
│                     WishAppSdk (Koin) → DatabaseRepository           │
│                                      ↓                              │
│                              SQLDelight → SQLite                    │
│                                                                      │
│ Дополнительно: iCloud (backup), Amplitude                            │
└──────────────────────────────────────────────────────────────────────┘
```

Ключевая идея: **бизнес-логика и работа с БД живут в `shared`**. Android и iOS — это
тонкие UI-слои + платформенные возможности (бэкап, аналитика, шаринг, изображения).

---

## 5. Модули и их зоны ответственности

### 5.1 `shared` — общая логика

Корневой пакет: `ru.vitaliy.belyaev.wishapp.shared`

```text
shared/src/
├── commonMain/
│   ├── kotlin/.../shared/
│   │   ├── data/
│   │   │   ├── Config.kt                    # имя БД
│   │   │   ├── WishAppSdk.kt                # фасад доступа к БД + restore
│   │   │   ├── NapierProxyKt.kt             # debug-логирование
│   │   │   ├── coroutines/                  # expect DispatcherProvider
│   │   │   ├── database/                    # expect DatabaseDriverFactory
│   │   │   ├── mapper/                      # DTO → domain
│   │   │   └── repository/DatabaseRepository.kt
│   │   ├── domain/
│   │   │   ├── entity/                      # WishEntity, TagEntity, ImageEntity...
│   │   │   ├── repository/                  # интерфейсы репозиториев
│   │   │   ├── LinksAdapter.kt              # сериализация ссылок через "|"
│   │   │   └── ShareWishListTextGenerator.kt
│   │   ├── utils/                           # DateTimeExt, PrivacyPolicy, SampleDataGenerator
│   │   └── di/Koin.kt                       # initKoin + wishAppSdkModule
│   └── sqldelight/
│       ├── .../database/Wish.sq, Tag.sq, Image.sq, WishTagRelation.sq
│       ├── .../database/migrations/1.sqm, 2.sqm
│       └── databases/1.db, 2.db, 3.db       # snapshot-файлы для verifyMigrations
├── androidMain/                             # AndroidSqliteDriver, dispatcher, Koin platform module
├── iosMain/                                 # NativeSqliteDriver, dispatcher, Koin helper
└── commonTest/                              # LinksAdapterTest
```

Ответственность:

- схема БД и SQL-запросы;
- доменные сущности и мапперы;
- репозитории и реактивные Flow;
- общая генерация текста для «поделиться»;
- `WishAppSdk` — единая точка создания/доступа к БД;
- восстановление БД из бэкапа на уровне строк.

Чего здесь быть не должно:

- Android Context, Intent, Compose, Hilt;
- SwiftUI, Combine, UIKit;
- платформенные Google Drive / iCloud / Amplitude.

### 5.2 `app` — Android-приложение

Корневой пакет: `ru.vitaliy.belyaev.wishapp`

```text
app/src/main/java/ru/vitaliy/belyaev/wishapp/
├── WishApplication.kt          # @HiltAndroidApp, dynamic colors, Coil, Amplitude
├── ui/
│   ├── AppActivity.kt          # single activity, setContent, deep link ACTION_SEND
│   ├── AppActivityViewModel.kt # тема, in-app review, удаление пустых желаний
│   ├── core/                   # переиспользуемые Compose-компоненты
│   ├── screens/                # экраны (см. раздел 6)
│   ├── theme/                  # цвета, типографика, WishAppTheme
│   └── model/                  # UI-модели
├── navigation/                 # Navigation3: Routes, NavigationState, Navigator, Navigation
├── di/                         # Hilt-модули
├── data/
│   ├── repository/datastore/   # DataStoreRepository
│   ├── repository/backup/      # Google Drive + Google Sign-In
│   └── repository/analytics/   # Firebase + Amplitude + Log composite
├── domain/
│   ├── repository/             # AnalyticsRepository, BackupRepository, BackupAuthRepository
│   ├── use_case/               # сценарии бэкапа/восстановления
│   ├── model/                  # Theme, BackupInfo, ShareData, ImageData, AppFeedback...
│   └── model/analytics/        # AnalyticsEvent + события
└── utils/                      # Intent, PDF, Snackbar, TrackScreenShow, Context...
```

### 5.3 `iosApp` — iOS-приложение (разработка на паузе)

Приложение сейчас отсутствует в App Store; новые iOS-only фичи не приоритет.
Модуль всё равно важен как потребитель `shared`: не ломай его сборку и базовую
работоспособность.

```text
iosApp/WishApp/
├── WishApp.swift               # @main, AppDelegate, Koin, Amplitude
├── AppViewModel.swift          # глобальные действия: complete/delete wish, snackbar
├── Navigation/
│   ├── NavigationModel.swift   # персистентный NavigationPath в AppStorage
│   └── MainNavSegment.swift    # WishList / WishDetailed / UpdateWishTags
├── View/
│   ├── Main/                   # корневой экран: All, Completed, теги
│   ├── WishList/               # список желаний
│   ├── WishDetailed/           # карточка желания
│   ├── UdpateWishTags/         # выбор/создание тегов (опечатка в имени папки)
│   ├── Settings/               # настройки
│   ├── Backup/                 # iCloud backup/restore
│   └── Common/                 # WebView, Snackbar, FullscreenLoading
├── Domain/
│   ├── Analytics/              # события и отправка в Amplitude
│   └── Entity/                 # ошибки, UserDefaultsKeys, расширения WishEntity
├── Utils/                      # Bundle, Date, FileManager, NavigationLink, URL...
└── Resources/                  # Assets, en.lproj, ru.lproj
```

Важно:

- iOS получает общий код через **Koin**: `WishAppSdkDiHelper().wishAppSdk`.
- `KoinKt.doInitKoin()` вызывается в `AppDelegate`.
- Swift-обёртки Flow/Future создаются KMP-NativeCoroutines: `createPublisher`, `createFuture`.

---

## 6. Экраны Android

Навигация описана в `app/src/main/java/ru/vitaliy/belyaev/wishapp/navigation/`.

| Экран | Файлы | ViewModel | Что делает |
|---|---|---|---|
| Wish List (главный) | `ui/screens/wish_list/WishListScreen.kt` + `components/` | `WishListViewModel` | Список желаний, фильтры All / Completed / By Tag, сортировка, редактирование, удаление, перемещение, шаринг, переход к бэкапу |
| Wish Detailed | `ui/screens/wishdetailed/WishDetailedScreen.kt` + `components/` | `WishDetailedViewModel` | Создание/редактирование желания: title, comment, links, images, tags, complete/delete |
| Wish Tags | `ui/screens/wishtags/WishTagsScreen.kt` + `components/` | `WishTagsViewModel` | Привязка/создание тегов для конкретного желания |
| Edit Tags | `ui/screens/edittags/EditTagsScreen.kt` + `components/` | `EditTagsViewModel` | Переименование и удаление тегов |
| Wish Images Viewer | `ui/screens/wish_images/WishImagesViewerScreen.kt`, `WishImagesViewerRoute.kt` | `WishImagesViewerViewModel` | Полноэкранный просмотр изображений с zoom и удалением |
| Settings | `ui/screens/settings/SettingsScreen.kt` + `components/` | `SettingsViewModel` | Тема, Backup, Rate app, Share app, About |
| Backup & Restore | `ui/screens/backup/BackupScreen.kt` + `components/` | `BackupViewModel` | Google Sign-In, проверка бэкапа, создание, восстановление, управление аккаунтом |
| About App | `ui/screens/aboutapp/AboutAppScreen.kt` | `AboutAppViewModel` | Версия, feedback по email, OSS-лицензии, privacy policy |
| Privacy Policy | `ui/screens/aboutapp/privacypolicy/PrivacyPolicyScreen.kt` | `PrivacyPolicyViewModel` | WebView с политикой конфиденциальности |

Особенности Wish List:

- фильтр `WishesFilter`: `All`, `Completed`, `ByTag`;
- сортировка `WishSortMode`: `Default`, `CreatedDateNewest`, `CreatedDateOldest`, `TitleAZ`, `TitleZA`;
  - `TitleAZ/TitleZA` сортируются только в Kotlin по `lowercase()`,
    потому что SQLite `COLLATE NOCASE` не учитывает регистр кириллицы;
- режим множественного выбора по long press + удаление выбранных;
- перемещение вверх/вниз доступно только в `Default`-сортировке;
- шаринг: текст (`ShareWishListTextGenerator`) или PDF (`ShareWishListPdfGenerator`);
- нижняя панель: tags sheet, sort sheet, share sheet, add wish;
- при пустом списке показывается заглушка + вход в Backup.

Особенности Wish Detailed:

- при создании нового желания запись сразу вставляется в БД (пустая), а при выходе
  удаляется, если осталась пустой (`AppActivityViewModel.onWishScreenExit`);
- title/comment сохраняются по мере ввода;
- ссылки хранятся в одном поле и сериализуются через `|` (`LinksAdapter`);
- изображения выбираются через `PickMultipleVisualMedia`, максимум 5, сжимаются до
  большей стороны 1280 px и JPEG quality 75;
- теги открываются отдельным экраном;
- выполненное желание показывается зачёркнутым.

---

## 7. Экраны iOS

> iOS сейчас на паузе и отсутствует в App Store. Таблица ниже — справочная:
> она нужна, чтобы при изменениях `shared` понимать, что может сломаться.

| Экран | Файлы | ViewModel | Что делает |
|---|---|---|---|
| Main | `View/Main/MainView.swift`, `Components/MainContentView.swift` | `MainViewModel` | Корневой список: All, Completed, теги с количеством; rename/delete тегов; settings; add wish |
| Wish List | `View/WishList/WishListView.swift`, `Components/WishListContentView.swift` | `WishListViewModel` | Список по режиму All / Completed / ByTag, шаринг текстом, редактирование, удаление, reorder |
| Wish Detailed | `View/WishDetailed/WishDetailedView.swift`, `Components/LinkView.swift` | `WishDetailedViewModel` | Title, comment, links, tags, complete/delete |
| Update Wish Tags | `View/UdpateWishTags/UpdateWishTagsView.swift` | `UpdateWishTagsViewModel` | Выбор/создание тегов для желания |
| Settings | `View/Settings/SettingsView.swift` | — (локальное состояние) | Support email, rate, privacy, backup, app info |
| Backup & Restore | `View/Backup/BackupAndRestoreView.swift` | `BackupAndRestoreViewModel` | Бэкап/восстановление через iCloud |
| Common | `View/Common/` | — | WebView, snackbar, fullscreen loader |

Особенности:

- iOS-навигация — `NavigationStack` + `NavigationPath`, путь сохраняется в `@AppStorage("navigationData")`
  и восстанавливается после перезапуска.
- `SettingsView` открывается как sheet, а не как отдельный элемент `mainPath`.
- В iOS **нет** выбора темы, сортировки списка и работы с изображениями.
- Теги в Main можно переименовывать/удалять прямо в списке.

---

## 8. Модель данных и база

### 8.1 Доменные сущности

`shared/src/commonMain/kotlin/.../domain/entity/`

- `WishEntity` — `id`, `title`, `link`, `links`, `comment`, `isCompleted`,
  `createdTimestamp`, `updatedTimestamp`, `position`, `tags`, `images`.
- `TagEntity` — `id`, `title`.
- `ImageEntity` — `id`, `wishId`, `rawData: ByteArray`.
- `TagWithWishCount` — тег + количество текущих (невыполненных) желаний.
- `WishSortMode` — режим сортировки.

### 8.2 SQLDelight-схема

Файлы: `shared/src/commonMain/sqldelight/ru/vitaliy/belyaev/wishapp/shared/data/database/`

- `Wish.sq` — таблица `Wish`
- `Tag.sq` — таблица `Tag`
- `Image.sq` — таблица `Image`
- `WishTagRelation.sq` — связь many-to-many `Wish ↔ Tag`

Ключевые правила:

- `Wish.link` — **не список**, а строка, где несколько ссылок разделены `|`.
  В доменную модель они разворачиваются через `LinksAdapter.getLinksListFromString`.
  Никогда не склеивай ссылки вручную — используй `LinksAdapter`.
- `Wish.position`:
  - обычные желания имеют позицию `>= 0`;
  - выполненные — `-1`;
  - сортировка по умолчанию — `ORDER BY position DESC`;
  - при удалении/завершении позиции пересчитываются;
  - новые желания получают `position = count(valid positions)`.
- `createdTimestamp` / `updatedTimestamp` — epoch millis (`kotlin.time.Clock`).
- `Image.rawData` — BLOB с уже сжатым JPEG.
- Имя БД: `Config.DATABASE_NAME = "ru_vitaliy_belyaev_wishapp.db"`.

### 8.3 Миграции

- Миграции лежат в `shared/src/commonMain/sqldelight/.../database/migrations/*.sqm`.
- Snapshot-файлы: `shared/src/commonMain/sqldelight/databases/1.db`, `2.db`, `3.db`.
- В `shared/build.gradle.kts` включён `verifyMigrations = true`.
- `DatabaseMigrator.updateWishPositionsAfterMigrationFrom1To2` — пример
  data-миграции, вызывается через `AfterVersion(1)` на Android и iOS.

**Если меняешь схему:**

1. Добавь/измени `.sq`.
2. Добавь `.sqm` с `ALTER TABLE ...`.
3. Обнови snapshot (`databases/N.db`) через Gradle/SQLDelight.
4. Пробегись по мапперам (`WishMapper`, `TagMapper`, `ImageMapper`) и всем запросам.
5. Проверь миграцию на обеих платформах.

### 8.4 Слои данных

- Интерфейсы: `domain/repository/WishesRepository`, `TagsRepository`,
  `WishTagRelationRepository`, `ImagesRepository`.
- Реализация одна: `data/repository/DatabaseRepository` — реализует все четыре.
- Мапперы: `data/mapper/WishMapper`, `TagMapper`, `ImageMapper`.
- Все публичные методы помечены `@NativeCoroutines` — без этой аннотации Swift
  не получит удобные `createPublisher`/`createFuture`.

---

## 9. Навигация

### Android

Используется **Navigation 3**:

- `navigation/Routes.kt` — `@Serializable` `NavKey`-классы:
  `MainRoute`, `WishDetailedRoute`, `SettingsRoute`, `BackupAndRestoreRoute`,
  `AboutAppRoute`, `PrivacyPolicyRoute`, `EditTagRoute`, `WishTagsRoute`,
  `WishImagesViewerRoute`.
- `navigation/Navigation.kt` — `NavDisplay` + `entryProvider`, здесь собираются экраны.
- `navigation/NavigationState.kt` — back stack-и, сохранение состояния.
- `navigation/Navigator.kt` — `navigate()` / `goBack()`.

Правило: для нового экрана добавь `NavKey`, `entry<>` в `Navigation.kt` и
навигационный callback в нужный экран.

Глубокие ссылки:

- `AppActivity` ловит `Intent.ACTION_SEND` с `text/plain`;
- валидирует URL и открывает `WishDetailedRoute(wishLink = ...)`;
- `onNewIntent` обрабатывает повторный шаринг без перезапуска.

### iOS

- `NavigationModel` хранит `mainPath`, `settingsPath`, `isSettingPresented`.
- `MainNavSegment`: `WishList`, `WishDetailed`, `UpdateWishTags`.
- `NavigationStack(path:)` + `.navigationDestination(for: MainNavSegment.self)`.
- Путь сериализуется в `AppStorage` и восстанавливается после запуска.

---

## 10. Состояние, ViewModel и потоки

### Android

- Базовый класс: `ui/core/viewmodel/BaseViewModel.kt`.
- Все корутины запускаются через `launchSafe`:
  - в debug исключение пробрасывается;
  - в release уходит в Crashlytics + Timber.
- Состояние — `StateFlow` / `SharedFlow`; UI собирает через `collectAsStateWithLifecycle`.
- `ViewModel` обычно подписывается на Flow репозитория в `init`.
- Экраны получают ViewModel через `hiltViewModel()`.
- `WishDetailedViewModel` выполняет записи строго по очереди, а `prepareForExit()`
  ждёт их завершения перед очисткой пустого желания. Удаление наблюдаемого желания
  не приводит к падению: `observeWishById` просто перестаёт отдавать значения.

### iOS

- ViewModel — `@MainActor final class ...: ObservableObject`.
- Flow из Kotlin превращается в Combine-publisher через `createPublisher`.
- suspend-функции — через `createFuture`.
- Тяжёлые операции уходят в `DispatchQueue.global()`, UI-обновления — на main.
- Подписки хранятся в `subscriptions: [AnyCancellable]`.

---

## 11. Dependency Injection

### Android — Hilt

Модули в `app/src/main/java/ru/vitaliy/belyaev/wishapp/di/`:

| Модуль | Что даёт |
|---|---|
| `WishAppSdkModule` | `WishAppSdk` (создаёт `DatabaseDriverFactory`) |
| `DatabaseRepositoryModule` | 4 интерфейса репозиториев из `wishAppSdk.getDatabaseRepository()` |
| `DataStoreModule` | `DataStore<Preferences>` |
| `BackupRepositoryProvideModule` | `BackupRepository`, `BackupAuthRepository` (Google Drive) |
| `AnalyticsModule` | `AnalyticsRepositoryComposite` (Firebase + Amplitude [+ Log в debug]) |
| `NetworkModule` | `OkHttpClient` с кэшем и логгером |
| `DispatcherProviderModule` | `DispatcherProvider` |
| `UseCaseProvideModule` | пустой/закомментированный; use cases создаются через `@Inject` |

Как добавить зависимость:

- если это общий репозиторий — добавь интерфейс в `shared/domain/repository`,
  реализацию в `DatabaseRepository`, провайдер в `DatabaseRepositoryModule`;
- если Android-only — создай класс с `@Inject constructor` или Hilt-модуль.

### iOS — Koin

- `shared/commonMain/di/Koin.kt` — `initKoin()` и `wishAppSdkModule`.
- `shared/iosMain/di/WishAppSdkDiHelper.kt` — `WishAppSdk by inject()`.
- Вызов: `KoinKt.doInitKoin()` в `AppDelegate`.

**Важно:** Android **не вызывает** `initKoin()`. На Android `WishAppSdk` создаётся
Hilt-модулем. Не пытайся «починить» это, не разобравшись: это осознанная схема.

---

## 12. Бэкап и восстановление

### Android — Google Drive

Файлы:

- `data/repository/backup/GoogleBackupAuthRepository.kt`
- `data/repository/backup/GoogleDriveBackupRepository.kt`
- `domain/use_case/CreateBackupUseCase.kt`
- `domain/use_case/RestoreBackupUseCase.kt`
- `domain/use_case/GetExistingBackupInfoUseCase.kt`
- `domain/use_case/NeedToShowRestoreUseCase.kt`
- `ui/screens/backup/BackupViewModel.kt`

Как работает:

- OAuth scope: `DriveScopes.DRIVE_APPDATA`.
- Файл бэкапа: `wishapp.backup`, MIME `application/octet-stream`.
- Хранится в приватной папке `appDataFolder` (не видна пользователю в Drive).
- В `appProperties` пишутся версия приложения и устройство.
- Создание: загружает файл БД (`context.getDatabasePath(WishAppSdk.databaseName)`).
- Восстановление: скачивает файл, удаляет текущую БД, записывает новую, затем
  приложение перезапускается через 3 секунды.
- Флаги в DataStore:
  - `KEY_IS_BACKUP_WAS_CREATED`
  - `KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED`
  - используются в `NeedToShowRestoreUseCase`, чтобы решить, показывать ли «Restore»
    или «Force update app data».
- `android:allowBackup="false"` — системный Android-бэкап отключён, используется
  только ручной Drive-бэкап.
- DataStore (тема, счётчики) в бэкап **не входит** — переносится только БД.

### iOS — iCloud

Файлы:

- `View/Backup/DBICloudFilesManager.swift`
- `View/Backup/DBLocalFilesManager.swift`
- `View/Backup/BackupAndRestoreViewModel.swift`
- `WishApp/entitlements`: `iCloud.com.vitaliy.belyaev.WishApp`, `CloudDocuments`

Как работает:

- Копируются три файла SQLite: `backup.db`, `backup.db-wal`, `backup.db-shm`
  в iCloud-контейнер.
- Рядом пишется `backup_meta_info.json` (timestamp + имя устройства).
- Восстановление: копирует файлы из iCloud во временную папку, затем
  `WishAppSdk.copyContentFromBackupDatabase(...)` очищает текущие таблицы и
  переносит строки из backup-БД.
- `DBLocalFilesManager` — локальный (не iCloud) вариант, сейчас не основной.

Не путай: **Android — Google Drive, iOS — iCloud**. Общего кода бэкапа нет.

---

## 13. Аналитика

### Android

- Интерфейс: `domain/repository/AnalyticsRepository.kt`
- Событие: `domain/model/analytics/AnalyticsEvent.kt` (`name`, `params`)
- Реализации: Firebase, Amplitude, Log (debug)
- Композит: `AnalyticsRepositoryComposite` рассылает событие во все реализации.
- Показ экрана: `utils/TrackScreenShowExt.kt` + `viewModel.trackScreenShow()`.

Чтобы добавить событие:

1. Создай `object`/`data class` в `domain/model/analytics/...` с уникальным `name`
   и `params`.
2. Вызови `analyticsRepository.trackEvent(MyEvent(...))` из ViewModel.
3. Для screen show используй существующий паттерн `trackScreenShow`.

### iOS

- `Domain/Analytics/Analytics.swift` — `WishAppAnalytics.logEvent(...)`.
- События — классы в `Domain/Analytics/*.swift`.
- Отправка только в Amplitude; Firebase-код закомментирован.

Имена событий на платформах могут отличаться (например, Android `Wish List Screen - Show`,
iOS `Wish List Screen - Show` с другим набором параметров). При добавлении нового
события по возможности синхронизируй имя и параметры.

---

## 14. Локализация, темы, ресурсы

### Android

- Языки: `values` (английский), `values-ru`, `values-uk`, `values-de`, `values-es`,
  `values-fr`, `values-pt`.
- Строки: `app/src/main/res/values*/strings.xml`.
- Названия приложения: `app_name.xml`.
- Тема: `ui/theme/WishAppTheme.kt`.
  - `Theme.SYSTEM / DARK / LIGHT`;
  - на Android 12+ используются dynamic colors (Material You);
  - выбранная тема хранится в DataStore.
- Для изображений экрана просмотра принудительно включается тёмная тема
  (`NavigationState.currentRoute is WishImagesViewerRoute` → `Theme.DARK`).

### iOS

- Локализации: только `en.lproj` и `ru.lproj`
  (`Localizable.strings`, `Localizable.stringsdict`, `InfoPlist.strings`).
- Тема — системная, отдельной настройки нет.
- Цвета/иконки — `Resources/Assets.xcassets`.

При добавлении строки добавляй её во все существующие локали или как минимум
в `values/strings.xml` (Android) и `en.lproj` + `ru.lproj` (iOS).

---

## 15. Сборка, запуск, проверки

### Требования

- JDK 17 для запуска Gradle (JVM target проекта — 11).
- Android Studio с поддержкой AGP 9.4 / compileSdk 37.
- Xcode с iOS 16+ SDK.
- `local.properties` с `sdk.dir` (не коммитится).
- `apikey.properties` в корне с `AMPLITUDE_API_KEY` (не коммитится).
- `app/google-services.json` и `iosApp/WishApp/GoogleService-Info.plist` для Firebase.
- `iosApp/WishApp/AppKeys.plist` с `AMPLITUDE_API_KEY` для iOS.

### Android

```bash
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:assembleRelease        # release (подписан тестовым keystore!)
./gradlew :app:testDebugUnitTest      # unit-тесты app
./gradlew :app:lintDebug              # Android lint
./gradlew :shared:allTests            # тесты shared (KMP)
```

Полезно:

```bash
./gradlew :shared:tasks --all | grep -i test   # точные имена test-задач
```

### iOS

Активная разработка iOS на паузе, приложение отсутствует в App Store. Сборка iOS
здесь нужна прежде всего как smoke-check, чтобы изменения в `shared` не сломали
общий код.

```bash
open iosApp/WishApp.xcodeproj
```

- Зависимости — через SPM, `Pods/` в репозиторий не входит (игнорируется).
- Перед сборкой Xcode сам запускает Run Script:
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode`.
- Если фреймворк не собирается, проверь, что Gradle-задача проходит отдельно:

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

### CI

В репозитории **нет** CI-конфигурации (`.github` отсутствует). Все проверки нужно
запускать вручную. Не утверждай, что «CI пройдёт» — его нет.

---

## 16. Правила для изменений

1. **Сначала читай, потом меняй.** Особенно `DatabaseRepository`, `.sq`, `WishEntity`.
2. **Бизнес-логику — в `shared`.** Не дублируй SQL/правила в Android и iOS.
3. **Помни про iOS, даже если он на паузе.** Если меняешь публичный API `shared`,
   проверь, что вызовы в `iosApp` не сломаны, а для Swift-доступа обязательна
   `@NativeCoroutines`. Новые iOS-only фичи не приоритет.
4. **Меняешь схему БД — делай миграцию.** Без `.sqm` и обновлённого snapshot
   `verifyMigrations` упадёт.
5. **Не редактируй сгенерированное:** `build/`, `shared/build/`, `Pods/`.
6. **Не коммить секреты:** `local.properties`, `apikey.properties`, ключи keystore.
7. **Транзакции для нескольких записей.** Примеры — `DatabaseRepository`.
8. **Ссылки только через `LinksAdapter`.**
9. **Не используй старую навигацию.** Только Navigation3 и `NavKey`.
10. **Не ломай iOS при изменениях `shared`.** Полную проверку iOS делай, когда
    это не требует много времени; как минимум убедись, что общий код и
    iOS-фреймворк компилируются.
11. **Не добавляй лишние комментарии** без необходимости; существующий код местами
    содержит русские комментарии — это нормально.
12. **Соблюдай стиль:** `kotlin.code.style=official`, пакеты
    `ru.vitaliy.belyaev.wishapp.*`.

---

## 17. Частые задачи

### Добавить Android-экран

1. Создай пакет в `ui/screens/<name>/`.
2. Добавь `<Name>Screen.kt` и `<Name>ViewModel.kt` (наследник `BaseViewModel`).
3. Добавь `@Serializable data object/class <Name>Route : NavKey` в `navigation/Routes.kt`.
4. Зарегистрируй `entry<<Name>Route>` в `navigation/Navigation.kt`.
5. Добавь строки в `res/values*/strings.xml`.
6. Если нужен экран-показ — добавь analytics-событие и `trackScreenShow`.

### Добавить запрос/поле в БД

1. Измени `.sq` в `shared/src/commonMain/sqldelight/...`.
2. Добавь `.sqm` миграцию (если меняется схема).
3. Обнови snapshot-файл в `databases/`.
4. Обнови `DatabaseRepository`, мапперы и доменную модель.
5. Проверь `WishMapper`, `TagMapper`, `ImageMapper`.
6. Прогони `./gradlew :shared:allTests` и сборку `app`.

### Добавить метод в репозиторий

1. Добавь в интерфейс `domain/repository/*Repository.kt`.
2. Реализуй в `data/repository/DatabaseRepository.kt`.
3. Поставь `@NativeCoroutines` на публичный метод.
4. Проверь, что Android-вызовы и iOS-вызовы компилируются.

### Добавить iOS-экран

1. Создай `View/<Name>/<Name>View.swift` и `...ViewModel.swift`.
2. Если экран участвует в основной навигации — добавь кейс в `MainNavSegment`.
3. Добавь `.navigationDestination` в `MainContentView.swift` или нужный родитель.
4. Если нужен доступ к БД — используй `WishAppSdkDiHelper().wishAppSdk`.
5. Добавь строки в `en.lproj` и `ru.lproj`.

### Добавить analytics-событие

- Android: класс события в `domain/model/analytics/`, вызов из ViewModel.
- iOS: класс события в `Domain/Analytics/`, вызов `WishAppAnalytics.logEvent`.
- Проверь, что `name` и `params` не ломают существующие дашборды.

### Добавить строку

- Android: `app/src/main/res/values/strings.xml` + все существующие `values-*`.
- iOS: `Resources/en.lproj/Localizable.strings` + `ru.lproj`.
- Для строк с форматированием используй `strings.xml` и `stringResource(id, ...)`.

---

## 18. Известные особенности и техдолг

- **iOS на паузе:** активная разработка и поддержка остановлены, приложение
  отсутствует в App Store. Изменения в `shared` всё равно не должны ломать
  iOS-сборку и оставлять приложение нерабочим.
- **Разный функционал платформ:**
  - изображения (добавление/просмотр/удаление) — только Android;
  - сортировка списка — только Android;
  - выбор темы — только Android;
  - бэкап — Google Drive на Android, iCloud на iOS.
- **Мёртвый/легаси-код:**
  - старый `iosApp/WishApp/ui/MainScreen/` (не входит в Xcode-таргет);
  - закомментированные участки в `UseCaseProvideModule`, Firebase на iOS.
- **Две DI-системы:** Hilt на Android, Koin в `shared`/iOS. Android не вызывает Koin.
- **`Pods/` не в git** — iOS-зависимости через SPM.
- **Тестов мало:** `shared` — `LinksAdapterTest`, `app` — `BytesSizeFormatterTest`,
  `WishDetailedViewModelTest`, `DatabaseRepositoryDeleteTest` и `SmokeTest`. Не полагайся на тесты как на полное покрытие.
- **Release-подпись:** в `app/build.gradle.kts` для release используется тестовый
  keystore (`test_keystore`, пароль `notasecret`). Это не production-подпись.
- **Секреты:** `apikey.properties`, `local.properties` и keystore не коммить.
  `AppKeys.plist`, `google-services.json` и `GoogleService-Info.plist` уже лежат в
  репозитории — не добавляй туда новые секреты и не свети значения в логах.
- **iOS review flow** срабатывает каждые 7 положительных действий
  (`NeedToRequestReviewUseCase`), Android — сложнее: учитывает счётчик показов
  (`AppActivityViewModel`).
- **Android in-app review** запускается через Google Play Review API.
- **`WishDetailedViewModel`** создаёт пустое желание сразу; не забывай про очистку
  пустых записей при выходе.
- **Snackbar на главном экране** приходит из `AppActivityViewModel.showSnackOnMainFlow`.
- **PDF-файлы шаринга** лежат в `filesDir/wishlist_pdf` и очищаются при старте
  Activity без сохранённого состояния.
- **Coil:** disk cache отключён, memory cache включён.

---

## 19. Шпаргалка «где что искать»

| Что нужно | Куда смотреть |
|---|---|
| Точка входа Android | `app/src/main/java/ru/vitaliy/belyaev/wishapp/ui/AppActivity.kt` |
| Application Android | `app/src/main/java/ru/vitaliy/belyaev/wishapp/WishApplication.kt` |
| Точка входа iOS | `iosApp/WishApp/WishApp.swift` |
| Навигация Android | `app/.../navigation/Navigation.kt`, `Routes.kt`, `Navigator.kt` |
| Навигация iOS | `iosApp/WishApp/Navigation/NavigationModel.swift` |
| Главный экран Android | `app/.../ui/screens/wish_list/WishListScreen.kt` |
| Главный экран iOS | `iosApp/WishApp/View/Main/Components/MainContentView.swift` |
| Карточка желания Android | `app/.../ui/screens/wishdetailed/WishDetailedScreen.kt` |
| Карточка желания iOS | `iosApp/WishApp/View/WishDetailed/WishDetailedView.swift` |
| Схема БД | `shared/src/commonMain/sqldelight/.../*.sq` |
| Миграции | `shared/src/commonMain/sqldelight/.../migrations/*.sqm` |
| Работа с БД | `shared/.../data/repository/DatabaseRepository.kt` |
| Сущности | `shared/.../domain/entity/` |
| Интерфейсы репозиториев | `shared/.../domain/repository/` |
| Инициализация shared | `shared/.../data/WishAppSdk.kt` |
| DI Android | `app/.../di/` |
| DI iOS/shared | `shared/.../di/Koin.kt`, `shared/src/iosMain/.../di/` |
| Бэкап Android | `app/.../data/repository/backup/`, `app/.../domain/use_case/` |
| Бэкап iOS | `iosApp/WishApp/View/Backup/` |
| Аналитика Android | `app/.../domain/model/analytics/`, `app/.../data/repository/analytics/` |
| Аналитика iOS | `iosApp/WishApp/Domain/Analytics/` |
| Тема Android | `app/.../ui/theme/WishAppTheme.kt` |
| Строки Android | `app/src/main/res/values*/strings.xml` |
| Строки iOS | `iosApp/WishApp/Resources/{en,ru}.lproj/Localizable.strings` |
| Версии зависимостей | `gradle/libs.versions.toml` |
| Настройки сборки | `app/build.gradle.kts`, `shared/build.gradle.kts`, `settings.gradle.kts` |

---

## 20. Мини-чеклист перед завершением задачи

- [ ] Изменения не дублируют логику между Android и iOS.
- [ ] Публичные методы `shared` помечены `@NativeCoroutines`, если нужны в Swift.
- [ ] При изменении схемы БД есть миграция и обновлён snapshot.
- [ ] Строки добавлены во все нужные локали.
- [ ] События аналитики добавлены, если появилось новое действие/экран.
- [ ] Сборка Android проходит: `./gradlew :app:assembleDebug`.
- [ ] Тесты проходят: `./gradlew :app:testDebugUnitTest` и `./gradlew :shared:allTests`.
- [ ] Если менялся `shared` или `iosApp` — по возможности проверено, что
  iOS-сборка не сломана (полная проверка iOS не обязательна, но рабочее
  состояние нужно сохранять).
- [ ] Не закоммичены секреты, keystore, `local.properties`, `apikey.properties`.

---

При изменении архитектуры, схемы БД, навигации или набора экранов — обнови этот
файл и при необходимости краткую инструкцию в `AGENTS.md`.
