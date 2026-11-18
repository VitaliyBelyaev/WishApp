package ru.vitaliy.belyaev.wishapp.wishlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.screenshots.DemoData
import ru.vitaliy.belyaev.wishapp.ui.screens.photos.PhotoViewerTestTags
import ru.vitaliy.belyaev.wishapp.ui.screens.wish.WishScreenTestTags

/**
 * UI-тест просмотра фото нового вида: листание и счётчик, «Сделать главным», удаление с «Вернуть»,
 * нажатие прячет кнопки, свайп вниз закрывает. Подготовка и помощники — в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.PhotoViewerTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class PhotoViewerTest : WishListUiTest() {

    @Test
    fun pagesMakesMainAndDeletesWithUndo() {
        openViewer()
        waitUntilExists(counter(1, PHOTOS))
        screenshot("viewer")

        swipePhoto()
        waitUntilExists(counter(2, PHOTOS))
        rule.onNode(hasTestTag(PhotoViewerTestTags.MAKE_MAIN) and hasText(string(R.string.photo_make_main)))
            .assertExists()

        // Главное фото встаёт первым, на экране остаётся оно же
        click(hasTestTag(PhotoViewerTestTags.MAKE_MAIN))
        waitUntilExists(counter(1, PHOTOS))
        waitUntilExists(hasTestTag(PhotoViewerTestTags.MAKE_MAIN) and hasText(string(R.string.photo_main)))
        waitUntilExists(hasText(string(R.string.snack_main_photo)))
        screenshot("main")

        click(hasTestTag(PhotoViewerTestTags.DELETE))
        waitUntilExists(hasText(string(R.string.snack_photo_deleted)))
        waitUntilExists(counterOf(PHOTOS - 1))
        screenshot("deleted")

        click(hasText(string(R.string.snack_undo)) and hasClickAction())
        waitUntilExists(counterOf(PHOTOS))
    }

    @Test
    fun tapHidesButtonsAndSwipeDownCloses() {
        openViewer()

        // Telephoto принимает нажатия, когда фото загрузилось: первое нажатие может прийти раньше
        tapUntilGone(hasTestTag(PhotoViewerTestTags.BACK))
        screenshot("hidden")
        tapPhoto()
        waitUntilExists(hasTestTag(PhotoViewerTestTags.BACK))

        rule.onNode(hasTestTag(PhotoViewerTestTags.PAGER)).performTouchInput {
            swipeDown(durationMillis = SWIPE_MILLIS)
        }
        waitUntilGone(hasTestTag(PhotoViewerTestTags.SCREEN))
        rule.onNode(hasTestTag(WishScreenTestTags.TITLE)).assertExists()
    }

    private fun openViewer() {
        val title = DemoData(language = "en").firstWishTitle
        rule.onNode(list()).performScrollToNode(wish(title))
        click(wish(title))
        waitUntilExists(hasTestTag(WishScreenTestTags.PHOTO))
        click(hasTestTag(WishScreenTestTags.PHOTO))
        waitUntilExists(hasTestTag(PhotoViewerTestTags.BACK))
    }

    private fun swipePhoto() {
        rule.onNode(hasTestTag(PhotoViewerTestTags.PAGER)).performTouchInput { swipeLeft() }
        rule.waitForIdle()
    }

    private fun tapUntilGone(matcher: SemanticsMatcher) {
        repeat(TAP_ATTEMPTS) {
            tapPhoto()
            // Дольше двойного нажатия, чтобы повтор не стал зумом
            val gone = runCatching { rule.waitUntil(TAP_WAIT_MILLIS) { !exists(matcher) } }.isSuccess
            if (gone) return
        }
        waitUntilGone(matcher)
    }

    private fun tapPhoto() {
        rule.onNode(hasTestTag(PhotoViewerTestTags.PAGER)).performTouchInput { click(center) }
        rule.waitForIdle()
    }

    private fun counter(position: Int, count: Int): SemanticsMatcher =
        hasTestTag(PhotoViewerTestTags.COUNTER) and hasText(string(R.string.photo_counter, position, count))

    /** Счётчик с этим числом фото, на каком фото — неважно. */
    private fun counterOf(
        count: Int,
    ): SemanticsMatcher = (1..count).map { counter(it, count) }.reduce { a, b -> a or b }

    private companion object {
        const val PHOTOS = 3
        const val SWIPE_MILLIS = 250L
        const val TAP_ATTEMPTS = 3
        const val TAP_WAIT_MILLIS = 2_000L
    }
}
