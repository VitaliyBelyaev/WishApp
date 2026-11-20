package ru.vitaliy.belyaev.wishapp.wishlist

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.about.AboutTestTags
import ru.vitaliy.belyaev.wishapp.ui.screens.about.LicensesTestTags
import ru.vitaliy.belyaev.wishapp.ui.screens.about.PolicyTestTags
import ru.vitaliy.belyaev.wishapp.ui.screens.about.TAPS_TO_PLAY
import ru.vitaliy.belyaev.wishapp.ui.screens.about.TAP_GAP_MILLIS
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsTestTags

/**
 * UI-тест «О приложении» нового вида: версия и строки, лицензии со шрифтами, политика. Подготовка и
 * помощники — в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.AboutScreenTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class AboutScreenTest : WishListUiTest() {

    @Test
    fun showsVersionAndOpensLicenses() {
        openAbout()
        val version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        waitUntilExists(hasText(string(R.string.app_version_pattern, version)))
        waitUntilExists(hasTestTag(AboutTestTags.SOURCE_CODE))
        listOf(R.string.feedback, R.string.licenses, R.string.privacy_policy).forEach { scrollTo(row(it)) }
        screenshot("about")

        click(row(R.string.licenses))
        waitUntilExists(hasTestTag(LicensesTestTags.LIST))
        // Лицензии шрифтов дописывает сборка, они есть и в debug, где у библиотек заглушка
        val onest = hasAnyAncestor(hasTestTag(LicensesTestTags.LIST)) and hasText("Onest") and hasClickAction()
        waitUntilExists(onest)
        check(exists(hasText("Unbounded")))
        screenshot("licenses")

        click(onest)
        waitUntilExists(hasTestTag(LicensesTestTags.TEXT))
        waitUntilExists(hasText("SIL OPEN FONT LICENSE", substring = true, ignoreCase = true))
        screenshot("license")

        dispatchBack()
        waitUntilExists(hasTestTag(LicensesTestTags.LIST))
        dispatchBack()
        waitUntilExists(hasTestTag(AboutTestTags.CONTENT))
        dispatchBack()
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
    }

    @Test
    fun fastTapsOnIconPlayEasterEgg() {
        openAbout()
        val icon = rule.onNode(hasTestTag(AboutTestTags.ICON))
        // Медленные нажатия не считаются подряд
        repeat(TAPS_TO_PLAY - 1) { icon.performTouchInput { click() } }
        Thread.sleep(TAP_GAP_MILLIS * 2)
        icon.performTouchInput { click() }
        rule.waitForIdle()
        check(!exists(hasTestTag(AboutTestTags.EASTER_EGG)))

        // Часы теста стоят, иначе ожидание прокрутит всю пасхалку разом
        rule.mainClock.autoAdvance = false
        repeat(TAPS_TO_PLAY) { icon.performTouchInput { click() } }
        rule.mainClock.advanceTimeBy(EGG_MID_MILLIS)
        // Снимок экрана тут бесполезен: при стоящих часах кадр не дорисовывается
        check(exists(hasTestTag(AboutTestTags.EASTER_EGG)))
        rule.mainClock.autoAdvance = true
        rule.waitUntil(EGG_TIMEOUT_MILLIS) { !exists(hasTestTag(AboutTestTags.EASTER_EGG)) }
        waitUntilExists(hasTestTag(AboutTestTags.CONTENT))
    }

    @Test
    fun opensPolicyOrShowsRetry() {
        openAbout()
        scrollTo(row(R.string.privacy_policy))
        click(row(R.string.privacy_policy))
        waitUntilExists(hasTestTag(PolicyTestTags.WEB))
        // Есть ли сеть у эмулятора, тест не знает: ждём конца загрузки, дальше страница или «Повторить»
        rule.waitUntil(POLICY_TIMEOUT_MILLIS) { !exists(hasTestTag(PolicyTestTags.LOADING)) }
        if (exists(hasTestTag(PolicyTestTags.FAILED))) {
            check(exists(hasText(string(R.string.policy_failed_title))))
            screenshot("policy_failed")
            // Без сети «Повторить» снова приводит к ошибке, а не к странице ошибки WebView
            click(hasText(string(R.string.retry)) and hasClickAction())
            rule.waitUntil(POLICY_TIMEOUT_MILLIS) {
                !exists(hasTestTag(PolicyTestTags.LOADING)) && exists(hasTestTag(PolicyTestTags.FAILED))
            }
        }
        // Первый кадр страницы WebView рисует уже после конца загрузки
        Thread.sleep(PAGE_DRAW_MILLIS)
        screenshot("policy")

        dispatchBack()
        waitUntilExists(hasTestTag(AboutTestTags.CONTENT))
    }

    private fun openAbout() {
        openMenu()
        click(menuItem(R.string.settings))
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
        val about = hasAnyAncestor(hasTestTag(SettingsTestTags.CONTENT)) and
            hasText(string(R.string.about_app)) and
            hasClickAction()
        rule.onNode(hasTestTag(SettingsTestTags.CONTENT)).performScrollToNode(about)
        click(about)
        waitUntilExists(hasTestTag(AboutTestTags.CONTENT))
    }

    private fun scrollTo(matcher: SemanticsMatcher) {
        rule.onNode(hasTestTag(AboutTestTags.CONTENT)).performScrollToNode(matcher)
    }

    private fun row(@StringRes title: Int): SemanticsMatcher =
        hasAnyAncestor(hasTestTag(AboutTestTags.CONTENT)) and hasText(string(title)) and hasClickAction()

    private companion object {
        const val POLICY_TIMEOUT_MILLIS = 30_000L
        const val EGG_TIMEOUT_MILLIS = 10_000L
        const val EGG_MID_MILLIS = 400L
        const val PAGE_DRAW_MILLIS = 1_000L
    }
}
