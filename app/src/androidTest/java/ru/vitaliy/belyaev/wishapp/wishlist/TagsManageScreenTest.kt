package ru.vitaliy.belyaev.wishapp.wishlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsTestTags
import ru.vitaliy.belyaev.wishapp.ui.screens.tags.TagsManageTestTags

/**
 * UI-тест управления ярлыками нового вида: вход из настроек и из шторки ярлыков, создание из поиска,
 * переименование на месте, дубль названия, цвет, удаление с «Вернуть» и без. Подготовка и помощники —
 * в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.TagsManageScreenTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class TagsManageScreenTest : WishListUiTest() {

    @Test
    fun opensFromSettingsAndFromTagsSheet() {
        openMenu()
        click(menuItem(R.string.settings))
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
        click(hasAnyAncestor(hasTestTag(SettingsTestTags.CONTENT)) and hasText(string(R.string.tags_manage_title)))
        waitUntilExists(heading(string(R.string.tags_manage_title)))
        waitUntilExists(tagName(fillerTagTitle(1)))
        screenshot("tags_manage")
        dispatchBack()
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
        dispatchBack()

        click(tab(R.string.nav_tags))
        click(hasText(string(R.string.tags_manage)) and hasClickAction())
        waitUntilExists(heading(string(R.string.tags_manage_title)))
        dispatchBack()
        waitUntilExists(tab(R.string.nav_tags))
    }

    @Test
    fun createsRenamesAndRecolorsTag() {
        openManage()
        search().performTextInput(NEW_TAG)
        click(hasText(string(R.string.tag_create, NEW_TAG)) and hasClickAction())
        // Поиск очистился, видны все ярлыки
        waitUntilExists(tagName(fillerTagTitle(1)))
        // Клавиатура уменьшает список: новый ярлык может оказаться ниже видимой части
        scrollTo(NEW_TAG)
        waitUntilExists(tagName(NEW_TAG))

        click(tagName(NEW_TAG))
        waitUntilExists(hasTestTag(TagsManageTestTags.NAME_FIELD))
        rule.onNode(hasTestTag(TagsManageTestTags.NAME_FIELD)).performTextReplacement(RENAMED_TAG)
        click(hasContentDescription(string(R.string.done)) and hasClickAction())
        waitUntilExists(tagName(RENAMED_TAG))
        check(!exists(tagName(NEW_TAG)))

        // Дубль названия не проходит: снэкбар, название прежнее
        click(tagName(RENAMED_TAG))
        rule.onNode(hasTestTag(TagsManageTestTags.NAME_FIELD)).performTextReplacement(fillerTagTitle(1).uppercase())
        click(hasContentDescription(string(R.string.done)) and hasClickAction())
        waitUntilExists(hasText(string(R.string.tag_exists)))
        waitUntilExists(tagName(RENAMED_TAG))

        click(hasContentDescription(string(R.string.cd_tag_color, RENAMED_TAG)))
        // Новый ярлык получил самый редкий цвет, берём следующий за ним
        val current = checkNotNull(tag(RENAMED_TAG)).color
        val next = TagColor.entries[(current.ordinal + 1) % TagColor.entries.size]
        rule.onNode(swatch(current.ordinal + 1)).assertIsSelected()
        rule.onNode(swatch(next.ordinal + 1)).assertIsNotSelected()
        click(swatch(next.ordinal + 1))
        rule.onNode(swatch(next.ordinal + 1)).assertIsSelected()
        screenshot("colors")
        waitUntil { tag(RENAMED_TAG)?.color == next }
    }

    @Test
    fun deletesWithUndoAndThenForReal() {
        openManage()
        val title = fillerTagTitle(2)
        waitUntilExists(tagName(title))

        click(deleteButton(title))
        waitUntilGone(tagName(title))
        click(hasText(string(R.string.snack_undo)) and hasClickAction())
        waitUntilExists(tagName(title))
        checkNotNull(tag(title))

        click(deleteButton(title))
        waitUntilGone(tagName(title))
        waitUntilExists(hasText(string(R.string.snack_tag_deleted, title)))
        // Без «Вернуть» снэкбар уходит через 6 секунд, и ярлык удаляется из базы
        waitUntil(timeoutMillis = DELETE_TIMEOUT_MILLIS) { tag(title) == null }
        check(!exists(tagName(title)))
    }

    private fun openManage() {
        openMenu()
        click(menuItem(R.string.settings))
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
        click(hasAnyAncestor(hasTestTag(SettingsTestTags.CONTENT)) and hasText(string(R.string.tags_manage_title)))
        waitUntilExists(heading(string(R.string.tags_manage_title)))
    }

    private fun scrollTo(title: String) {
        rule.waitUntil(WAIT_MILLIS) {
            runCatching {
                rule.onNode(hasTestTag(TagsManageTestTags.LIST)).performScrollToNode(hasText(title))
            }.isSuccess
        }
    }

    private fun search() = rule.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(TagsManageTestTags.SEARCH)))

    private fun tagName(title: String): SemanticsMatcher =
        hasAnyAncestor(hasTestTag(TagsManageTestTags.LIST)) and hasText(title) and hasClickAction()

    private fun deleteButton(title: String): SemanticsMatcher =
        hasContentDescription(string(R.string.cd_delete_tag, title)) and hasClickAction()

    private fun swatch(position: Int): SemanticsMatcher =
        hasContentDescription(string(R.string.a11y_color_position, position, TagColor.entries.size))

    private fun tag(title: String) = runBlocking {
        WishAppSdk(DatabaseDriverFactory(targetContext)).getDatabaseRepository().getAllTags()
            .firstOrNull { it.title == title }
    }

    private fun waitUntil(timeoutMillis: Long = WAIT_MILLIS, condition: () -> Boolean) {
        rule.waitUntil(timeoutMillis, condition)
    }

    private companion object {
        const val NEW_TAG = "Garden"
        const val RENAMED_TAG = "Backyard"
        const val WAIT_MILLIS = 5_000L
        const val DELETE_TIMEOUT_MILLIS = 15_000L
    }
}
