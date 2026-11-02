# Ревью ветки `feature/ai-experiments`

> Рабочий документ по текущей ветке: что изменено и что осталось доделать/исправить.
> Не является частью архитектурной документации проекта.

## Контекст

- Ветка: `feature/ai-experiments`
- HEAD: `adee588` (`Add architecture docs and AGENTS.md`)
- Ветки `develop` в репозитории нет — сравнение сделано с `dev` (`cbe51bd`).
- `origin/feature/ai-experiments` пока указывает на `b0a80fd`; docs-коммит `adee588` локальный.
- Изменения: 5 коммитов, 33 файла, `+1652 / −91`.

Коммиты:

```text
6caa21c Add nav3 libs
f997e40 Remove old nav controller
0a4c341 Add sorting feature
b0a80fd Fix wish detailed crash
adee588 Add architecture docs and AGENTS.md
```

Статус: **не готово к merge** — блокеры 1–5 и P2 закрыты; остаются два P3, iOS-плагин и ручная проверка.

---

## Что изменено

### 1. Переход на Navigation3

- Добавлены зависимости `navigation3-runtime`, `navigation3-ui`,
  `lifecycle-viewmodel-navigation3`.
- Добавлены `NavigationState`, `Navigator` и `@Serializable NavKey`-маршруты
  (`MainRoute`, `WishDetailedRoute`, `SettingsRoute` и т.д.).
- Новые route переименованы без суффикса `2`; старые строковые route удалены.
- `AppActivity` и `Navigation.kt` переведены с `NavHostController` на `NavDisplay`.
- `WishDetailedViewModel`, `WishTagsViewModel` и `WishImagesViewerViewModel`
  получают аргументы через `@AssistedInject`, а не через `SavedStateHandle`
  или `initialize(...)`.
- Для Image Viewer тема определяется по `NavigationState.currentRoute`, а не по
  `topLevelRoute`.
- Share-link обрабатывается в `LaunchedEffect` и перед открытием `WishDetailed`
  возвращает back stack к `MainRoute`.

### 2. Сортировка списка

- В `shared` добавлен `WishSortMode`:
  `Default`, `CreatedDateNewest`, `CreatedDateOldest`, `TitleAZ`, `TitleZA`.
- Добавлены SQL-запросы сортировки по дате в `Wish.sq` и `WishTagRelation.sq`.
  Миграция не требуется: схема БД не менялась.
- `TitleAZ/TitleZA` сортируются только в Kotlin по `title.lowercase()`,
  потому что SQLite `COLLATE NOCASE` не учитывает регистр кириллицы.
- Для iOS сохранены старые методы без `sortMode`, сортировка вынесена в
  отдельные `*Sorted`-методы.
- На Android добавлены:
  - `SortBottomSheetContent`;
  - иконка сортировки в `WishListBottomBar`;
  - `sortMode` в `MainScreenState`;
  - блокировка reorder при нестандартной сортировке;
  - строки во всех локалях.

### 3. Фикс WishDetailed

- Аргументы экрана передаются явно из `WishDetailedRoute`.
- `AppActivityViewModel` получается через `LocalActivity`.
- Debug-логи `RTRT` удалены.
- Новое желание получает id синхронно, до запуска `insertWish`.
- `currentWishId` сохраняется в `SavedStateHandle` и восстанавливается после
  смерти процесса без создания второго пустого желания.
- Записи экрана идут строго по очереди. Выход ждёт их завершения и использует
  `getWishByIdOrNull`, поэтому быстрый back больше не падает на пустом id.
- `viewModelScope.cancel()` при выходе убран: `observeWishById` использует
  `mapToOneOrNull` + `filterNotNull`, поэтому удаление желания не роняет наблюдателя.
- `deleteWishesByIds` идемпотентен и пропускает уже удалённые желания.
- Потеря фото при выходе во время их обработки принята как допустимая.
- Повторный back защищён активным `exitJob`, а не необратимым флагом.
- Добавлены unit-тесты `WishDetailedViewModelTest` на id, восстановление,
  preselected tag и порядок выхода.

### 4. Документация

- Добавлены `AGENTS.md` и `docs/ARCHITECTURE.md`.

---

## Блокеры (P0/P1)

### [x] 1. Краш просмотрщика изображений

**Статус:** исправлено через `@AssistedInject`; нужна ручная проверка.

**Где:** `WishImagesViewerViewModel.kt:24,29`, `Navigation.kt:118`.

**Проблема:**

