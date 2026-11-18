package ru.vitaliy.belyaev.wishapp.wishlist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
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
    fun linkFieldTakesBareDomainAndClearsText() {
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

        // Крестик очищает поле, вместо него снова «Вставить»
        click(newWish(NewWishTestTags.LINK_CLEAR))
        waitUntilGone(newWish(NewWishTestTags.LINK_CLEAR))
        rule.onNode(newWish(NewWishTestTags.LINK_FIELD))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
        rule.onNode(hasText(string(R.string.link_invalid))).assertDoesNotExist()
        rule.onNode(hasText(string(R.string.link_paste))).assertExists()

        rule.onNode(newWish(NewWishTestTags.LINK_FIELD)).performTextInput(BARE_LINK)
        rule.onNode(newWish(NewWishTestTags.LINK_FIELD)).performImeAction()
        waitUntilGone(newWish(NewWishTestTags.LINK_FIELD))
        waitUntilExists(hasText(BARE_LINK_HOST))
        screenshot("link_added")

        // Только ссылка: этого достаточно, название можно не писать
        click(newWish(NewWishTestTags.CREATE))
        waitUntilGone(newWish(NewWishTestTags.SHEET))
    }

    @Test
    fun textInLinkFieldDoesNotBlockCreating() {
        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.LINK_CHIP))
        click(newWish(NewWishTestTags.LINK_CHIP))
        waitUntilExists(newWish(NewWishTestTags.LINK_FIELD))
        rule.onNode(newWish(NewWishTestTags.LINK_FIELD)).performTextInput(NOT_A_LINK)
        rule.onNode(newWish(NewWishTestTags.TITLE)).performTextInput(NEW_WISH)

        // Не ссылка подсказкой не мешает: хотелка создаётся, текст сохраняется ссылкой как есть
        click(newWish(NewWishTestTags.CREATE))
        waitUntilGone(newWish(NewWishTestTags.SHEET))
        waitUntilFirstWish(NEW_WISH)
    }

    @Test
    fun sharedTextGoesIntoOpenPanel() {
        click(addButton())
        waitUntilExists(newWish(NewWishTestTags.TITLE))
        rule.onNode(newWish(NewWishTestTags.TITLE)).performTextInput(NEW_WISH)
        share(
            Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "$SHARED_TITLE\n$SHARED_LINK"),
        )

        // Человек ходил за ссылкой: она добавилась, написанное им название осталось
        waitUntilExists(hasText(SHARED_LINK_HOST, substring = true) and hasAnyAncestor(newWish(NewWishTestTags.SHEET)))
        assertTitle(NEW_WISH)
        screenshot("shared_into_open_panel")
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

    @Test
    fun sharedTextReturnsToListAndOpensPanelWithTitleAndLink() {
        // Открытая хотелка закрывается: шторка встаёт поверх списка
        click(wish(TOP_WISH))
        waitUntilGone(addButton())
        share(
            Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "$SHARED_TITLE\n$SHARED_LINK"),
        )

        waitUntilExists(newWish(NewWishTestTags.SHEET))
        assertTitle(SHARED_TITLE)
        rule.onNode(hasText(SHARED_LINK_HOST, substring = true) and hasAnyAncestor(newWish(NewWishTestTags.SHEET)))
            .assertExists()
        rule.onNode(addButton()).assertExists()
        // Присланное — новая хотелка, а не открытый заново черновик: «Очистить» нет
        rule.onNode(newWish(NewWishTestTags.CLEAR)).assertDoesNotExist()
        screenshot("shared_text")

        click(newWish(NewWishTestTags.CREATE))
        waitUntilGone(newWish(NewWishTestTags.SHEET))
        waitUntilFirstWish(SHARED_TITLE)
    }

    @Test
    fun sharedImagesTakeFirstFiveAndUnreadableOnesShowMessage() {
        val images = insertImages(SHARED_IMAGES_COUNT)
        try {
            share(
                Intent(Intent.ACTION_SEND_MULTIPLE)
                    .setType("image/jpeg")
                    .putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            )
            // Только фото: название пустое, галочка доступна
            waitUntilExists(hasText(string(R.string.chip_photo_count, MAX_PHOTOS)))
            waitUntilExists(hasText(string(R.string.snack_photos_added_partly, MAX_PHOTOS, SHARED_IMAGES_COUNT)))
            assertTitle("")
            rule.onNode(newWish(NewWishTestTags.CREATE)).assertIsEnabled()
            screenshot("shared_images")
            click(newWish(NewWishTestTags.CREATE))
            waitUntilGone(newWish(NewWishTestTags.SHEET))

            // Ни одна картинка не прочиталась: шторки нет, только сообщение. Провайдера нет, поэтому и
            // выдавать доступ не к чему: адрес галереи система без разрешения не передаст
            val missing = Uri.parse(MISSING_IMAGE_URI)
            share(Intent(Intent.ACTION_SEND).setType("image/jpeg").putExtra(Intent.EXTRA_STREAM, missing))
            waitUntilExists(hasText(string(R.string.photo_add_failed)))
            rule.onNode(newWish(NewWishTestTags.SHEET)).assertDoesNotExist()
        } finally {
            images.forEach { targetContext.contentResolver.delete(it, null, null) }
        }
    }

    /** Как «Поделиться» из другого приложения: активити уже открыта, придёт `onNewIntent`. */
    private fun share(intent: Intent) {
        targetContext.startActivity(
            intent.setClass(targetContext, AppActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        rule.waitForIdle()
    }

    /** Картинки в галерее эмулятора от имени приложения: свои записи читаются без разрешений. */
    private fun insertImages(count: Int): List<Uri> = (1..count).map { number ->
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "wishapp_share_test_$number.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val resolver = targetContext.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        requireNotNull(resolver.openOutputStream(uri)).use { stream ->
            Bitmap.createBitmap(TEST_IMAGE_SIDE, TEST_IMAGE_SIDE, Bitmap.Config.ARGB_8888)
                .apply { eraseColor(Color.rgb(number * COLOR_STEP, COLOR_STEP, COLOR_STEP)) }
                .compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
        }
        uri
    }

    /** Только введённый текст: подсказка пустого поля тоже лежит в его семантике. */
    private fun assertTitle(text: String) {
        rule.onNode(newWish(NewWishTestTags.TITLE))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text)))
    }

    private companion object {
        const val SHARED_TITLE = "Linen shirt"
        const val SHARED_LINK = "https://shop.example.com/item/42"
        const val SHARED_LINK_HOST = "shop.example.com"
        const val SHARED_IMAGES_COUNT = 7
        const val MAX_PHOTOS = 5
        const val MISSING_IMAGE_URI = "content://com.example.missing/image.jpg"
        const val TEST_IMAGE_SIDE = 64
        const val COLOR_STEP = 30
        const val JPEG_QUALITY = 90
    }
}
