package ru.vitaliy.belyaev.wishapp.smoke

import android.content.Context
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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
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
 * Быстрая проверка, что приложение запускается и все экраны открываются: один успешный
 * сценарий без разбора всех состояний. Нужна после обновления зависимостей. Запускается через `scripts/run_smoke_test.sh`.
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
                Firebase.analytics.setAnalyticsCollectionEnabled(false)
                Firebase.crashlytics.isCrashlyticsCollectionEnabled = false
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
        openReorderMode()
        openShareSheet()
        addWish()
        openWishScreens()
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

    private fun openReorderMode() {
        clickDescription("Reorder wishes")
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithContentDescription("Move down").fetchSemanticsNodes().isNotEmpty()
        }
        clickDescription("Reorder wishes")
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodesWithContentDescription("Move down").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun openShareSheet() {
        clickDescription("Share wishes")
        waitForText(string(R.string.share_title))
        rule.onNodeWithText(string(R.string.share_type_text_button_text)).assertIsDisplayed()
        rule.onNodeWithText(string(R.string.share_type_pdf_button_text)).assertIsDisplayed()
        device.pressBack()
        waitUntilGone(string(R.string.share_title))
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

    private fun openWishScreens() {
        openFirstWish()
        openImagesViewer()
        openWishMenu()
        createTagFromWish()
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

    private fun openImagesViewer() {
        rule.onAllNodes(wishImageMatcher).onFirst().performClick()
        waitUntilGone(demoData.firstWishTitle)
        clickDescription("Menu")
        clickText(string(R.string.delete))
        waitForText(string(R.string.delete_wish_image_title))
        clickText(string(R.string.cancel))
        clickDescription("Back")
        waitForText(demoData.firstWishTitle)
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

    private fun openDeleteLinkDialog() {
        rule.onAllNodesWithContentDescription("Delete Link").onFirst().performClick()
        waitForText(string(R.string.delete_wish_link_title))
        clickText(string(R.string.cancel))
        waitUntilGone(string(R.string.delete_wish_link_title))
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

        clickDescription("Back")
        waitForList()
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

        openNavigationMenu()
        clickLastText(string(R.string.all_wishes))
        waitUntilGone(NEW_WISH_TITLE)
        scrollListToTop()
        waitForText(TOP_WISH_TITLE)
    }

    private fun openNavigationMenu() {
        clickDescription("Navigation menu")
        waitForText(string(R.string.edit_tags))
    }

    private fun openSettingsScreens() {
        clickDescription("Settings")
        waitForText(string(R.string.backup))

        clickText(string(R.string.theme_dark))
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

        /** Новые желания попадают в начало списка, поэтому сверху последнее добавленное. */
        val TOP_WISH_TITLE = fillerTitle(FILLER_WISHES_COUNT)

        fun fillerTitle(number: Int): String = "Filler wish $number"
    }
}