- ViewModel читает `wishId` и `wishImageIndex` из `SavedStateHandle`
  через `requireNotNull`.
- `Navigation.kt` создаёт entry без передачи `WishImagesViewerRoute`.
- Navigation3 не прокидывает аргументы `NavKey` в `SavedStateHandle` автоматически
  (это видно по `ViewModelStoreNavEntryDecorator`).

**Итог:** открытие изображения, скорее всего, падает с `IllegalArgumentException`.

**Сделано:**

- `route.wishId` и `route.wishImageIndex` передаются в
  `WishImagesViewerScreenRoute`.
- ViewModel получает `wishId` через `@AssistedInject`.
- `SavedStateHandle` и `requireNotNull` убраны.

**Проверка:** открыть желание с изображением → тап по изображению → просмотрщик
открывается без краша.

---

### [x] 2. Экран тегов не работает

**Статус:** исправлено через `@AssistedInject`; нужна ручная проверка.

**Где:** `WishTagsViewModel.kt:29`, `Navigation.kt:108`.

**Проблема:**

- `WishTagsViewModel` получает `wishId` из `SavedStateHandle`.
- В Navigation3 аргумента там нет, поэтому `wishId` пустой.
- `Navigation.kt` игнорирует `WishTagsRoute.wishId`.

**Итог:** список тегов пустой; новые связи могут записываться с пустым `wishId`.

**Сделано:**

- `route.wishId` передаётся в `WishTagsScreen`.
- ViewModel получает `wishId` через `@AssistedInject`.
- Зависимость от `SavedStateHandle` для аргументов навигации убрана.

**Проверка:** открыть желание → теги → выбрать/создать тег → тег привязывается
к нужному желанию.

---

### [x] 3. Поломан iOS-контракт `shared`

**Статус:** исправлено. Старые `observeAllWishes(isCompleted:)` и
`observeWishesByTag(tagId:)` восстановлены, сортировка вынесена в новые
`observeAllWishesSorted` / `observeWishesByTagSorted`. Swift-вызовы не менялись;
KSP снова генерирует старые сигнатуры.

**Где:** `WishesRepository.kt:53,65`, `DatabaseRepository.kt:251,327`,
`iosApp/WishApp/View/WishList/WishListViewModel.swift:101,103,105,107`.

**Проблема:**

- В `observeAllWishes` и `observeWishesByTag` добавлен обязательный `sortMode`.
- Default-аргументы Kotlin не экспортируются в Swift.
- Сгенерированный KSP-код
  (`shared/build/generated/ksp/.../WishesRepositoryNative.kt`)
  содержит `sortMode` как обязательный параметр.
- Текущие Swift-вызовы передают только `isCompleted` / `tagId`.

**Итог:** после починки iOS-сборки Swift-код не скомпилируется.

**Сделано:**

- Старые Swift-сигнатуры оставлены без `sortMode`.
- Для сортировки добавлены отдельные `*Sorted`-методы.
- Android использует `*Sorted`-методы.
- KSP генерирует и старые, и новые native-методы.

**Проверка:** iOS-фреймворк собирается, `iosApp` компилируется.

---

### [x] 4. Не работает принудительный тёмный режим в просмотрщике изображений

**Статус:** исправлено. В `NavigationState` добавлен `currentRoute`
(`backStacks[topLevelRoute]?.lastOrNull()`), `AppActivity` проверяет его вместо
`topLevelRoute`. Нужна ручная проверка темы.

**Где:** `AppActivity.kt:127`.

**Проблема:**

- Проверка: `navigationState.topLevelRoute is WishImagesViewerRoute`.
- `topLevelRoutes` содержит только `MainRoute`.
- `Navigator.navigate()` добавляет экраны в back stack, не меняя `topLevelRoute`.
- Условие всегда `false`.

**Итог:** просмотрщик изображений больше не принудительно тёмный.

**Сделано:**

- `NavigationState.currentRoute` смотрит на верх активного back stack.
- `AppActivity` вычисляет `Theme.DARK` напрямую из `currentRoute`.

---

### [x] 5. Некорректная обработка share-ссылки

**Статус:** исправлено. Навигация вынесена из композиции в `LaunchedEffect`,
pop-to-root проверяет `currentRoute`. Нужна ручная проверка сценария.

**Где:** `AppActivity.kt:138-141,147`.

**Проблема:**

- Условие `navigationState.topLevelRoute != MainRoute` всегда `false`.
- При шаринге с экрана настроек/детали новый `WishDetailed` пушится сверху,
  а не возвращает к корню.
