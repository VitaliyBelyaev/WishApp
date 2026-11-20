package ru.vitaliy.belyaev.wishapp.wishlist

import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performScrollToNode
import java.time.LocalDateTime
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupCallbacks
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupContent
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupProgress
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupState
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupStatus
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupTestTags
import ru.vitaliy.belyaev.wishapp.ui.screens.backup.BackupWork
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsTestTags
import ru.vitaliy.belyaev.wishapp.ui.theme.AppTheme

/**
 * UI-тест резервной копии нового вида. Войти в Google на эмуляторе нельзя, поэтому настоящий экран
 * проверяется только без аккаунта, а остальные состояния рисуются из готовых данных. Подготовка
 * и помощники — в [WishListUiTest]. Запуск:
 *
 *     TEST_CLASS=ru.vitaliy.belyaev.wishapp.wishlist.BackupScreenTest scripts/run_smoke_test.sh
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
internal class BackupScreenTest : WishListUiTest() {

    @Test
    fun opensFromSettingsAndAsksForAccess() {
        openMenu()
        click(menuItem(R.string.settings))
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
        click(hasText(string(R.string.backup_title)) and hasClickAction())

        waitUntilExists(hasText(string(R.string.backup_in_drive)))
        waitUntilExists(button(R.string.backup_allow_access_to_drive_text))
        screenshot("signed_out")

        click(hasContentDescription(string(R.string.cd_backup_info)))
        waitUntilExists(hasText(string(R.string.backup_help_title)))
        screenshot("info")
        // Настоящая кнопка «назад»: шторка в своём окне, а dispatchBack() попал бы мимо неё в экран
        device.pressBack()
        waitUntilGone(isDialog())
        waitUntilExists(hasText(string(R.string.backup_in_drive)))

        click(hasContentDescription(string(R.string.cd_back)) and hasClickAction())
        waitUntilExists(hasTestTag(SettingsTestTags.CONTENT))
    }

    @Test
    fun drawsEveryStateAndConfirmsReplacing() {
        val callbacks = RecordingCallbacks()
        var state by mutableStateOf(BackupState(BackupStatus.Checking(ACCOUNT)))
        rule.runOnUiThread {
            rule.activity.setContent {
                AppTheme(selectedTheme = Theme.SYSTEM, useWallpaperColors = false) {
                    BackupContent(state = state, callbacks = callbacks)
                }
            }
        }

        waitUntilExists(hasText(string(R.string.backup_checking)))
        // Аккаунт — шапка с аватаром-буквой, а не строка-действие: не нажимается
        waitUntilExists(hasText("A"))
        check(!exists(hasText(ACCOUNT) and hasClickAction()))
        screenshot("checking")

        state = BackupState(BackupStatus.CheckFailed(ACCOUNT))
        waitUntilExists(hasText(string(R.string.backup_check_failed)))
        click(button(R.string.retry))
        check(callbacks.refreshes == 1)
        screenshot("check_failed")

        state = BackupState(BackupStatus.NoBackup(ACCOUNT))
        waitUntilExists(hasText(string(R.string.backup_none_title)))
        screenshot("no_backup")

        // Новый телефон: главное — вернуть данные, и только после подтверждения
        state = BackupState(BackupStatus.HasBackup(backup, isRestoreSuggested = true))
        waitUntilExists(hasText(string(R.string.backup_exists_title)))
        screenshot("restore_suggested")
        click(button(R.string.backup_restore))
        waitUntilExists(isDialog())
        screenshot("restore_dialog")
        click(hasTestTag(BackupTestTags.CONFIRM))
        waitUntilGone(isDialog())
        check(callbacks.restores == 1)

        // Новая копия поверх старой тоже только с предупреждением
        scrollTo(hasText(string(R.string.backup_create_new)))
        click(hasText(string(R.string.backup_create_new)) and hasClickAction())
        waitUntilExists(hasText(string(R.string.backup_new_confirm_title)))
        click(hasText(string(R.string.cancel)))
        waitUntilGone(isDialog())
        check(callbacks.creates == 0)

        // Вошли с профилем: имя первой строкой, почта под ним
        state = BackupState(BackupStatus.HasBackup(backup, isRestoreSuggested = false), accountName = NAME)
        waitUntilExists(hasText(string(R.string.backup_all_safe)))
        waitUntilExists(hasText(NAME))
        waitUntilExists(hasText(ACCOUNT))
        screenshot("all_safe")
        click(button(R.string.backup_save_now))
        check(callbacks.creates == 1)
        click(hasContentDescription(string(R.string.cd_backup_refresh)))
        check(callbacks.refreshes == 2)
        scrollTo(hasText(string(R.string.backup_replace)))
        click(hasText(string(R.string.backup_replace)) and hasClickAction())
        waitUntilExists(hasText(string(R.string.backup_replace_confirm_title)))
        screenshot("replace_dialog")
        click(hasTestTag(BackupTestTags.CONFIRM))
        check(callbacks.restores == 2)

        // Пока сохраняем, остальные действия не нажимаются
        state =
            state.copy(
                work = BackupWork.Saving(
                    BackupProgress(fraction = 0.42f, loadedBytes = 1_008_000, totalBytes = 2_400_000),
                ),
            )
        waitUntilExists(hasText(string(R.string.backup_saving)))
        check(!exists(hasText(string(R.string.backup_replace)) and hasClickAction()))
        screenshot("saving")

        state =
            state.copy(
                work = BackupWork.Restoring(
                    BackupProgress(fraction = 0.6f, loadedBytes = 1_440_000, totalBytes = 2_400_000),
                ),
            )
        waitUntilExists(hasTestTag(BackupTestTags.RESTORING))
        screenshot("restoring")
        // «Назад» во время восстановления не уводит с экрана
        dispatchBack()
        waitUntilExists(hasTestTag(BackupTestTags.RESTORING))
        check(callbacks.backs == 0)
    }

    private fun button(@StringRes text: Int): SemanticsMatcher = hasText(string(text)) and hasClickAction()

    private fun scrollTo(matcher: SemanticsMatcher) {
        rule.onNode(
            hasTestTag(BackupTestTags.CONTENT),
        ).performScrollToNode(matcher and hasAnyAncestor(hasTestTag(BackupTestTags.CONTENT)))
    }

    private val backup = BackupInfo.Value(
        fileId = "file",
        modifiedDateTime = LocalDateTime.of(2026, 10, 15, 18, 30),
        sizeInBytes = 2_400_000,
        accountEmail = ACCOUNT,
        device = "Samsung S24",
    )

    private class RecordingCallbacks : BackupCallbacks {
        var refreshes = 0
        var creates = 0
        var restores = 0
        var backs = 0

        override fun onBack() {
            backs++
        }
        override fun onInfo() = Unit
        override fun onAllowAccess() = Unit
        override fun onRefresh() {
            refreshes++
        }
        override fun onCreate() {
            creates++
        }
        override fun onRestore() {
            restores++
        }
        override fun onSignOut() = Unit
        override fun onDisconnect() = Unit
    }

    private companion object {
        const val ACCOUNT = "anna@example.com"
        const val NAME = "Анна Смирнова"
    }
}
