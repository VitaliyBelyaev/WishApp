package ru.vitaliy.belyaev.wishapp.smoke

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AmplitudeWrapper
import ru.vitaliy.belyaev.wishapp.screenshots.DemoData
import ru.vitaliy.belyaev.wishapp.screenshots.wishImageMatcher
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.ui.AppActivity

/**
 * Проверка, что приложение запускается, все экраны открываются и основные действия
 * доходят до базы: один успешный сценарий без разбора всех состояний. Нужна после
 * обновления зависимостей и перед релизом. Запускается через `scripts/run_smoke_test.sh`,
 * перед релизом ещё и на сборке autotest (с R8), поэтому тест работает как пользователь:
 * код приложения напрямую трогает только заполнение базы.
 *
 * Тест стирает базу приложения, поэтому без аргумента `-e smoke true` он пропускается.
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class SmokeTest {

    val rule = createAndroidComposeRule<AppActivity>()

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val demoData = DemoData(language = "en")

    /** База заполняется до запуска активности, иначе список удержит позицию на старых элементах. */
    private val smokeModeRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                assumeTrue(
                    "Запускайте через scripts/run_smoke_test.sh",
                    InstrumentationRegistry.getArguments().getString("smoke") == "true"
                )
                // Java API вместо ktx-расширений: в сборке autotest R8 вырезает
                // `com.google.firebase.Firebase`, приложение его не использует
                FirebaseAnalytics.getInstance(targetContext).setAnalyticsCollectionEnabled(false)
                FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
                AmplitudeWrapper.amplitude?.configuration?.optOut = true
                seedDatabase()
                base.evaluate()
            }
        }
    }

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(smokeModeRule).around(rule)

    private val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private fun seedDatabase() = runBlocking {
        val repository = WishAppSdk(DatabaseDriverFactory(targetContext)).getDatabaseRepository()
        demoData.seed(repository)
        (1..FILLER_WISHES_COUNT).forEach { number ->
            repository.insertWish(createEmptyWish().copy(title = fillerTitle(number)))
        }
    }

    @Test
    fun mainFlow() {
        scrollWishList()
        sortWishes()
        reorderWishes()
        selectWishes()
        shareWishes()
        addWish()
        receiveSharedLinks()
        openWishScreens()
        editWish()
        completeWish()
        openEditTags()
        filterWishes()
        openSettingsScreens()
        deleteWishFromList()
    }

    private fun scrollWishList() {
        waitForText(TOP_WISH_TITLE)
        rule.onNodeWithText(TOP_WISH_TITLE).assertIsDisplayed()
        // Последнего желания не должно быть на экране, иначе тест не проверяет прокрутку
        rule.onNodeWithText(demoData.lastWishTitle).assertDoesNotExist()

        scrollListTo(demoData.lastWishTitle)
        rule.onNodeWithText(demoData.lastWishTitle).assertIsDisplayed()
        scrollListToTop()
        rule.onNodeWithText(TOP_WISH_TITLE).assertIsDisplayed()
    }

    private fun sortWishes() {
        listOf(R.string.sort_created_newest, R.string.sort_created_oldest, R.string.sort_title_za).forEach { mode ->
            selectSortMode(mode)
            waitForList()
        }

        selectSortMode(R.string.sort_title_az)
        // В алфавитном порядке желания-заполнители идут ниже демо-желаний
        scrollListToTop()
        waitUntilGone(TOP_WISH_TITLE)

        selectSortMode(R.string.sort_default)
        scrollListToTop()
        waitForText(TOP_WISH_TITLE)
    }

    private fun selectSortMode(titleResId: Int) {
        clickDescription("Sort wishes")
        waitForText(string(R.string.sort_title))
        clickText(string(titleResId))
        waitUntilGone(string(R.string.sort_title))
    }

    /** Переставляет верхнее желание на одну позицию вниз и обратно, порядок сохраняется в базе. */
    private fun reorderWishes() {
        val secondWishTitle = fillerTitle(FILLER_WISHES_COUNT - 1)
        clickDescription("Reorder wishes")
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithContentDescription("Move down").fetchSemanticsNodes().isNotEmpty()
        }

        clickDescriptionInRowOf("Move down", TOP_WISH_TITLE)
        waitForOrder(upper = secondWishTitle, lower = TOP_WISH_TITLE)
        clickDescriptionInRowOf("Move up", TOP_WISH_TITLE)
        waitForOrder(upper = TOP_WISH_TITLE, lower = secondWishTitle)

        clickDescription("Reorder wishes")
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithContentDescription("Move down").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun selectWishes() {
        rule.onAllNodesWithText(TOP_WISH_TITLE).onFirst().performSemanticsAction(SemanticsActions.OnLongClick)
        waitForDescription("Close")
        clickText(fillerTitle(FILLER_WISHES_COUNT - 1))
        clickDescription("Menu")
        clickText(string(R.string.select_all))
        clickDescription("Close")
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithContentDescription("Close").fetchSemanticsNodes().isEmpty()
        }
    }

    /** Текст и PDF уходят в системное окно «Поделиться», тест только дожидается его и возвращается. */
    private fun shareWishes() {
        clickDescription("Share wishes")
        waitForText(string(R.string.share_title))
        clickText(string(R.string.share_type_text_button_text))
        returnFromSystemShareSheet()

        clickDescription("Share wishes")
        waitForText(string(R.string.share_title))
        clickText(string(R.string.share_type_pdf_button_text))
        returnFromSystemShareSheet(timeoutMillis = PDF_TIMEOUT_MILLIS)
    }

    private fun returnFromSystemShareSheet(timeoutMillis: Long = WAIT_TIMEOUT_MILLIS) {
        val appPackage = targetContext.packageName
        val deadline = System.currentTimeMillis() + timeoutMillis
        // Пока сверху системное окно, дерево Compose приложения недоступно: ждём только смену пакета
        while (device.currentPackageName == appPackage) {
            if (System.currentTimeMillis() > deadline) {
                val pdfFailed = rule.onAllNodesWithText(string(R.string.share_pdf_error_message))
                    .fetchSemanticsNodes().isNotEmpty()
                error(if (pdfFailed) "Не удалось поделиться PDF" else "Системное окно «Поделиться» не открылось")
            }
            Thread.sleep(POLL_MILLIS)
        }
        device.pressBack()
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) { device.currentPackageName == appPackage }
        if (rule.onAllNodesWithText(string(R.string.share_title)).fetchSemanticsNodes().isNotEmpty()) {
            device.pressBack()
        }
        waitUntilGone(string(R.string.share_title))
        waitForList()
    }

    private fun addWish() {
        clickDescription("Add wish")
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size > LINK_FIELD_INDEX
        }
        rule.onAllNodes(hasSetTextAction())[TITLE_FIELD_INDEX].performTextInput(NEW_WISH_TITLE)
        rule.onAllNodes(hasSetTextAction())[COMMENT_FIELD_INDEX].performTextInput(NEW_WISH_COMMENT)
        rule.onAllNodes(hasSetTextAction())[LINK_FIELD_INDEX].performTextInput(NEW_WISH_LINK)
        clickDescription("Add Link")
        waitForText(NEW_WISH_LINK_HOST)
        clickDescription("Back")

        waitForList()
        // Список держит позицию на прежних элементах, и новое желание может оказаться выше экрана
        scrollListTo(NEW_WISH_TITLE)
        rule.onNodeWithText(NEW_WISH_TITLE).assertIsDisplayed()
        rule.onNodeWithText(NEW_WISH_COMMENT).assertIsDisplayed()
    }

    /**
     * Ссылка из другого приложения: неверная даёт ошибку, верная открывает новое желание
     * со ссылкой в поле ввода.
     */
    private fun receiveSharedLinks() {
        rule.activity.startActivity(sharedTextIntent(INVALID_SHARED_LINK))
        waitForText(string(R.string.invalid_link_error_message))

        rule.activity.startActivity(sharedTextIntent(SHARED_LINK))
        waitForText(SHARED_LINK)
        clickDescription("Add Link")
        waitForText(SHARED_LINK_HOST)
        deleteOpenedWish()
    }

    private fun sharedTextIntent(text: String): Intent =
        Intent(rule.activity, AppActivity::class.java)
            .setAction(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)

    private fun deleteOpenedWish() {
        clickDescription("Menu")
        clickText(string(R.string.delete))
        waitForText(string(R.string.delete_wish_title))
        clickLastText(string(R.string.delete))
        waitForList()
        waitUntilGone(SHARED_LINK_HOST)
    }

    private fun openWishScreens() {
        openFirstWish()
        openImagesViewer()
        openWishMenu()
        createTagFromWish()
        toggleTagOnWish()
        openDeleteLinkDialog()
        clickDescription("Back")

        waitForList()
        scrollListTo(demoData.firstWishTitle)
        waitForText(demoData.newTagTitle)
    }

    /** Заодно проверяет загрузку картинок: пока она не декодирована, её ширина нулевая. */
    private fun openFirstWish() {
        scrollListTo(demoData.firstWishTitle)
        rule.onAllNodesWithText(demoData.firstWishTitle).onFirst().performClick()
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodes(wishImageMatcher).fetchSemanticsNodes().firstOrNull()?.let { it.size.width > 0 } ?: false
        }
    }

    /** Удаляет одно фото из просмотра: у первого демо-желания их несколько. */
    private fun openImagesViewer() {
        val imagesCount = rule.onAllNodes(wishImageMatcher).fetchSemanticsNodes().size
        rule.onAllNodes(wishImageMatcher).onFirst().performClick()
        waitUntilGone(demoData.firstWishTitle)
        clickDescription("Menu")
        clickText(string(R.string.delete))
        waitForText(string(R.string.delete_wish_image_title))
        clickText(string(R.string.cancel))
        waitUntilGone(string(R.string.delete_wish_image_title))

        clickDescription("Menu")
        clickText(string(R.string.delete))
        waitForText(string(R.string.delete_wish_image_title))
        clickLastText(string(R.string.delete))
        waitUntilGone(string(R.string.delete_wish_image_title))
        clickDescription("Back")
        waitForText(demoData.firstWishTitle)
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodes(wishImageMatcher).fetchSemanticsNodes().size == imagesCount - 1
        }
    }

    private fun openWishMenu() {
        clickDescription("Menu")
        clickText(string(R.string.delete))
        waitForText(string(R.string.delete_wish_title))
        clickText(string(R.string.cancel))
        waitUntilGone(string(R.string.delete_wish_title))
    }

    private fun createTagFromWish() {
        clickDescription("Open tags")
        waitForText(demoData.firstTagTitle)
        rule.onNode(hasSetTextAction()).performTextInput(demoData.newTagTitle)
        val createTagText = rule.activity.getString(R.string.create_new_pattern, demoData.newTagTitle)
        waitForText(createTagText)
        clickText(createTagText)
        waitUntilGone(createTagText)
        clickDescription("Back")
        waitForText(demoData.newTagTitle)
    }

    /** Снимает ярлык с желания и возвращает его: на ярлык опирается фильтр дальше. */
    private fun toggleTagOnWish() {
        repeat(2) { round ->
            clickDescription("Open tags")
            waitForText(demoData.newTagTitle)
            clickLastText(demoData.firstTagTitle)
            clickDescription("Back")
            waitForText(demoData.newTagTitle)
            if (round == 0) waitUntilGone(demoData.firstTagTitle) else waitForText(demoData.firstTagTitle)
        }
    }

    private fun openDeleteLinkDialog() {
        rule.onAllNodesWithContentDescription("Delete Link").onFirst().performClick()
        waitForText(string(R.string.delete_wish_link_title))
        clickText(string(R.string.cancel))
        waitUntilGone(string(R.string.delete_wish_link_title))
    }

    private fun editWish() {
        val fillerToEdit = fillerTitle(1)
        scrollListTo(fillerToEdit)
        rule.onAllNodesWithText(fillerToEdit).onFirst().performClick()
        waitForText(string(R.string.enter_link))
        rule.onAllNodes(hasSetTextAction())[TITLE_FIELD_INDEX].performTextReplacement(EDITED_WISH_TITLE)
        rule.onAllNodes(hasSetTextAction())[COMMENT_FIELD_INDEX].performTextInput(EDITED_WISH_COMMENT)
        clickDescription("Back")

        waitForList()
        scrollListTo(EDITED_WISH_TITLE)
        rule.onNodeWithText(EDITED_WISH_TITLE).assertIsDisplayed()
        rule.onNodeWithText(EDITED_WISH_COMMENT).assertIsDisplayed()
        rule.onAllNodesWithText(fillerToEdit).fetchSemanticsNodes().let { check(it.isEmpty()) { "Старое название осталось" } }
    }

    private fun completeWish() {
        scrollListTo(NEW_WISH_TITLE)
        rule.onNodeWithText(NEW_WISH_TITLE).performClick()
        waitForText(string(R.string.enter_link))
        clickDescription("Menu")
        clickText(string(R.string.wish_done))
        waitForText(string(R.string.wish_done_snack_message))
        waitForList()
        waitUntilGone(NEW_WISH_TITLE)
    }

    private fun openEditTags() {
        openNavigationMenu()
        clickText(string(R.string.edit_tags))
        waitForText(demoData.firstTagTitle)

        // В режиме редактирования у ярлыка появляется кнопка удаления
        clickLastText(demoData.firstTagTitle)
        clickDescription("Delete")
        waitForText(string(R.string.delete_tag_title))
        clickText(string(R.string.cancel))
        waitUntilGone(string(R.string.delete_tag_title))
        clickDescription("Done")

        renameAndDeleteTag()

        clickDescription("Back")
        waitForList()
    }

    /** Переименовывает ярлык, созданный на экране желания, и удаляет его. */
    private fun renameAndDeleteTag() {
        waitForText(demoData.newTagTitle)
        clickLastText(demoData.newTagTitle)
        rule.onNode(hasSetTextAction()).performTextReplacement(RENAMED_TAG_TITLE)
        clickDescription("Done")
        waitForText(RENAMED_TAG_TITLE)
        waitUntilGone(demoData.newTagTitle)

        clickLastText(RENAMED_TAG_TITLE)
        clickDescription("Delete")
        waitForText(string(R.string.delete_tag_title))
        clickLastText(string(R.string.delete))
        waitUntilGone(RENAMED_TAG_TITLE)
    }

    private fun filterWishes() {
        openNavigationMenu()
        // Текст ярлыка есть и на карточках под шторкой, нужен верхний
        clickLastText(demoData.firstTagTitle)
        waitForText(demoData.firstWishTitle)
        waitUntilGone(TOP_WISH_TITLE)

        openNavigationMenu()
        clickLastText(string(R.string.completed_wishes))
        waitForText(NEW_WISH_TITLE)
        restoreCompletedWish()

        openNavigationMenu()
        clickLastText(string(R.string.all_wishes))
        waitForList()
        scrollListTo(NEW_WISH_TITLE)
        rule.onNodeWithText(NEW_WISH_TITLE).assertIsDisplayed()
        scrollListToTop()
        waitForText(TOP_WISH_TITLE)
    }

    /** Возвращает выполненное желание в активные, из раздела «Выполненные» оно уходит. */
    private fun restoreCompletedWish() {
        rule.onNodeWithText(NEW_WISH_TITLE).performClick()
        waitForText(string(R.string.enter_link))
        clickDescription("Menu")
        clickText(string(R.string.wish_not_done))
        // В отличие от отметки «Выполнено», возврат в активные оставляет экран желания открытым
        clickDescription("Back")
        // В демо-данных есть и другие выполненные, поэтому раздел не пустеет
        waitForList()
        waitUntilGone(NEW_WISH_TITLE)
    }

    private fun openNavigationMenu() {
        clickDescription("Navigation menu")
        waitForText(string(R.string.edit_tags))
    }

    private fun openSettingsScreens() {
        clickDescription("Settings")
        waitForText(string(R.string.backup))

        clickText(string(R.string.theme_dark))
        clickText(string(R.string.theme_light))
        clickText(string(R.string.theme_system_title))

        clickText(string(R.string.backup))
        waitForText(string(R.string.backup_allow_access_to_drive_text), BACKUP_SIGN_IN_TIMEOUT_MILLIS)
        clickDescription("Backup info")
        waitForText(string(R.string.backup_description))
        device.pressBack()
        waitUntilGone(string(R.string.backup_description))
        clickDescription("Back")
        waitForText(string(R.string.about_app))

        clickText(string(R.string.about_app))
        waitForText(string(R.string.licenses))
        openPrivacyPolicy()
        openLicenses()
        clickDescription("Back")

        waitForText(string(R.string.about_app))
        clickDescription("Back")
        waitForList()
    }

    /** Загрузку самой страницы не проверяем, чтобы тест не зависел от сети. */
    private fun openPrivacyPolicy() {
        clickText(string(R.string.privacy_policy))
        waitUntilGone(string(R.string.licenses))
        clickDescription("Back")
        waitForText(string(R.string.licenses))
    }

    /** Лицензии открываются в отдельной активности, поэтому Compose-правило их не видит. */
    private fun openLicenses() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val licensesMonitor = instrumentation.addMonitor(OssLicensesMenuActivity::class.java.name, null, false)
        clickText(string(R.string.licenses))
        assertNotNull(
            "Экран лицензий не открылся",
            licensesMonitor.waitForActivityWithTimeout(WAIT_TIMEOUT_MILLIS)
        )
        instrumentation.removeMonitor(licensesMonitor)
        device.pressBack()
        waitForText(string(R.string.licenses))
    }

    private fun deleteWishFromList() {
        scrollListToTop()
        waitForText(TOP_WISH_TITLE)
        rule.onAllNodesWithText(TOP_WISH_TITLE).onFirst().performSemanticsAction(SemanticsActions.OnLongClick)
        clickDescription("Delete")
        waitForText(string(R.string.delete_wishes_title))
        clickLastText(string(R.string.delete))
        waitUntilGone(TOP_WISH_TITLE)
    }

    private fun scrollListTo(text: String) {
        rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
        rule.waitForIdle()
    }

    private fun scrollListToTop() {
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        rule.waitForIdle()
    }

    private fun topOf(text: String): Float? =
        rule.onAllNodesWithText(text).fetchSemanticsNodes().firstOrNull()?.boundsInRoot?.top

    /** Список после перестановки может прокрутиться, поэтому каждый раз возвращаемся наверх. */
    private fun waitForOrder(upper: String, lower: String) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            scrollListToTop()
            val upperTop = topOf(upper)
            val lowerTop = topOf(lower)
            upperTop != null && lowerTop != null && upperTop < lowerTop
        }
    }

    /** Кнопка с описанием в той же строке списка, что и текст: у каждой строки свои «Вверх» и «Вниз». */
    private fun clickDescriptionInRowOf(description: String, text: String) {
        val rowCenter = rule.onAllNodesWithText(text).onFirst().fetchSemanticsNode().boundsInRoot.center.y
        val buttons = rule.onAllNodesWithContentDescription(description)
        val index = buttons.fetchSemanticsNodes()
            .withIndex()
            .minBy { (_, node) -> kotlin.math.abs(node.boundsInRoot.center.y - rowCenter) }
            .index
        buttons[index].performClick()
        rule.waitForIdle()
    }

    private fun waitForDescription(description: String) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForList() {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithContentDescription("Add wish").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun string(resId: Int): String = rule.activity.getString(resId)

    private fun clickText(text: String) {
        rule.onNodeWithText(text).performClick()
        rule.waitForIdle()
    }

    /** Текст может быть и на экране, и в открытой шторке поверх него — нужен верхний. */
    private fun clickLastText(text: String) {
        rule.onAllNodesWithText(text).onLast().performClick()
        rule.waitForIdle()
    }

    private fun clickDescription(description: String) {
        rule.onNodeWithContentDescription(description).performClick()
        rule.waitForIdle()
    }

    private fun waitForText(text: String, timeoutMillis: Long = WAIT_TIMEOUT_MILLIS) {
        rule.waitUntil(timeoutMillis) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilGone(text: String) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
        }
    }

    private companion object {

        const val WAIT_TIMEOUT_MILLIS = 10_000L
        const val BACKUP_SIGN_IN_TIMEOUT_MILLIS = 30_000L
        const val FILLER_WISHES_COUNT = 30
        const val TITLE_FIELD_INDEX = 0
        const val COMMENT_FIELD_INDEX = 1
        const val LINK_FIELD_INDEX = 2
        const val NEW_WISH_TITLE = "Smoke test wish"
        const val NEW_WISH_COMMENT = "Smoke test comment"
        const val NEW_WISH_LINK_HOST = "example.com"
        const val NEW_WISH_LINK = "https://$NEW_WISH_LINK_HOST/smoke"
        const val SHARED_LINK_HOST = "example.net"
        const val SHARED_LINK = "https://$SHARED_LINK_HOST/shared"
        const val INVALID_SHARED_LINK = "not a link"
        const val EDITED_WISH_TITLE = "Edited smoke wish"
        const val EDITED_WISH_COMMENT = "Edited smoke comment"
        const val RENAMED_TAG_TITLE = "Renamed smoke tag"
        const val PDF_TIMEOUT_MILLIS = 30_000L
        const val POLL_MILLIS = 200L

        /** Новые желания попадают в начало списка, поэтому сверху последнее добавленное. */
        val TOP_WISH_TITLE = fillerTitle(FILLER_WISHES_COUNT)

        fun fillerTitle(number: Int): String = "Filler wish $number"
    }
}
