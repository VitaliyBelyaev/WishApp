package ru.vitaliy.belyaev.wishapp.wishlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTextReplacement
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.wish.WishScreenTestTags

/**
 * UI-тест экрана хотелки нового вида: правка на месте без «Сохранить», «Сбылось» и «Удалить»
 * без диалогов с «Вернуть» на списке, пустая хотелка при выходе, ярлык с отменой. Подготовка
 * и помощники — в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.WishScreenTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class WishScreenTest : WishListUiTest() {

    @Test
    fun titleSavesItselfAndBackReturnsToList() {
        openWish(TOP_WISH)
        screenshot("wish")
        rule.onNode(screen(WishScreenTestTags.TITLE)).performTextReplacement(NEW_WISH)

        dispatchBack()
        waitUntilGone(screen(WishScreenTestTags.SCREEN))
        waitUntilFirstWish(NEW_WISH)
    }

    @Test
    fun cameTrueLeavesListWithUndo() {
        openWish(TOP_WISH)
        click(screen(WishScreenTestTags.CAME_TRUE))

        waitUntilGone(screen(WishScreenTestTags.SCREEN))
        waitUntilExists(hasText(string(R.string.snack_came_true, TOP_WISH)))
        rule.onNode(wish(TOP_WISH)).assertDoesNotExist()
        screenshot("came_true")

        click(undo())
        waitUntilFirstWish(TOP_WISH)
    }

    @Test
    fun deleteFromMenuHasNoDialogAndCanBeUndone() {
        openWish(TOP_WISH)
        click(screen(WishScreenTestTags.MENU))
        waitUntilExists(menuItem(R.string.wish_share))
        click(menuItem(R.string.delete))

        waitUntilGone(screen(WishScreenTestTags.SCREEN))
        waitUntilExists(hasText(string(R.string.snack_deleted, TOP_WISH)))
        rule.onNode(wish(TOP_WISH)).assertDoesNotExist()

        click(undo())
        waitUntilFirstWish(TOP_WISH)
    }

    @Test
    fun emptiedWishIsDeletedOnExit() {
        openWish(TOP_WISH)
        rule.onNode(screen(WishScreenTestTags.TITLE)).performTextReplacement("")

        dispatchBack()
        waitUntilGone(screen(WishScreenTestTags.SCREEN))
        waitUntilExists(hasText(string(R.string.snack_empty_wish_deleted)))
        rule.onNode(wish(TOP_WISH)).assertDoesNotExist()
    }

    @Test
    fun doneWishTagRemovedWithUndo() {
        click(tab(R.string.nav_done))
        waitUntilExists(wish(DONE_WISH_SPEAKER))
        openWish(DONE_WISH_SPEAKER)
        // У сбывшейся вместо «Сбылось» — «Снова хочу»
        rule.onNode(screen(WishScreenTestTags.WISH_AGAIN)).assertExists()
        rule.onNode(screen(WishScreenTestTags.CAME_TRUE)).assertDoesNotExist()

        click(tagChip(TAG_ELECTRONICS))
        waitUntilExists(hasText(string(R.string.tag_shown)))
        click(hasText(string(R.string.tag_remove)) and hasClickAction())

        waitUntilExists(hasText(string(R.string.snack_tag_removed, TAG_ELECTRONICS)))
        waitUntilGone(tagChip(TAG_ELECTRONICS))
        click(undo())
        waitUntilExists(tagChip(TAG_ELECTRONICS))
    }

    private fun openWish(title: String) {
        click(wish(title))
        waitUntilExists(screen(WishScreenTestTags.TITLE))
    }

    private fun screen(tag: String): SemanticsMatcher = hasTestTag(tag)

    private fun tagChip(title: String): SemanticsMatcher =
        hasText(title) and hasClickAction() and hasAnyAncestor(hasTestTag(WishScreenTestTags.SCREEN))

    private fun undo(): SemanticsMatcher = hasText(string(R.string.snack_undo)) and hasClickAction()

    private companion object {
        const val TAG_ELECTRONICS = "Electronics"
    }
}
