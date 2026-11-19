package ru.vitaliy.belyaev.wishapp.wishlist

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.ViewModelProvider
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.UiLook
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsTestTags

/**
 * UI-тест настроек нового вида: вход из «⋮», тема и цвет, строки, выбор вида и «назад».
 * Подготовка и помощники — в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.SettingsScreenTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class SettingsScreenTest : WishListUiTest() {

    @Test
    fun switchesThemeAndColorAndGoesBack() {
        openSettings()
        screenshot("settings")
        rule.onNode(choice(R.string.theme_auto)).assertIsSelected()

        // Тема и цвет хранятся в DataStore и переживают тест: возвращаем их в finally
        try {
            click(choice(R.string.theme_dark))
            rule.onNode(choice(R.string.theme_dark)).assertIsSelected()
            rule.onNode(choice(R.string.theme_auto)).assertIsNotSelected()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                click(choice(R.string.color_wallpaper))
                rule.onNode(choice(R.string.color_wallpaper)).assertIsSelected()
                rule.onNode(choice(R.string.color_persimmon)).assertIsNotSelected()
            } else {
                // Ниже Android 12 цветов из обоев нет, и блок «Цвет» не показывается
                check(!exists(hasText(string(R.string.color_wallpaper))))
            }
            screenshot("dark")
        } finally {
            click(choice(R.string.theme_auto))
            if (exists(choice(R.string.color_persimmon))) click(choice(R.string.color_persimmon))
        }
        rule.onNode(choice(R.string.theme_auto)).assertIsSelected()

        dispatchBack()
        waitUntilExists(tab(R.string.nav_tags))
    }

    @Test
    fun rowsOpenTheirScreens() {
        openSettings()
        listOf(R.string.backup_title, R.string.tags_manage_title, R.string.about_app).forEach { row ->
            scrollTo(row(row))
        }
        // «Оценить» и «Поделиться с друзьями» уходят из приложения, их здесь не нажимаем
        scrollTo(row(R.string.settings_rate))
        scrollTo(row(R.string.settings_share))

        click(row(R.string.tags_manage_title))
        waitUntilGone(hasTestTag(SettingsTestTags.CONTENT))
        dispatchBack()
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
    }

    @Test
    fun classicLookSwitchesWholeApp() {
        openSettings()
        rule.onNode(hasTestTag(SettingsTestTags.LOOK_NEW)).assertIsSelected()
        rule.onNode(hasTestTag(SettingsTestTags.LOOK_CLASSIC)).assertIsNotSelected()
        try {
            click(hasTestTag(SettingsTestTags.LOOK_CLASSIC))
            // Прежний вид начинается со списка: ни настроек, ни дока нового вида
            waitUntilGone(hasTestTag(SettingsTestTags.CONTENT))
            waitUntilGone(tab(R.string.nav_tags))
            screenshot("classic")
        } finally {
            rule.runOnUiThread {
                ViewModelProvider(rule.activity)[AppActivityViewModel::class.java].onUiLookChosen(UiLook.NEW)
            }
        }
        waitUntilExists(tab(R.string.nav_tags))
    }

    private fun openSettings() {
        openMenu()
        click(menuItem(R.string.settings))
        waitUntilExists(heading(string(R.string.settings)))
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
        // Тема читается из DataStore: до этого блоки выбора не рисуются
        waitUntilExists(choice(R.string.theme_auto))
    }

    private fun scrollTo(matcher: SemanticsMatcher) {
        rule.onNode(hasTestTag(SettingsTestTags.CONTENT)).performScrollToNode(matcher)
    }

    private fun choice(@StringRes label: Int): SemanticsMatcher = isSelectable() and hasText(string(label))

    private fun row(@StringRes title: Int): SemanticsMatcher =
        hasAnyAncestor(hasTestTag(SettingsTestTags.CONTENT)) and hasText(string(title)) and hasClickAction()
}
