package ru.vitaliy.belyaev.wishapp.screenshots

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
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
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AmplitudeWrapper
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.CurrentBackupInfoHolder
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupAuthRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupRepository
import ru.vitaliy.belyaev.wishapp.domain.use_case.CreateBackupUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.GetExistingBackupInfoUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.IsUserSignedInToBackupServiceUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.NeedToShowRestoreUseCase
import ru.vitaliy.belyaev.wishapp.domain.use_case.RestoreBackupUseCase
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.shared.utils.PrivacyPolicy
import ru.vitaliy.belyaev.wishapp.ui.AppActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.domain.repository.BackupLoadProgressListener
import ru.vitaliy.belyaev.wishapp.ui.old.screens.backup.BackupScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.backup.BackupViewModel
import ru.vitaliy.belyaev.wishapp.ui.old.screens.wish_list.WishListScreen
import ru.vitaliy.belyaev.wishapp.ui.old.screens.wish_list.WishListViewModel
import ru.vitaliy.belyaev.wishapp.ui.old.theme.WishAppTheme

/**
 * Снимает скриншоты всех экранов Android-приложения. Запускается через
 * `scripts/take_screenshots.sh` на отдельном AVD.
 *
 * Тест стирает базу приложения, поэтому без аргумента `-e screenshots true` он
 * пропускается ещё до запуска активности. Это не спасает от самого
 * `connectedAndroidTest`: после прогона он удаляет debug-приложение вместе с данными.
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalCoroutinesApi::class,
)
internal class ScreenshotsTest {

    val rule = createAndroidComposeRule<AppActivity>()

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val demoData = DemoData(language = InstrumentationRegistry.getArguments().getString("locale").orEmpty())

    /**
     * Пропускает тест без `-e screenshots true`. Иначе до запуска активности выключает
     * отправку аналитики и заполняет базу: если добавлять желания при открытом списке,
     * LazyColumn удержит позицию на старых элементах и новые окажутся выше экрана.
     */
    private val screenshotsModeRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                assumeTrue(
                    "Запускайте через scripts/take_screenshots.sh",
                    InstrumentationRegistry.getArguments().getString("screenshots") == "true"
                )
                Firebase.analytics.setAnalyticsCollectionEnabled(false)
                Firebase.crashlytics.isCrashlyticsCollectionEnabled = false
                AmplitudeWrapper.amplitude?.configuration?.optOut = true
                val seedRepository = WishAppSdk(DatabaseDriverFactory(targetContext)).getDatabaseRepository()
                runBlocking { demoData.seed(seedRepository) }
                base.evaluate()
            }
        }
    }

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(screenshotsModeRule).around(rule)

    private val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val repository: DatabaseRepository
        get() = rule.activity.wishAppSdk.getDatabaseRepository()

    /** Клавиатура показывает буфер обмена, который эмулятор получает с компьютера. */
    @Before
    fun clearClipboard() {
        rule.runOnUiThread {
            rule.activity.getSystemService(ClipboardManager::class.java).clearPrimaryClip()
        }
    }

    // region Wish list

    @Test
    fun wishList() {
        waitForText(demoData.firstWishTitle)
        shot("01_wish_list")
    }

    @Test
    fun wishListSheets() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Navigation menu")
        shot("02_tags_sheet")
        device.pressBack()

        clickDescription("Sort wishes")
        shot("03_sort_sheet")
        device.pressBack()

        clickDescription("Share wishes")
        shot("04_share_sheet")
    }

    @Test
    fun wishListFilters() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Navigation menu")
        clickLastText(demoData.firstTagTitle)
        shot("05_wish_list_by_tag")

        clickDescription("Navigation menu")
        clickString(R.string.completed_wishes)
        shot("06_wish_list_completed")
    }

    @Test
    fun wishListReorderAndSelection() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Reorder wishes")
        shot("07_wish_list_reorder")
        clickDescription("Reorder wishes")

        rule.onAllNodesWithText(demoData.firstWishTitle).onFirst().performSemanticsAction(SemanticsActions.OnLongClick)
        shot("08_wish_list_selection")

        clickDescription("Delete")
        shot("10_wish_list_delete_dialog")
        clickString(R.string.cancel)

        clickDescription("Menu")
        shot("09_wish_list_selection_menu")
    }

    @Test
    fun wishListEmptyStates() {
        runBlocking {
            repository.clearAllWishes()
            repository.clearAllTags()
            repository.insertTag(demoData.firstTagTitle)
        }
        waitForText(string(R.string.empty_current_wishes_message))
        shot("11_wish_list_empty")

        clickDescription("Navigation menu")
        clickString(R.string.completed_wishes)
        shot("12_wish_list_completed_empty")

        clickDescription("Navigation menu")
        clickLastText(demoData.firstTagTitle)
        shot("13_wish_list_by_tag_empty")
    }

    // endregion

    // region Wish detailed, images and tags

    @Test
    fun wishDetailed() {
        openFirstWish()
        shot("20_wish_detailed")

        clickDescription("Menu")
        shot("21_wish_detailed_menu")

        clickString(R.string.delete)
        shot("22_wish_detailed_delete_dialog")
        clickString(R.string.cancel)

        rule.onAllNodesWithContentDescription("Delete Link").onFirst().performClick()
        shot("23_wish_detailed_delete_link_dialog")
    }

    @Test
    fun wishDetailedNew() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Add wish")
        shotWithKeyboard("24_wish_detailed_new")
    }

    @Test
    fun wishDetailedLinkEntered() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Add wish")
        val linkField = rule.onAllNodes(hasSetTextAction())[LINK_FIELD_INDEX]
        linkField.performClick()
        linkField.performTextInput(LINK_TO_ENTER)
        shotWithKeyboard("24b_wish_detailed_link_entered")
    }

    @Test
    fun wishImagesViewer() {
        openFirstWish()
        rule.onAllNodes(wishImageMatcher).onFirst().performClick()
        // Картинка декодируется вне Compose, поэтому waitForIdle её не дожидается
        SystemClock.sleep(IMAGE_DECODE_MILLIS)
        shot("25_wish_images_viewer")

        clickDescription("Menu")
        shot("26_wish_images_viewer_menu")

        clickString(R.string.delete)
        shot("27_wish_images_viewer_delete_dialog")
    }

    @Test
    fun wishTags() {
        openFirstWish()
        clickDescription("Open tags")
        waitForText(demoData.firstTagTitle)
        shotWithKeyboard("28_wish_tags")

        rule.onNode(hasSetTextAction()).performTextInput(demoData.newTagTitle)
        shotWithKeyboard("29_wish_tags_new")
    }

    @Test
    fun editTags() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Navigation menu")
        clickString(R.string.edit_tags)
        waitForText(demoData.firstTagTitle)
        shot("30_edit_tags")

        clickLastText(demoData.firstTagTitle)
        shotWithKeyboard("31_edit_tags_edit_mode")

        clickDescription("Delete")
        shot("32_edit_tags_delete_dialog")
    }

    // endregion

    // region Snackbars

    @Test
    fun snackbarWishCompleted() {
        openFirstWish()
        clickDescription("Menu")
        clickString(R.string.wish_done)
        shotWithText("70_snackbar_wish_completed", R.string.wish_done_snack_message)
    }

    @Test
    fun wishDetailedInvalidLink() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Add wish")
        val linkField = rule.onAllNodes(hasSetTextAction())[LINK_FIELD_INDEX]
        linkField.performClick()
        linkField.performTextInput(INVALID_LINK)
        clickDescription("Add Link")
        shotWithText("71_wish_detailed_invalid_link", R.string.link_invalid)
    }

    @Test
    fun wishDetailedFromSharedText() {
        waitForText(demoData.firstWishTitle)
        val intent = Intent(rule.activity, AppActivity::class.java)
            .setAction(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "$SHARED_TEXT_TITLE: $LINK_TO_ENTER")
        rule.activity.startActivity(intent)
        waitForText(SHARED_TEXT_TITLE)
        shot("71b_wish_detailed_from_shared_text")
    }

    @Test
    fun snackbarDeleteError() {
        val activity = rule.activity
        val failingRepository = object : WishesRepository by repository {
            override suspend fun deleteWishesByIds(ids: List<String>) = error("Fake delete error")
        }
        rule.runOnUiThread {
            val dataStoreRepository = DataStoreRepository(
                PreferenceDataStoreFactory.create {
                    activity.preferencesDataStoreFile("screenshots_${UUID.randomUUID()}")
                }
            )
            val viewModel = WishListViewModel(
                failingRepository,
                repository,
                repository,
                activity.analyticsRepository,
                dataStoreRepository
            )
            activity.setContent {
                WishAppTheme(selectedTheme = Theme.SYSTEM) {
                    WishListScreen(
                        openWishDetailed = {},
                        onAddWishClicked = {},
                        onSettingIconClicked = {},
                        onEditTagClick = {},
                        onGoToBackupScreenClicked = {},
                        viewModel = viewModel,
                    )
                }
            }
        }
        waitForText(demoData.firstWishTitle)
        rule.onAllNodesWithText(demoData.firstWishTitle).onFirst().performSemanticsAction(SemanticsActions.OnLongClick)
        clickDescription("Delete")
        clickLastText(string(R.string.delete))
        shotWithText("72_snackbar_delete_error", R.string.delete_wishes_error_message)
    }

    @Test
    fun snackbarFailToOpenLink() {
        waitForText(demoData.firstWishTitle)
        runBlocking { repository.updateWishLink(INVALID_LINK, firstWishId()) }
        openFirstWish()
        rule.onNodeWithText(INVALID_LINK).performClick()
        shotWithText("73_snackbar_fail_to_open_link", R.string.fail_to_open_link)
    }

    /** На эмуляторе письма умеет отправлять только Gmail, поэтому на время теста выключаем его. */
    @Test
    fun snackbarNoEmailApp() {
        // На настоящем устройстве почтовые приложения не трогаем
        assumeTrue(InstrumentationRegistry.getArguments().getString("emulator") == "true")
        waitForText(demoData.firstWishTitle)
        device.executeShellCommand("pm disable-user --user 0 $GMAIL_PACKAGE")
        try {
            clickDescription("Settings")
            clickString(R.string.about_app)
            clickString(R.string.feedback)
            shotWithText("74_snackbar_no_email_app", R.string.no_email_app_error)
        } finally {
            device.executeShellCommand("pm enable $GMAIL_PACKAGE")
        }
    }

    @Test
    fun snackbarBackupCreated() {
        showBackupScreen(
            checkBackup = { BackupInfo.None(FAKE_ACCOUNT_EMAIL) },
            uploadBackup = { fakeBackupInfo },
        )
        waitForText(string(R.string.backup_create_backup_button_text))
        clickString(R.string.backup_create_backup_button_text)
        shotWithText("75_snackbar_backup_created", R.string.backup_create_backup_success_text)
    }

    @Test
    fun snackbarBackupCreateError() {
        showBackupScreen(
            checkBackup = { BackupInfo.None(FAKE_ACCOUNT_EMAIL) },
            uploadBackup = { error("Fake upload error") },
        )
        waitForText(string(R.string.backup_create_backup_button_text))
        clickString(R.string.backup_create_backup_button_text)
        shotWithText("76_snackbar_backup_create_error", R.string.backup_create_backup_error_text)
    }

    @Test
    fun snackbarBackupRestored() {
        showBackupScreen(
            checkBackup = { fakeBackupInfo },
            downloadBackup = {},
            contextWrapper = ::RestoreWithoutRestartContext,
        )
        confirmBackupRestore()
        shotWithText("77_snackbar_backup_restored", R.string.backup_restore_backup_success_text)
    }

    @Test
    fun snackbarBackupRestoreError() {
        showBackupScreen(
            checkBackup = { fakeBackupInfo },
            downloadBackup = { error("Fake download error") },
        )
        confirmBackupRestore()
        shotWithText("78_snackbar_backup_restore_error", R.string.backup_restore_backup_error_text)
    }

    @Test
    fun snackbarBackupCheckError() {
        var isFirstCheck = true
        showBackupScreen(
            checkBackup = {
                if (isFirstCheck) {
                    isFirstCheck = false
                    fakeBackupInfo
                } else {
                    error("Fake check backup error")
                }
            },
        )
        waitForText(string(R.string.backup_restore_button_text))
        clickDescription("Refresh")
        shotWithText("79_snackbar_backup_check_error", R.string.backup_check_backup_error_text)
    }

    @Test
    fun snackbarBackupAuthError() {
        val viewModel = showBackupScreen(checkBackup = { fakeBackupInfo })
        waitForText(string(R.string.backup_restore_button_text))
        rule.runOnUiThread {
            viewModel.onSignInResultReceived(ActivityResult(Activity.RESULT_FIRST_USER, null))
        }
        shotWithText("80_snackbar_backup_auth_error", R.string.backup_google_auth_error_text)
    }

    private fun confirmBackupRestore() {
        waitForText(string(R.string.backup_restore_button_text))
        clickString(R.string.backup_restore_button_text)
        clickLastText(string(R.string.backup_restore_backup_confirm_dialog_positive_text))
    }

    private suspend fun firstWishId(): String {
        return repository.getAllWishes(isCompleted = false).first { it.title == demoData.firstWishTitle }.id
    }

    private fun shotWithText(name: String, textResId: Int) {
        waitForText(string(textResId))
        shot(name)
    }

    // endregion

    // region Settings and about

    @Test
    fun settingsAndAbout() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Settings")
        shot("40_settings")

        clickString(R.string.about_app)
        shot("41_about_app")

        clickString(R.string.privacy_policy)
        waitForPrivacyPolicyLoaded()
        shot("42_privacy_policy")
    }

    @Test
    fun ossLicenses() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Settings")
        clickString(R.string.about_app)
        clickString(R.string.licenses)
        shot("43_oss_licenses")
    }

    // endregion

    // region Backup

    @Test
    fun backupSignedOut() {
        waitForText(demoData.firstWishTitle)
        clickDescription("Settings")
        clickString(R.string.backup)
        waitForText(string(R.string.backup_allow_access_to_drive_text), BACKUP_SIGN_IN_TIMEOUT_MILLIS)
        shot("50_backup_signed_out")

        clickDescription("Backup info")
        shot("51_backup_info_sheet")
    }

    @Test
    fun backupNoBackup() {
        showBackupScreen(checkBackup = { BackupInfo.None(FAKE_ACCOUNT_EMAIL) })
        waitForText(string(R.string.backup_create_backup_button_text))
        shot("52_backup_no_backup")
    }

    @Test
    fun backupWithRestore() {
        showBackupScreen(checkBackup = { fakeBackupInfo })
        waitForText(string(R.string.backup_restore_button_text))
        shot("53_backup_with_restore")

        clickString(R.string.backup_create_backup_button_text)
        shot("54_backup_create_dialog")
        clickString(R.string.cancel)

        clickString(R.string.backup_restore_button_text)
        shot("55_backup_restore_dialog")
    }

    @Test
    fun backupWithForceUpdate() {
        showBackupScreen(checkBackup = { fakeBackupInfo }, wasBackupCreated = true)
        waitForText(string(R.string.backup_force_update_button_text))
        shot("56_backup_with_force_update")

        clickString(R.string.backup_force_update_button_text)
        shot("57_backup_force_update_dialog")
    }

    @Test
    fun backupCheckError() {
        showBackupScreen(checkBackup = { error("Fake check backup error") })
        waitForText(string(R.string.backup_retry_check))
        shot("58_backup_check_error")
    }

    @Test
    fun backupChecking() {
        showBackupScreen(checkBackup = { awaitCancellation() })
        waitForText(string(R.string.backup_check_backup_loader_text))
        shot("59_backup_checking")
    }

    @Test
    fun backupUploading() {
        showBackupScreen(checkBackup = { BackupInfo.None(FAKE_ACCOUNT_EMAIL) })
        waitForText(string(R.string.backup_create_backup_button_text))
        clickString(R.string.backup_create_backup_button_text)
        waitForText(string(R.string.backup_create_backup_loader_text))
        shot("60_backup_uploading")
    }

    @Test
    fun backupRestoring() {
        showBackupScreen(checkBackup = { fakeBackupInfo })
        waitForText(string(R.string.backup_restore_button_text))
        clickString(R.string.backup_restore_button_text)
        clickLastText(string(R.string.backup_restore_backup_confirm_dialog_positive_text))
        waitForText(string(R.string.backup_restore_backup_loader_text))
        shot("61_backup_restoring")
    }

    /**
     * Состояния с Google-аккаунтом на эмуляторе без входа не получить, поэтому
     * рисуем настоящий [BackupScreen] поверх фейковых репозиториев.
     */
    private fun showBackupScreen(
        checkBackup: suspend () -> BackupInfo,
        wasBackupCreated: Boolean = false,
        uploadBackup: suspend () -> BackupInfo.Value = { awaitCancellation() },
        downloadBackup: suspend () -> Unit = { awaitCancellation() },
        contextWrapper: (Context) -> Context = { it },
    ): BackupViewModel {
        val activity = rule.activity
        val backupRepository = FakeBackupRepository(checkBackup, uploadBackup, downloadBackup)
        val dataStoreRepository = DataStoreRepository(
            PreferenceDataStoreFactory.create {
                activity.preferencesDataStoreFile("screenshots_${UUID.randomUUID()}")
            }
        )
        runBlocking { dataStoreRepository.updateIsBackupWasCreated(wasBackupCreated) }

        val viewModel = rule.runOnUiThread {
            val currentBackupInfoHolder = CurrentBackupInfoHolder()
            val viewModel = BackupViewModel(
                createBackupUseCase = CreateBackupUseCase(backupRepository, dataStoreRepository),
                restoreBackupUseCase = RestoreBackupUseCase(backupRepository, dataStoreRepository),
                isUserSignedInToBackupServiceUseCase = IsUserSignedInToBackupServiceUseCase(FakeBackupAuthRepository),
                getExistingBackupInfoUseCase = GetExistingBackupInfoUseCase(backupRepository, currentBackupInfoHolder),
                currentBackupInfoHolder = currentBackupInfoHolder,
                backupAuthRepository = FakeBackupAuthRepository,
                analyticsRepository = activity.analyticsRepository,
                needToShowRestoreUseCase = NeedToShowRestoreUseCase(dataStoreRepository),
                wishAppSdk = activity.wishAppSdk,
            )
            activity.setContent {
                WishAppTheme(selectedTheme = Theme.SYSTEM) {
                    val appViewModel = hiltViewModel<AppActivityViewModel>(activity)
                    CompositionLocalProvider(LocalContext provides contextWrapper(activity)) {
                        BackupScreen(
                            onBackPressed = {},
                            analyticsRepository = activity.analyticsRepository,
                            viewModel = viewModel,
                            appViewModel = appViewModel,
                        )
                    }
                }
            }
            viewModel
        }
        rule.waitForIdle()
        return viewModel
    }

    /**
     * После восстановления приложение заменяет файл базы и перезапускает процесс, что
     * оборвало бы тест. Поэтому база здесь временная, а перезапуск уходит на остановленный
     * поток и не выполняется.
     */
    private class RestoreWithoutRestartContext(base: Context) : ContextWrapper(base) {

        private val stoppedLooper: Looper = HandlerThread("stopped-looper").run {
            start()
            looper.also { quit() }
        }

        override fun getMainLooper(): Looper = stoppedLooper

        override fun getDatabasePath(name: String): File = File(cacheDir, "screenshots_restored_$name")
    }

    private val fakeBackupInfo = BackupInfo.Value(
        fileId = "fake-file-id",
        modifiedDateTime = LocalDateTime.of(2026, 10, 15, 18, 30),
        sizeInBytes = 2_411_724,
        accountEmail = FAKE_ACCOUNT_EMAIL,
        device = "Google Pixel 7",
    )

    private class FakeBackupRepository(
        private val checkBackup: suspend () -> BackupInfo,
        private val uploadBackup: suspend () -> BackupInfo.Value,
        private val downloadBackup: suspend () -> Unit,
    ) : BackupRepository {

        override suspend fun checkExistingBackup(): BackupInfo = checkBackup()

        override suspend fun uploadNewBackup(
            backupFile: File,
            progressListener: BackupLoadProgressListener,
        ): BackupInfo.Value {
            progressListener.onProgressChanged(FAKE_PROGRESS, FAKE_LOADED_BYTES)
            return uploadBackup()
        }

        override suspend fun downloadBackup(
            fileId: String,
            outputStream: OutputStream,
            progressListener: BackupLoadProgressListener,
        ) {
            progressListener.onProgressChanged(FAKE_PROGRESS, FAKE_LOADED_BYTES)
            downloadBackup()
        }
    }

    private object FakeBackupAuthRepository : BackupAuthRepository {

        override fun isSignedIn(): Boolean = true

        override suspend fun signInSilently() = Unit

        override suspend fun signOut() = Unit

        override suspend fun disconnectAccount() = Unit

        override suspend fun checkIsSignedInFromIntent(intent: Intent): Boolean = true

        override fun getSignInIntent(): Intent = Intent()
    }

    // endregion

    private fun openFirstWish() {
        waitForText(demoData.firstWishTitle)
        rule.onAllNodesWithText(demoData.firstWishTitle).onFirst().performClick()
        // Комментарий есть и на карточке в списке, поэтому ждём именно поле ввода
        val commentField = hasSetTextAction() and hasText(demoData.firstWishComment)
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodes(commentField).fetchSemanticsNodes().isNotEmpty()
        }
        // Пока картинка не декодирована, её ширина нулевая и клик по ней не попадёт
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.onAllNodes(wishImageMatcher).fetchSemanticsNodes().firstOrNull()?.let { it.size.width > 0 } ?: false
        }
    }

    /** На странице с ошибкой WebView тоже дойдёт до 100%, поэтому сначала проверяем сам адрес. */
    private fun waitForPrivacyPolicyLoaded() {
        val connection = URL(PrivacyPolicy.url).openConnection() as HttpURLConnection
        val responseCode = try {
            connection.responseCode
        } finally {
            connection.disconnect()
        }
        assertTrue("Политика недоступна, HTTP $responseCode", responseCode in 200..299)

        rule.waitUntil(WEB_PAGE_TIMEOUT_MILLIS) {
            rule.runOnUiThread {
                val webView = findWebView(rule.activity.window.decorView)
                webView != null && webView.progress == 100 && webView.contentHeight > 0
            }
        }
    }

    private fun findWebView(view: View): WebView? = when (view) {
        is WebView -> view
        is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull { findWebView(view.getChildAt(it)) }
        else -> null
    }

    private fun string(resId: Int): String = rule.activity.getString(resId)

    private fun clickString(resId: Int) {
        rule.onNodeWithText(string(resId)).performClick()
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

    /** Клавиатура открывается асинхронно, и waitForIdle её появления не ждёт. */
    private fun shotWithKeyboard(name: String) {
        rule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            rule.runOnUiThread {
                ViewCompat.getRootWindowInsets(rule.activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            }
        }
        shot(name)
    }

    private fun shot(name: String) {
        rule.waitForIdle()
        SystemClock.sleep(SETTLE_MILLIS)
        val directory = File(rule.activity.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        assertTrue("Не удалось снять $name", device.takeScreenshot(File(directory, "$name.png")))
    }

    private companion object {

        const val WAIT_TIMEOUT_MILLIS = 10_000L
        const val BACKUP_SIGN_IN_TIMEOUT_MILLIS = 30_000L
        const val SETTLE_MILLIS = 700L
        const val WEB_PAGE_TIMEOUT_MILLIS = 30_000L
        const val IMAGE_DECODE_MILLIS = 2_000L
        const val FAKE_ACCOUNT_EMAIL = "user@example.com"
        const val LINK_FIELD_INDEX = 2
        const val LINK_TO_ENTER = "https://example.com/headphones"
        const val INVALID_LINK = "not-a-link"
        const val SHARED_TEXT_TITLE = "Настольная лампа"
        const val GMAIL_PACKAGE = "com.google.android.gm"
        const val FAKE_PROGRESS = 0.42
        const val FAKE_LOADED_BYTES = 1_012_924L
    }
}
