package ru.vitaliy.belyaev.wishapp.wishlist

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.NewWishTestTags

/**
 * UI-тест панели новой хотелки на экране списка: создание кнопкой-галочкой и галочкой клавиатуры
 * (панель ждёт следующую), черновик после закрытия и «Вернуться», ярлык фильтра, поле ссылки,
 * создание ярлыка в выборе ярлыков. Подготовка и помощники — в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.NewWishSheetTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class NewWishSheetTest : WishListUiTest() {

    @Test
    fun plusOpensNewWishSheetAndCheckCreatesWishFirst() {
        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.SHEET))
        // Пока пусто, галочка недоступна: ярлык или пробелы хотелкой не считаются
        rule.onNode(newWish(NewWishTestTags.CREATE)).assertIsNotEnabled()
        rule.onNode(newWish(NewWishTestTags.TITLE)).performTextInput(NEW_WISH)
        // Новую хотелку не чистят: «Очистить» есть только у открытого заново черновика
        rule.onNode(newWish(NewWishTestTags.CLEAR)).assertDoesNotExist()
        screenshot("sheet")

        click(newWish(NewWishTestTags.CREATE))
        waitUntilGone(newWish(NewWishTestTags.SHEET))
        waitUntilFirstWish(NEW_WISH)
    }

    @Test
    fun keyboardDoneCreatesAndKeepsPanelForNextWish() {
        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.TITLE))
        rule.onNode(newWish(NewWishTestTags.TITLE)).performTextInput(NEW_WISH)
        rule.onNode(newWish(NewWishTestTags.TITLE)).performImeAction()

        // Хотелка уже в списке, а панель ждёт следующую с пустым названием
        waitUntilExists(wish(NEW_WISH))
        rule.onNode(newWish(NewWishTestTags.SHEET)).assertExists()
        assertTitle("")
        rule.onNode(newWish(NewWishTestTags.TITLE)).performTextInput(NEXT_WISH)
        click(newWish(NewWishTestTags.CREATE))
        waitUntilGone(newWish(NewWishTestTags.SHEET))
        waitUntilFirstWish(NEXT_WISH)
    }

    @Test
    fun closingWithoutCheckKeepsDraftAndGoBackReturnsIt() {
        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.TITLE))
        rule.onNode(newWish(NewWishTestTags.TITLE)).performTextInput(NEW_WISH)
        // Системное «назад» закрывает клавиатуру, второе — шторку
        device.pressBack()
        rule.waitForIdle()
        if (exists(newWish(NewWishTestTags.SHEET))) device.pressBack()
        waitUntilGone(newWish(NewWishTestTags.SHEET))

        waitUntilExists(hasText(string(R.string.snack_not_created)))
        rule.onNode(wish(NEW_WISH)).assertDoesNotExist()
        screenshot("not_created")
        click(hasText(string(R.string.snack_go_back)))
        waitUntilExists(newWish(NewWishTestTags.TITLE))
        assertTitle(NEW_WISH)
        // Курсор в конце названия: можно сразу дописывать
        rule.onNode(newWish(NewWishTestTags.TITLE))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange, TextRange(NEW_WISH.length)))
        screenshot("draft_returned")

        // «Очистить» начинает заново. Нажатие действием: панель в этот момент едет вместе с клавиатурой
        rule.onNode(newWish(NewWishTestTags.CLEAR)).performSemanticsAction(SemanticsActions.OnClick)
        rule.waitForIdle()
        assertTitle("")
    }

    @Test
    fun plusInsideTagStartsWithThatTag() {
        click(tab(R.string.nav_tags))
        waitUntilExists(tagRow(TAG_BOOKS))
        click(tagRow(TAG_BOOKS))
        waitUntilExists(heading(TAG_BOOKS))

        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.TAG_CHIP))
        rule.onNode(
            newWish(NewWishTestTags.TAG_CHIP),
            useUnmergedTree = true,
        ).assert(hasAnyDescendant(hasText(TAG_BOOKS)))
        rule.onNode(newWish(NewWishTestTags.TITLE)).performTextInput(NEW_WISH)
        click(newWish(NewWishTestTags.CREATE))

        // Карточка видна в текущем фильтре, потому что у неё этот ярлык
        waitUntilFirstWish(NEW_WISH)
        rule.onNode(heading(TAG_BOOKS)).assertExists()
    }

    @Test
    fun linkFieldTakesBareDomainAndShowsErrorForText() {
        // Со ссылкой в буфере вместо «Ссылка» был бы чип «Вставить ссылку»
        rule.runOnUiThread {
            rule.activity.getSystemService(ClipboardManager::class.java)
                .setPrimaryClip(ClipData.newPlainText(null, NOT_A_LINK))
        }
        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.LINK_CHIP))
        click(newWish(NewWishTestTags.LINK_CHIP))
        waitUntilExists(newWish(NewWishTestTags.LINK_FIELD))

        rule.onNode(newWish(NewWishTestTags.LINK_FIELD)).performTextInput(NOT_A_LINK)
        rule.onNode(newWish(NewWishTestTags.LINK_FIELD)).performImeAction()
        waitUntilExists(hasText(string(R.string.link_invalid)))

        rule.onNode(newWish(NewWishTestTags.LINK_FIELD)).performTextReplacement(BARE_LINK)
        rule.onNode(newWish(NewWishTestTags.LINK_FIELD)).performImeAction()
        waitUntilGone(newWish(NewWishTestTags.LINK_FIELD))
        waitUntilExists(hasText(BARE_LINK_HOST))
        screenshot("link_added")

        // Только ссылка: этого достаточно, название можно не писать
        click(newWish(NewWishTestTags.CREATE))
        waitUntilGone(newWish(NewWishTestTags.SHEET))
    }

    @Test
    fun tagPickerCreatesTagAndPutsItOnCard() {
        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.TAG_CHIP))
        click(newWish(NewWishTestTags.TAG_CHIP))
        waitUntilExists(newWish(NewWishTestTags.TAG_PICKER))
        rule.onNode(hasSetTextAction() and hasAnyAncestor(hasAnyDescendant(newWish(NewWishTestTags.TAG_PICKER))))
            .performTextInput(NEW_TAG)
        // Нажатие действием, а не пальцем: пока выезжает клавиатура, строка может сдвинуться
        waitUntilExists(hasText(string(R.string.tag_create, NEW_TAG)))
        rule.onNode(hasText(string(R.string.tag_create, NEW_TAG)) and hasClickAction())
            .performSemanticsAction(SemanticsActions.OnClick)
        waitUntilExists(hasText(string(R.string.tag_on_card)))
        screenshot("tag_picker")
        click(hasText(string(R.string.done)))
        waitUntilGone(newWish(NewWishTestTags.TAG_PICKER))

        rule.onNode(
            newWish(NewWishTestTags.TAG_CHIP),
            useUnmergedTree = true,
        ).assert(hasAnyDescendant(hasText(NEW_TAG)))
    }

    /** Только введённый текст: подсказка пустого поля тоже лежит в его семантике. */
    private fun assertTitle(text: String) {
        rule.onNode(newWish(NewWishTestTags.TITLE))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text)))
    }
}