- Навигация при старте выполняется прямо во время композиции.

**Сделано:**

- Первичный share-link обрабатывается в `LaunchedEffect`.
- Перед открытием нового `WishDetailed` выполняется pop-to-root через
  `currentRoute`/`goBack(MainRoute)`.

**Проверка:** поделиться ссылкой из другого приложения, находясь на вложенном
экране → открывается `WishDetailed`, back возвращает на главный экран.

---

## Средние замечания (P2)

### [x] 6. Сортировка по title для кириллицы

**Статус:** исправлено быстрым вариантом. `TitleAZ/TitleZA` дополнительно
сортируются в `DatabaseRepository` по `title.lowercase()`. Комментарий в коде
объясняет причину и ограничение: это не полноценная locale-aware сортировка.

**Где:** `Wish.sq:80-90`, `WishTagRelation.sq:52,59`.

`ORDER BY title COLLATE NOCASE` в SQLite регистронезависим только для ASCII.
Для русских названий порядок может быть неожиданным.

### [x] 7. Reorder остаётся выключенным после возврата к Default

**Статус:** оставляем текущее поведение. После нестандартной сортировки reorder
нужно включить вручную; в коде добавлен комментарий.

**Где:** `WishListViewModel.kt:159-173`.

### [x] 8. Нет analytics-события для сортировки

**Статус:** не делаем по решению по ветке.

### [x] 9. `sortMode` не сохраняется между перезапусками

**Статус:** не делаем. После холодного старта используется `Default`.

### [ ] 10. iOS-сборка заблокирована несовместимостью плагина

`./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` падает:

```text
NoSuchMethodError:
org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
```

Это несовместимость `KMP-NativeCoroutines 1.0.0-ALPHA-22` с Kotlin `2.2.21`.
Версии те же, что и на `dev`, то есть проблема, судя по всему, уже существовала
до этой ветки. Но из-за неё iOS smoke-check сейчас невозможен.

---

## Мелочи (P3)

- [x] Убрать debug-логи `RTRT`:
  - `WishDetailedScreen.kt`
  - `WishDetailedViewModel.kt`
- [x] Убрать неиспользуемые импорты и закомментированный старый nav-код:
  - `Navigation.kt`
  - `AppActivity.kt`
- [x] Удалить мёртвый код:
  - старые строковые route и `ARG_*` в `Routes.kt`;
  - `WishAppAnimatedNavHost.kt`;
  - `NavTransitions.kt`;
  - неиспользуемый `wishImageId` в route и `WishImageClickData`.
- [ ] Вынести `contentDescription` в ресурсы:
  - `WishListBottomBar.kt:69,104`.
- [x] Добавить newline в конце файлов:
  - `Navigation.kt`
  - `NavigationState.kt`
  - `Navigator.kt`
- [ ] Проверить анимацию закрытия `SortBottomSheetContent`
  (`onDismissRequest` сейчас просто меняет boolean).
- [x] Тесты на сортировку не пишем по решению по ветке.

---

## Проверки

Уже выполнено:

```bash
./gradlew :app:compileDebugKotlin --offline
./gradlew :app:testDebugUnitTest :shared:testDebugUnitTest --offline
git diff --check dev...HEAD
```

Результат:

- Android-компиляция — успешно.
- Unit-тесты `app` и `shared` — успешно.
- `git diff --check` — чисто.
- iOS framework — не собирается по причине из пункта 10.

---

## Ручной чек-лист перед merge

- [ ] Создание желания.
- [ ] Редактирование title/comment/links.
- [ ] Добавление и просмотр изображений.
- [ ] Открытие просмотрщика изображений, удаление изображения.
- [ ] Экран тегов: выбор, создание, снятие тега.
- [ ] Переименование и удаление тегов.
- [ ] Завершение и возврат желания в текущие.
- [ ] Reorder в `Default`-сортировке.
- [ ] Все пять режимов сортировки.
- [ ] Шаринг ссылки из другого приложения:
  - с главного экрана;
  - с вложенного экрана.
- [ ] Тёмная тема в просмотрщике изображений.
- [ ] Сборка iOS-фреймворка и `iosApp` после устранения блокера плагина.

---

## Итог

Сортировка сделана аккуратно: SQL находится в `shared`, схема БД не менялась,
строки добавлены во все локали. Но ветка требует исправления P0/P1:

1. images viewer crash;
2. tags screen;
3. iOS API break;
4. force-dark;
5. share-link navigation.

После этого — повторное ревью и ручной прогон чек-листа.
