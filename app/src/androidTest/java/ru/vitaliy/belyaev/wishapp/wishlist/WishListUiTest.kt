package ru.vitaliy.belyaev.wishapp.wishlist

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
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
 * Общая основа UI-тестов нового вида списка: заполняет базу, включает новый вид с настройками списка
 * по умолчанию и даёт поиск элементов, действия, ожидания и снимки экрана. Работает через настоящую
 * активность и базу, как [ru.vitaliy.belyaev.wishapp.smoke.SmokeTest], и так же стирает базу,
 * поэтому без аргумента `-e smoke true` пропускается. Новый вид пока выбирается только в debug,
 * поэтому на сборке autotest тесты пропускаются.
 *
 * Снимки экрана в ключевых состояниях складываются в `files/wishlist-test` приложения на устройстве,
 * скрипт забирает их в `app/build/wishlist-test-screenshots`: по ним видно, что всё выглядит как задумано.
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
// Абстрактный, чтобы JUnit не запускал его сам: тестов в нём нет, только подготовка
@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class WishListUiTest {

    val rule = createAndroidComposeRule<AppActivity>()

    @get:Rule
    val testName = TestName()

    protected val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    protected val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

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

    // region Выбор и перенос

    protected fun addButton(): SemanticsMatcher =
        hasContentDescription(string(R.string.cd_add_wish)) and hasClickAction()

    protected fun newWish(tag: String): SemanticsMatcher = hasTestTag(tag)

    protected fun selectionCount(count: Int): SemanticsMatcher =
        hasTestTag(WishListTestTags.SELECTION_COUNT) and hasText(string(R.string.selection_count, count))

    /** Долгое нажатие и отпускание на месте. */
    protected fun longPressInPlace(matcher: SemanticsMatcher) {
        rule.onNode(matcher).performTouchInput { longClick() }
        rule.waitForIdle()
    }

    /** Долгое нажатие и перенос пальцем в точку [target] в координатах корня. */
    protected fun dragTo(matcher: SemanticsMatcher, target: Offset) {
        val bounds = boundsOf(matcher)
        rule.onNode(matcher).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_EXTRA_MILLIS)
            val start = center
            val end = target - bounds.topLeft
            repeat(DRAG_STEPS) { step ->
                moveTo(start + (end - start) * ((step + 1) / DRAG_STEPS.toFloat()))
                advanceEventTime(DRAG_STEP_MILLIS)
            }
            up()
        }
        rule.waitForIdle()
    }

    protected fun customActionLabels(matcher: SemanticsMatcher): List<String> =
        rule.onNode(matcher).fetchSemanticsNode().config
            .getOrNull(SemanticsActions.CustomActions).orEmpty().map { it.label }

    protected fun performCustomAction(matcher: SemanticsMatcher, @StringRes label: Int) {
        val action = rule.onNode(matcher).fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label == string(label) }
        rule.runOnUiThread { action.action() }
        rule.waitForIdle()
    }

    // endregion

    // region Поиск элементов

    /** Карточка или строка хотелки: описание для TalkBack начинается с названия. */
    protected fun wish(title: String): SemanticsMatcher = SemanticsMatcher("хотелка «$title»") { node ->
        val description = node.description()
        description == title || description.startsWith("$title,")
    } and hasClickAction()

    protected fun listItem(): SemanticsMatcher = hasAnyAncestor(list()) and hasClickAction()

    protected fun list(): SemanticsMatcher = hasTestTag(WishListTestTags.LIST)

    protected fun heading(title: String): SemanticsMatcher = isHeading() and hasText(title)

    protected fun tab(@StringRes label: Int): SemanticsMatcher = tab(string(label))

    protected fun tab(label: String): SemanticsMatcher =
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    protected fun menuItem(@StringRes text: Int): SemanticsMatcher = hasText(string(text)) and hasClickAction()

    protected fun sortRow(@StringRes text: Int): SemanticsMatcher =
        hasText(string(text)) and isSelectable() and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

    protected fun tagRow(title: String): SemanticsMatcher =
        hasText(title) and isSelectable() and !SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    protected fun SemanticsNode.description(): String =
        config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull().orEmpty()

    // endregion

    // region Действия и ожидания

    protected fun openMenu() {
        click(hasContentDescription(string(R.string.cd_more)))
        waitUntilExists(menuItem(R.string.settings))
    }

    protected fun closeMenu() {
        device.pressBack()
        waitUntilGone(menuItem(R.string.settings))
    }

    protected fun chooseSort(@StringRes mode: Int) {
        openMenu()
        click(menuItem(R.string.menu_sort))
        waitUntilExists(sortRow(mode))
        click(sortRow(mode))
        waitUntilGone(sortRow(mode))
    }

    /** Первая хотелка раздела: левая в верхнем ряду. */
    protected fun waitUntilFirstWish(title: String) {
        val isExpected = { description: String? ->
            description != null && (description == title || description.startsWith("$title,"))
        }
        try {
            rule.waitUntil(WAIT_TIMEOUT_MILLIS) { isExpected(firstWishDescription()) }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("Первой должна быть «$title», а первая «${firstWishDescription()}»", e)
        }
    }

    // Сетка держит собранными и элементы вне экрана с нулевым размером: они не в счёт
    protected fun firstWishDescription(): String? = rule.onAllNodes(listItem()).fetchSemanticsNodes()
        .filter { it.boundsInRoot.width > 0f }
        .minWithOrNull(compareBy<SemanticsNode>({ it.boundsInRoot.top }, { it.boundsInRoot.left }))
        ?.description()

    protected fun dispatchBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    protected fun click(matcher: SemanticsMatcher) {
        rule.onNode(matcher).performClick()
        rule.waitForIdle()
    }

    protected fun exists(matcher: SemanticsMatcher): Boolean = rule.onAllNodes(
        matcher,
    ).fetchSemanticsNodes().isNotEmpty()

    protected fun boundsOf(matcher: SemanticsMatcher): Rect = rule.onNode(matcher).fetchSemanticsNode().boundsInRoot

    protected fun waitUntilExists(matcher: SemanticsMatcher) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) { exists(matcher) }
    }

    protected fun waitUntilGone(matcher: SemanticsMatcher) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) { !exists(matcher) }
    }

    protected fun string(@StringRes resId: Int, vararg args: Any): String = rule.activity.getString(resId, *args)

    protected fun shareHint(@StringRes section: Int, count: Int): String =
        string(R.string.share_scope, string(section), count.toString())

    protected fun shareScope(section: String, count: Int): String = string(
        R.string.share_scope,
        section,
        rule.activity.resources.getQuantityString(R.plurals.wishes_count, count, count),
    )

    /** Снимок всего экрана вместе со шторками и меню, файлы забирает scripts/run_smoke_test.sh. */
    protected fun screenshot(name: String) {
        rule.waitForIdle()
        val dir = File(targetContext.getExternalFilesDir(null), SCREENSHOTS_DIR).apply { mkdirs() }
        device.takeScreenshot(File(dir, "${testName.methodName}_$name.png"))
    }

    // endregion

    companion object {

        const val WAIT_TIMEOUT_MILLIS = 10_000L
        const val ROW_TOLERANCE_PX = 2f
        const val SCREENSHOTS_DIR = "wishlist-test"
        const val FILLER_WISHES_COUNT = 30
        const val FILLER_TAGS_COUNT = 8
        const val SCROLL_MIDDLE_INDEX = 10
        const val LONG_PRESS_EXTRA_MILLIS = 200L
        const val DRAG_STEPS = 20
        const val DRAG_STEP_MILLIS = 16L

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
        const val NEW_WISH = "Robot vacuum"
        const val NEXT_WISH = "Plant pot"
        const val NEW_TAG = "Kitchen"
        const val NOT_A_LINK = "just words"
        const val BARE_LINK = "example.com/item/42"
        const val BARE_LINK_HOST = "example.com"

        /** Новые желания попадают в начало списка, поэтому сверху последнее добавленное. */
        val TOP_WISH = fillerTitle(FILLER_WISHES_COUNT)

        fun fillerTitle(number: Int): String = "Filler wish $number"

        fun fillerTagTitle(number: Int): String = "Filler label %02d".format(Locale.ROOT, number)
    }
}
