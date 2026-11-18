package ru.vitaliy.belyaev.wishapp.wishlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishListTestTags

/**
 * UI-тест экрана списка в новом виде: вкладки панели, шторки ярлыков и сортировки, меню «⋮»,
 * поиск, вид карточками и списком, прокрутка и системное «назад», режим выбора с массовыми действиями
 * и «Вернуть», перенос пальцем и действиями TalkBack «Выше» и «Ниже». Подготовка и помощники —
 * в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.WishListScreenTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class WishListScreenTest : WishListUiTest() {

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
    fun scrollIsKeptPerTabAndTappingSelectedTabReturnsToTop() {
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

        // Другой раздел открывается со своей прокрутки, впервые с начала,
        // а «Хотелки» при возврате там, где их оставили
        rule.onNode(list()).performScrollToIndex(ACTIVE_COUNT - 1)
        waitUntilExists(wish(LAST_WISH))
        click(tab(R.string.nav_done))
        waitUntilExists(wish(DONE_WISH_SPEAKER))
        click(tab(R.string.nav_wishes))
        waitUntilExists(wish(LAST_WISH))
        rule.onNode(wish(TOP_WISH)).assertDoesNotExist()
        screenshot("restored_after_done")

        // Нажатие на открытую вкладку после возврата по-прежнему поднимает список
        click(tab(R.string.nav_wishes))
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            exists(wish(TOP_WISH)) && kotlin.math.abs(boundsOf(wish(TOP_WISH)).top - topAtStart) < 1f
        }
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
        rule.onNode(hasText(string(R.string.share_as_image))).assertIsDisplayed()
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

    @Test
    fun pickPartOpensSelectionAndShareFromDockRisesAboveIt() {
        // Из «⋮» меню выпадает сверху, в нём есть «Выбрать только часть»
        openMenu()
        click(menuItem(R.string.menu_share))
        waitUntilExists(hasTestTag(WishListTestTags.SHARE_MENU))
        assertTrue("Меню из шапки должно быть сверху", shareMenuBounds().bottom < rootHeight() / 2)
        click(menuItem(R.string.share_pick_part))

        // Выбор со всеми отмеченными, меню закрыто
        waitUntilExists(selectionCount(ACTIVE_COUNT))
        waitUntilGone(hasTestTag(WishListTestTags.SHARE_MENU))

        // «Поделиться» в панели выбора: меню над панелью, без пункта выбора
        click(hasContentDescription(string(R.string.menu_share)) and hasClickAction())
        waitUntilExists(hasText(string(R.string.selection_count, ACTIVE_COUNT)) and hasAnyAncestor(shareMenu()))
        assertTrue("Меню из панели выбора должно быть снизу", shareMenuBounds().top > rootHeight() / 2)
        rule.onNode(hasText(string(R.string.share_pick_part))).assertDoesNotExist()
        rule.onNode(hasText(string(R.string.share_as_text))).assertIsDisplayed()
        screenshot("share_from_selection")
        device.pressBack()
        waitUntilGone(hasTestTag(WishListTestTags.SHARE_MENU))
        // «Назад» закрыло только меню, выбор остался
        rule.onNode(selectionCount(ACTIVE_COUNT)).assertExists()
    }

    private fun shareMenu() = hasTestTag(WishListTestTags.SHARE_MENU)

    private fun shareMenuBounds() = rule.onNode(shareMenu()).fetchSemanticsNode().boundsInRoot

    private fun rootHeight() = rule.onRoot().fetchSemanticsNode().size.height

    @Test
    fun longPressInPlaceSelectsAndSelectionHeaderWorks() {
        longPressInPlace(wish(TOP_WISH))
        waitUntilExists(selectionCount(1))
        // Панель стала действиями: плюса и вкладок нет
        rule.onNode(hasContentDescription(string(R.string.cd_add_wish))).assertDoesNotExist()
        rule.onNode(tab(R.string.nav_wishes)).assertDoesNotExist()
        rule.onNode(hasTestTag(WishListTestTags.SELECTION_DONE)).assertIsDisplayed()
        screenshot("selected")

        // В режиме обычное нажатие переключает выбор, а не открывает хотелку
        click(wish(fillerTitle(FILLER_WISHES_COUNT - 1)))
        waitUntilExists(selectionCount(2))
        click(wish(fillerTitle(FILLER_WISHES_COUNT - 1)))
        waitUntilExists(selectionCount(1))

        click(hasText(string(R.string.selection_all)) and hasClickAction())
        waitUntilExists(selectionCount(ACTIVE_COUNT))

        // «Назад» сначала снимает выбор и остаётся в «Хотелках»
        device.pressBack()
        waitUntilGone(hasTestTag(WishListTestTags.SELECTION_COUNT))
        rule.onNode(tab(R.string.nav_wishes)).assertIsSelected()

        // Снятый последний выбор закрывает режим
        longPressInPlace(wish(TOP_WISH))
        waitUntilExists(selectionCount(1))
        click(wish(TOP_WISH))
        waitUntilGone(hasTestTag(WishListTestTags.SELECTION_COUNT))
    }

    @Test
    fun dragMovesWishAndKeepsOrder() {
        val second = fillerTitle(FILLER_WISHES_COUNT - 1)
        val third = fillerTitle(FILLER_WISHES_COUNT - 2)
        // Первая карточка в верхнем ряду слева, третья под ней: тащим первую на место третьей
        dragTo(wish(TOP_WISH), boundsOf(wish(third)).center + Offset(0f, boundsOf(wish(third)).height / 2))
        waitUntilFirstWish(second)
        // Перенос не выделяет карточку
        rule.onNode(hasTestTag(WishListTestTags.SELECTION_COUNT)).assertDoesNotExist()
        screenshot("dragged")

        // Порядок записан в базу: после смены раздела он тот же
        click(tab(R.string.nav_done))
        waitUntilExists(heading(string(R.string.nav_done)))
        click(tab(R.string.nav_wishes))
        waitUntilFirstWish(second)
    }

    @Test
    fun talkBackMovesWishWithCustomActions() {
        val second = fillerTitle(FILLER_WISHES_COUNT - 1)
        // У первой нет «Выше»
        assertTrue(customActionLabels(wish(TOP_WISH)).none { it == string(R.string.a11y_move_up) })
        performCustomAction(wish(TOP_WISH), R.string.a11y_move_down)
        waitUntilFirstWish(second)
        performCustomAction(wish(TOP_WISH), R.string.a11y_move_up)
        waitUntilFirstWish(TOP_WISH)

        // «Выбрать несколько» TalkBack по-прежнему выбирает, хотя долгое нажатие пальцем ведёт перенос
        rule.onNode(wish(TOP_WISH)).performSemanticsAction(SemanticsActions.OnLongClick)
        waitUntilExists(selectionCount(1))
    }

    @Test
    fun moveInsideTagChangesOnlyTagOrder() {
        click(tab(R.string.nav_tags))
        waitUntilExists(tagRow(TAG_BOOKS))
        click(tagRow(TAG_BOOKS))
        waitUntilExists(heading(TAG_BOOKS))
        val first = if (exists(wish(BOOKS_WISH_NOVEL)) && firstWishDescription()!!.startsWith(BOOKS_WISH_NOVEL)) {
            BOOKS_WISH_NOVEL
        } else {
            BOOKS_WISH_READER
        }
        val second = if (first == BOOKS_WISH_NOVEL) BOOKS_WISH_READER else BOOKS_WISH_NOVEL
        waitUntilFirstWish(first)

        performCustomAction(wish(first), R.string.a11y_move_down)
        waitUntilFirstWish(second)

        // В «Хотелках» чужие хотелки остались на своих местах
        click(tab(R.string.nav_wishes))
        waitUntilFirstWish(TOP_WISH)
    }

    @Test
    fun deleteSelectedHidesWishesAndUndoReturnsThem() {
        longPressInPlace(wish(TOP_WISH))
        waitUntilExists(selectionCount(1))
        click(hasTestTag(WishListTestTags.SELECTION_DELETE))
        waitUntilGone(wish(TOP_WISH))
        rule.onNode(hasTestTag(WishListTestTags.SELECTION_COUNT)).assertDoesNotExist()
        waitUntilExists(hasText(string(R.string.snack_deleted_count, 1)))
        screenshot("deleted")

        click(hasText(string(R.string.snack_undo)) and hasClickAction())
        waitUntilFirstWish(TOP_WISH)
    }

    @Test
    fun cameTrueMovesSelectedToDone() {
        longPressInPlace(wish(TOP_WISH))
        waitUntilExists(selectionCount(1))
        click(hasTestTag(WishListTestTags.SELECTION_DONE))
        waitUntilGone(wish(TOP_WISH))
        waitUntilExists(hasText(string(R.string.snack_came_true_count, 1)))
        // Другое действие подтверждает прошлое: хотелка уходит в «Сбылось»
        click(tab(R.string.nav_done))
        waitUntilExists(heading(string(R.string.nav_done)))
        waitUntilExists(wish(TOP_WISH))

        // В «Сбылось» переноса нет, долгое нажатие сразу выбирает, а действие — «Снова хочу»
        longPressInPlace(wish(TOP_WISH))
        waitUntilExists(selectionCount(1))
        rule.onNode(hasTestTag(WishListTestTags.SELECTION_DONE)).assert(hasText(string(R.string.wish_again)))
    }

    // region Выбор и перенос
}
