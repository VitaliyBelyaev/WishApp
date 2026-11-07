package ru.vitaliy.belyaev.wishapp.wishlist

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AmplitudeWrapper
import ru.vitaliy.belyaev.wishapp.domain.model.UiLook
import ru.vitaliy.belyaev.wishapp.screenshots.DemoData
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishListTestTags

/**
 * UI-тест экрана списка в новом виде: вкладки панели, шторки ярлыков и сортировки, меню «⋮»,
 * поиск, вид карточками и списком, прокрутка и системное «назад». Работает через настоящую
 * активность и базу, как [ru.vitaliy.belyaev.wishapp.smoke.SmokeTest], и так же стирает базу,
 * поэтому без аргумента `-e smoke true` пропускается. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.WishListScreenTest scripts/run_smoke_test.sh
 *
 * Новый вид пока не выпущен и выбирается только в debug, поэтому на сборке autotest тест пропускается.
 * Снимки экрана в ключевых состояниях складываются в `files/wishlist-test` приложения на устройстве,
 * скрипт забирает их в `app/build/wishlist-test-screenshots`: по ним видно, что всё выглядит как задумано.
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class WishListScreenTest {

    val rule = createAndroidComposeRule<AppActivity>()

    @get:Rule
    val testName = TestName()

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    /** База заполняется до запуска активности, иначе список удержит позицию на старых элементах. */
    private val seedRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                assumeTrue(
                    "Запускайте через scripts/run_smoke_test.sh",
                    InstrumentationRegistry.getArguments().getString("smoke") == "true",
                )
                assumeTrue("Новый вид пока выбирается только в debug", BuildConfig.DEBUG)
                FirebaseAnalytics.getInstance(targetContext).setAnalyticsCollectionEnabled(false)
                FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
                AmplitudeWrapper.amplitude?.configuration?.optOut = true
                seedDatabase()
                base.evaluate()
            }
        }
    }

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(seedRule).around(rule)

    private fun seedDatabase() = runBlocking {
        val repository = WishAppSdk(DatabaseDriverFactory(targetContext)).getDatabaseRepository()
        DemoData(language = "en").seed(repository)
        // С восемью ярлыками и больше в шторке появляется поиск
        (1..FILLER_TAGS_COUNT).forEach { number -> repository.insertTag(fillerTagTitle(number)) }
        (1..FILLER_WISHES_COUNT).forEach { number ->
            repository.insertWish(createEmptyWish().copy(title = fillerTitle(number)))
        }
    }

    /**
     * Выбор вида хранится в DataStore и переживает перезапуск активности, вид и сортировка списка тоже.
     * Поэтому каждый тест сам включает новый вид и возвращает список к виду по умолчанию.
     */
    @Before
    fun openNewLookWithDefaultListSettings() {
        rule.runOnUiThread {
            ViewModelProvider(rule.activity)[AppActivityViewModel::class.java].onUiLookChosen(UiLook.NEW)
        }
        waitUntilExists(tab(R.string.nav_tags))
        waitUntilExists(wish(TOP_WISH))

        openMenu()
        if (exists(menuItem(R.string.menu_show_cards))) {
            click(menuItem(R.string.menu_show_cards))
        } else {
            closeMenu()
        }
        if (exists(hasTestTag(WishListTestTags.SORT_DOT))) chooseSort(R.string.sort_my_order)
        waitUntilFirstWish(TOP_WISH)
    }

    @Test
    fun wishesTabShowsActiveWishesInTwoColumns() {
        rule.onNode(heading(string(R.string.nav_wishes))).assertIsDisplayed()
        rule.onNode(tab(R.string.nav_wishes)).assertIsSelected()
        rule.onNode(tab(R.string.nav_done)).assertIsNotSelected()
            // Счётчик сбывшихся на вкладке
            .assert(hasText(DONE_COUNT.toString()))
        rule.onNode(tab(R.string.nav_tags)).assertIsNotSelected()
        rule.onNode(hasContentDescription(string(R.string.cd_add_wish))).assertIsDisplayed()
        rule.onNode(hasContentDescription(string(R.string.cd_search))).assertIsDisplayed()
        rule.onNode(hasContentDescription(string(R.string.cd_more))).assertIsDisplayed()
        rule.onNode(hasTestTag(WishListTestTags.SORT_DOT)).assertDoesNotExist()

        // Сбывшиеся не в «Хотелках», а конец списка ещё не на экране
        rule.onNode(wish(DONE_WISH_SPEAKER)).assertDoesNotExist()
        rule.onNode(wish(LAST_WISH)).assertDoesNotExist()

        // Две колонки, первые карточки под заголовком, а не под ним
        val first = boundsOf(wish(TOP_WISH))
        val second = boundsOf(wish(fillerTitle(FILLER_WISHES_COUNT - 1)))
        assertEquals(first.top, second.top, ROW_TOLERANCE_PX)
        assertTrue("Вторая карточка правее первой", second.left > first.right)
        assertTrue("Карточки ниже заголовка", first.top >= boundsOf(heading(string(R.string.nav_wishes))).bottom)
        screenshot("cards")
    }

    @Test
    fun layoutToggleSwitchesBetweenCardsAndList() {
        openMenu()
        rule.onNode(menuItem(R.string.menu_show_list)).assertIsDisplayed()
        click(menuItem(R.string.menu_show_list))

        // Одна колонка: следующая хотелка под первой
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            val first = boundsOf(wish(TOP_WISH))
            val second = boundsOf(wish(fillerTitle(FILLER_WISHES_COUNT - 1)))
            second.top > first.bottom && kotlin.math.abs(second.left - first.left) < ROW_TOLERANCE_PX
        }
        screenshot("list")

        openMenu()
        click(menuItem(R.string.menu_show_cards))
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            val first = boundsOf(wish(TOP_WISH))
            val second = boundsOf(wish(fillerTitle(FILLER_WISHES_COUNT - 1)))
            second.left > first.right
        }
    }

    @Test
    fun overflowMenuShowsSectionActions() {
        openMenu()
        rule.onNode(menuItem(R.string.menu_share)).assert(hasText(shareHint(R.string.nav_wishes, ACTIVE_COUNT)))
        rule.onNode(menuItem(R.string.menu_sort)).assert(hasText(string(R.string.sort_my_order)))
        rule.onNode(menuItem(R.string.menu_show_list)).assertIsDisplayed()
        rule.onNode(menuItem(R.string.settings)).assertIsDisplayed()
        rule.onNode(hasTestTag(WishListTestTags.MENU_SORT_DOT), useUnmergedTree = true).assertDoesNotExist()
        screenshot("menu")
        closeMenu()
    }

    @Test
    fun sortChangesOrderAndMarksNonDefaultSort() {
        openMenu()
        click(menuItem(R.string.menu_sort))
        waitUntilExists(sortRow(R.string.sort_my_order))
        rule.onNode(sortRow(R.string.sort_my_order)).assertIsSelected()
        listOf(R.string.sort_created_newest, R.string.sort_created_oldest, R.string.sort_az, R.string.sort_za)
            .forEach { rule.onNode(sortRow(it)).assertIsNotSelected() }
        screenshot("sort_sheet")
        click(sortRow(R.string.sort_az))
        waitUntilGone(sortRow(R.string.sort_my_order))

        waitUntilFirstWish(FIRST_WISH_AZ)
        // Точка на «⋮» и на пункте «Сортировка»: порядок не по умолчанию
        rule.onNode(hasTestTag(WishListTestTags.SORT_DOT)).assertExists()
        openMenu()
        rule.onNode(menuItem(R.string.menu_sort)).assert(hasText(string(R.string.sort_az)))
        rule.onNode(hasTestTag(WishListTestTags.MENU_SORT_DOT), useUnmergedTree = true).assertExists()
        screenshot("menu_with_dot")
        closeMenu()

        chooseSort(R.string.sort_za)
        waitUntilFirstWish(FIRST_WISH_ZA)

        chooseSort(R.string.sort_my_order)
        waitUntilFirstWish(TOP_WISH)
        waitUntilGone(hasTestTag(WishListTestTags.SORT_DOT))
    }

    @Test
    fun doneTabShowsCompletedWishesWithOwnSort() {
        click(tab(R.string.nav_done))
        waitUntilExists(heading(string(R.string.nav_done)))
        rule.onNode(tab(R.string.nav_done)).assertIsSelected()
        rule.onNode(tab(R.string.nav_wishes)).assertIsNotSelected()
        waitUntilExists(wish(DONE_WISH_SPEAKER))
        rule.onNode(wish(DONE_WISH_BLANKET)).assertIsDisplayed()
        rule.onNode(wish(TOP_WISH)).assertDoesNotExist()

        // Сортировка «Сбылось» своя: без «Моего порядка», по умолчанию сначала новые
        if (exists(hasTestTag(WishListTestTags.SORT_DOT))) chooseSort(R.string.sort_created_newest)
        openMenu()
        rule.onNode(menuItem(R.string.menu_sort)).assert(hasText(string(R.string.sort_created_newest)))
        click(menuItem(R.string.menu_sort))
        waitUntilExists(sortRow(R.string.sort_created_newest))
        rule.onNode(sortRow(R.string.sort_my_order)).assertDoesNotExist()
        click(sortRow(R.string.sort_az))
        waitUntilFirstWish(DONE_WISH_BLANKET)
        chooseSort(R.string.sort_za)
        waitUntilFirstWish(DONE_WISH_SPEAKER)
        rule.onNode(hasTestTag(WishListTestTags.SORT_DOT)).assertExists()
        screenshot("done")

        chooseSort(R.string.sort_created_newest)
        waitUntilGone(hasTestTag(WishListTestTags.SORT_DOT))
        // Сортировка «Хотелок» от этого не поменялась
        click(tab(R.string.nav_wishes))
        waitUntilFirstWish(TOP_WISH)
        rule.onNode(hasTestTag(WishListTestTags.SORT_DOT)).assertDoesNotExist()
    }

    @Test
    fun systemBackFromDoneReturnsToWishes() {
        click(tab(R.string.nav_done))
        waitUntilExists(heading(string(R.string.nav_done)))
        device.pressBack()
        waitUntilExists(heading(string(R.string.nav_wishes)))
        rule.onNode(tab(R.string.nav_wishes)).assertIsSelected()
        waitUntilFirstWish(TOP_WISH)
    }

    @Test
    fun tagsSheetSearchesAndSelectsTag() {
        click(tab(R.string.nav_tags))
        waitUntilExists(hasText(string(R.string.tags_manage)))
        rule.onNode(tagRow(TAG_BOOKS)).assertIsDisplayed().assertIsNotSelected()
            // Сколько текущих хотелок с ярлыком
            .assert(hasText(TAG_BOOKS_COUNT.toString()))
        rule.onNode(hasText(string(R.string.tags_find))).assertIsDisplayed()
        rule.onNode(hasContentDescription(string(R.string.cd_clear_search))).assertDoesNotExist()
        screenshot("tags_sheet")

        // Поиск сужает список, крестик его очищает
        rule.onNode(hasSetTextAction()).performTextInput("trav")
        waitUntilGone(tagRow(TAG_BOOKS))
        rule.onNode(tagRow(TAG_TRAVEL)).assertIsDisplayed()
        click(hasContentDescription(string(R.string.cd_clear_search)))
        waitUntilExists(tagRow(TAG_BOOKS))
        rule.onNode(hasContentDescription(string(R.string.cd_clear_search))).assertDoesNotExist()

        rule.onNode(hasSetTextAction()).performTextInput("no such tag")
        waitUntilExists(hasText(string(R.string.tags_not_found)))
        screenshot("tags_sheet_not_found")
        click(hasContentDescription(string(R.string.cd_clear_search)))
        waitUntilExists(tagRow(TAG_BOOKS))

        // Выбор ярлыка закрывает шторку и показывает его хотелки
        click(tagRow(TAG_BOOKS))
        waitUntilGone(hasText(string(R.string.tags_manage)))
        waitUntilExists(heading(TAG_BOOKS))
        rule.onNode(tab(TAG_BOOKS)).assertIsSelected()
        rule.onNode(tab(R.string.nav_wishes)).assertIsNotSelected()
        waitUntilExists(wish(BOOKS_WISH_NOVEL))
        rule.onNode(wish(BOOKS_WISH_READER)).assertIsDisplayed()
        rule.onNode(wish(NOT_BOOKS_WISH)).assertDoesNotExist()
        rule.onNode(wish(TOP_WISH)).assertDoesNotExist()
        // У ярлыка «Поделиться» вынесено в заголовок
        rule.onNode(hasContentDescription(string(R.string.menu_share))).assertIsDisplayed()
        screenshot("tag")

        // В шторке выбранный ярлык отмечен, можно сразу переключиться на другой
        click(tab(TAG_BOOKS))
        waitUntilExists(hasText(string(R.string.tags_manage)))
        rule.onNode(tagRow(TAG_BOOKS)).assertIsSelected()
        click(tagRow(TAG_TRAVEL))
        waitUntilExists(heading(TAG_TRAVEL))
        waitUntilExists(wish(TRAVEL_WISH_TRIP))
        rule.onNode(wish(TRAVEL_WISH_BACKPACK)).assertIsDisplayed()
        rule.onNode(wish(BOOKS_WISH_NOVEL)).assertDoesNotExist()

        device.pressBack()
        waitUntilExists(heading(string(R.string.nav_wishes)))
        rule.onNode(tab(R.string.nav_tags)).assertIsNotSelected()
        waitUntilFirstWish(TOP_WISH)
    }

    @Test
    fun tagsSheetIsOpenAgainAfterManage() {
        click(tab(R.string.nav_tags))
        waitUntilExists(hasText(string(R.string.tags_manage)))
        click(hasText(string(R.string.tags_manage)))
        // Экран управления ярлыками открыт поверх списка
        waitUntilGone(tab(R.string.nav_tags))

        device.pressBack()
        waitUntilExists(tab(R.string.nav_tags))
        waitUntilExists(hasText(string(R.string.tags_manage)))
        rule.onNode(tagRow(TAG_BOOKS)).assertIsDisplayed()

        device.pressBack()
        waitUntilGone(hasText(string(R.string.tags_manage)))
        rule.onNode(heading(string(R.string.nav_wishes))).assertIsDisplayed()
    }

    @Test
    fun scrollAndTappingSelectedTabReturnsToTop() {
        val topAtStart = boundsOf(wish(TOP_WISH)).top

        // Прокрутка жестом в середине экрана, ниже начинается панель, и она касания не пропускает
        rule.onNode(list()).performTouchInput {
            swipeUp(startY = centerY + height / 6f, endY = top + height / 6f)
        }
        rule.waitForIdle()
        assertTrue("Список прокрутился", !exists(wish(TOP_WISH)) || boundsOf(wish(TOP_WISH)).top < topAtStart - 1f)
        screenshot("scrolled")

        // Нажатие на открытую вкладку поднимает список
        click(tab(R.string.nav_wishes))
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            exists(wish(TOP_WISH)) && kotlin.math.abs(boundsOf(wish(TOP_WISH)).top - topAtStart) < 1f
        }

        // Издалека тоже: с конца списка
        rule.onNode(list()).performScrollToIndex(ACTIVE_COUNT - 1)
        waitUntilExists(wish(LAST_WISH))
        rule.onNode(wish(TOP_WISH)).assertDoesNotExist()
        click(tab(R.string.nav_wishes))
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            exists(wish(TOP_WISH)) && kotlin.math.abs(boundsOf(wish(TOP_WISH)).top - topAtStart) < 1f
        }

        // Другой раздел открывается с начала, и «Хотелки» при возврате тоже
        rule.onNode(list()).performScrollToIndex(ACTIVE_COUNT - 1)
        waitUntilExists(wish(LAST_WISH))
        click(tab(R.string.nav_done))
        waitUntilExists(wish(DONE_WISH_SPEAKER))
        click(tab(R.string.nav_wishes))
        waitUntilFirstWish(TOP_WISH)
        assertEquals(topAtStart, boundsOf(wish(TOP_WISH)).top, 1f)
    }

    @Test
    fun areaUnderDockDoesNotPassTouchesToList() {
        // Середина списка: карточки есть и под панелью
        rule.onNode(list()).performScrollToIndex(SCROLL_MIDDLE_INDEX)
        rule.waitForIdle()
        val addButton = boundsOf(hasContentDescription(string(R.string.cd_add_wish)))
        // Левее кнопки «+» и ниже панели: место между панелью и системной навигацией
        val point = with(rule.density) { Offset(addButton.left - 24.dp.toPx(), addButton.bottom + 6.dp.toPx()) }
        val cardUnderPoint = rule.onAllNodes(listItem()).fetchSemanticsNodes()
            .firstOrNull { it.boundsInRoot.contains(point) }
        assertTrue("Под точкой нажатия должна быть карточка, иначе проверка ничего не значит", cardUnderPoint != null)

        val listBounds = boundsOf(list())
        rule.onNode(list()).performTouchInput { this.click(point - listBounds.topLeft) }
        rule.waitForIdle()
        // Экран хотелки не открылся: список и панель на месте
        rule.onNode(tab(R.string.nav_wishes)).assertIsDisplayed()

        // А видимая карточка открывается, после «назад» список на месте
        click(tab(R.string.nav_wishes))
        waitUntilFirstWish(TOP_WISH)
        click(wish(TOP_WISH))
        waitUntilGone(tab(R.string.nav_wishes))
        device.pressBack()
        waitUntilExists(tab(R.string.nav_wishes))
        waitUntilExists(wish(TOP_WISH))
    }

    @Test
    fun searchFiltersSectionAndOffersOtherSection() {
        click(hasContentDescription(string(R.string.cd_search)))
        waitUntilExists(hasSetTextAction())
        rule.onNode(hasText(string(R.string.search_hint))).assertIsDisplayed()
        rule.onNode(hasContentDescription(string(R.string.cd_clear_search))).assertDoesNotExist()

        rule.onNode(hasSetTextAction()).performTextInput("lamp")
        waitUntilGone(wish(TOP_WISH))
        rule.onNode(wish(WISH_LAMP)).assertIsDisplayed()
        screenshot("search")

        // Крестик очищает запрос, поиск остаётся открытым
        click(hasContentDescription(string(R.string.cd_clear_search)))
        waitUntilExists(wish(TOP_WISH))
        rule.onNode(hasSetTextAction()).assertExists()

        // Не нашлось здесь, но есть в «Сбылось»
        rule.onNode(hasSetTextAction()).performTextInput(DONE_WISH_BLANKET)
        waitUntilExists(hasText(string(R.string.search_empty_title)))
        rule.onNode(hasText(string(R.string.search_elsewhere, string(R.string.nav_wishes)))).assertIsDisplayed()
        rule.onNode(hasText(string(R.string.search_create, DONE_WISH_BLANKET))).assertIsDisplayed()
        screenshot("search_elsewhere")
        click(hasText(string(R.string.search_go_to, string(R.string.nav_done))))
        // Панель спрятана, пока открыта клавиатура: раздел видно по подсказке и заголовку после поиска
        waitUntilExists(wish(DONE_WISH_BLANKET))
        rule.onNode(hasSetTextAction()).assert(hasText(DONE_WISH_BLANKET))

        // «Назад» сначала закрывает поиск, потом ведёт в «Хотелки». Через диспетчер активности,
        // потому что системное «назад» сперва прячет клавиатуру
        dispatchBack()
        waitUntilExists(heading(string(R.string.nav_done)))
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        dispatchBack()
        waitUntilExists(heading(string(R.string.nav_wishes)))
        waitUntilFirstWish(TOP_WISH)

        // Ничего нигде: предлагает создать хотелку, закрывается стрелкой
        click(hasContentDescription(string(R.string.cd_search)))
        rule.onNode(hasSetTextAction()).performTextInput("qwerty")
        waitUntilExists(hasText(string(R.string.search_create, "qwerty")))
        rule.onNode(hasText(string(R.string.search_empty_text))).assertIsDisplayed()
        click(hasContentDescription(string(R.string.cd_close_search)))
        waitUntilExists(heading(string(R.string.nav_wishes)))
        waitUntilExists(wish(TOP_WISH))
    }

    @Test
    fun shareMenuShowsScopeOfSection() {
        openMenu()
        click(menuItem(R.string.menu_share))
        waitUntilExists(hasText(shareScope(string(R.string.nav_wishes), ACTIVE_COUNT)))
        rule.onNode(hasText(string(R.string.share_as_text))).assertIsDisplayed()
        rule.onNode(hasText(string(R.string.share_as_pdf))).assertIsDisplayed()
        screenshot("share")
        device.pressBack()
        waitUntilGone(hasText(string(R.string.share_as_text)))

        // У ярлыка кнопка в заголовке, подпись с его названием
        click(tab(R.string.nav_tags))
        waitUntilExists(tagRow(TAG_BOOKS))
        click(tagRow(TAG_BOOKS))
        waitUntilExists(heading(TAG_BOOKS))
        click(hasContentDescription(string(R.string.menu_share)))
        waitUntilExists(hasText(shareScope(TAG_BOOKS, TAG_BOOKS_COUNT)))
        device.pressBack()
        waitUntilGone(hasText(string(R.string.share_as_text)))
    }

    // region Поиск элементов

    /** Карточка или строка хотелки: описание для TalkBack начинается с названия. */
    private fun wish(title: String): SemanticsMatcher = SemanticsMatcher("хотелка «$title»") { node ->
        val description = node.description()
        description == title || description.startsWith("$title,")
    } and hasClickAction()

    private fun listItem(): SemanticsMatcher = hasAnyAncestor(list()) and hasClickAction()

    private fun list(): SemanticsMatcher = hasTestTag(WishListTestTags.LIST)

    private fun heading(title: String): SemanticsMatcher = isHeading() and hasText(title)

    private fun tab(@StringRes label: Int): SemanticsMatcher = tab(string(label))

    private fun tab(label: String): SemanticsMatcher =
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun menuItem(@StringRes text: Int): SemanticsMatcher = hasText(string(text)) and hasClickAction()

    private fun sortRow(@StringRes text: Int): SemanticsMatcher =
        hasText(string(text)) and isSelectable() and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

    private fun tagRow(title: String): SemanticsMatcher =
        hasText(title) and isSelectable() and !SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun SemanticsNode.description(): String =
        config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull().orEmpty()

    // endregion

    // region Действия и ожидания

    private fun openMenu() {
        click(hasContentDescription(string(R.string.cd_more)))
        waitUntilExists(menuItem(R.string.settings))
    }

    private fun closeMenu() {
        device.pressBack()
        waitUntilGone(menuItem(R.string.settings))
    }

    private fun chooseSort(@StringRes mode: Int) {
        openMenu()
        click(menuItem(R.string.menu_sort))
        waitUntilExists(sortRow(mode))
        click(sortRow(mode))
        waitUntilGone(sortRow(mode))
    }

    /** Первая хотелка раздела: левая в верхнем ряду. */
    private fun waitUntilFirstWish(title: String) {
        val isExpected = { description: String? ->
            description != null && (description == title || description.startsWith("$title,"))
        }
        try {
            rule.waitUntil(WAIT_TIMEOUT_MILLIS) { isExpected(firstWishDescription()) }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("Первой должна быть «$title», а первая «${firstWishDescription()}»", e)
        }
    }

    private fun firstWishDescription(): String? = rule.onAllNodes(listItem()).fetchSemanticsNodes()
        .minWithOrNull(compareBy<SemanticsNode>({ it.boundsInRoot.top }, { it.boundsInRoot.left }))
        ?.description()

    private fun dispatchBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun click(matcher: SemanticsMatcher) {
        rule.onNode(matcher).performClick()
        rule.waitForIdle()
    }

    private fun exists(matcher: SemanticsMatcher): Boolean = rule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    private fun boundsOf(matcher: SemanticsMatcher): Rect = rule.onNode(matcher).fetchSemanticsNode().boundsInRoot

    private fun waitUntilExists(matcher: SemanticsMatcher) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) { exists(matcher) }
    }

    private fun waitUntilGone(matcher: SemanticsMatcher) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) { !exists(matcher) }
    }

    private fun string(@StringRes resId: Int, vararg args: Any): String = rule.activity.getString(resId, *args)

    private fun shareHint(@StringRes section: Int, count: Int): String =
        string(R.string.share_scope, string(section), count.toString())

    private fun shareScope(section: String, count: Int): String = string(
        R.string.share_scope,
        section,
        rule.activity.resources.getQuantityString(R.plurals.wishes_count, count, count),
    )

    /** Снимок всего экрана вместе со шторками и меню, файлы забирает scripts/run_smoke_test.sh. */
    private fun screenshot(name: String) {
        rule.waitForIdle()
        val dir = File(targetContext.getExternalFilesDir(null), SCREENSHOTS_DIR).apply { mkdirs() }
        device.takeScreenshot(File(dir, "${testName.methodName}_$name.png"))
    }

    // endregion

    private companion object {

        const val WAIT_TIMEOUT_MILLIS = 10_000L
        const val ROW_TOLERANCE_PX = 2f
        const val SCREENSHOTS_DIR = "wishlist-test"
        const val FILLER_WISHES_COUNT = 30
        const val FILLER_TAGS_COUNT = 8
        const val SCROLL_MIDDLE_INDEX = 10

        // Хотелки и ярлыки из DemoData(language = "en")
        const val DEMO_ACTIVE_COUNT = 7
        const val ACTIVE_COUNT = DEMO_ACTIVE_COUNT + FILLER_WISHES_COUNT
        const val DONE_COUNT = 2
        const val LAST_WISH = "E-reader"
        const val WISH_LAMP = "Desk lamp"
        const val DONE_WISH_SPEAKER = "Smart speaker"
        const val DONE_WISH_BLANKET = "Blanket"
        const val TAG_BOOKS = "Books"
        const val TAG_BOOKS_COUNT = 2
        const val BOOKS_WISH_NOVEL = "The Master and Margarita"
        const val BOOKS_WISH_READER = "E-reader"
        const val NOT_BOOKS_WISH = "Coffee machine"
        const val TAG_TRAVEL = "Travel"
        const val TRAVEL_WISH_TRIP = "Trip to Georgia"
        const val TRAVEL_WISH_BACKPACK = "Laptop backpack"
        const val FIRST_WISH_AZ = "Coffee machine"
        const val FIRST_WISH_ZA = "Wireless headphones"

        /** Новые желания попадают в начало списка, поэтому сверху последнее добавленное. */
        val TOP_WISH = fillerTitle(FILLER_WISHES_COUNT)

        fun fillerTitle(number: Int): String = "Filler wish $number"

        fun fillerTagTitle(number: Int): String = "Filler label %02d".format(number)
    }
}
