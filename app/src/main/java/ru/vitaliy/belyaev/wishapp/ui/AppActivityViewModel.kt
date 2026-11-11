package ru.vitaliy.belyaev.wishapp.ui

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.UiLook
import ru.vitaliy.belyaev.wishapp.domain.model.UiLookMode
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateDownloadedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateRestartClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateRestartDismissedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishCreatedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedChangeWishCompletenessClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishImagesViewerDeleteImageConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.resolveUiLook
import ru.vitaliy.belyaev.wishapp.domain.model.storedValue
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.repository.RemoteConfigRepository
import ru.vitaliy.belyaev.wishapp.domain.use_case.UpdateAnalyticsUserPropertiesUseCase
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.isEmpty
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.WISHLIST_PDF_DIR_NAME
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import timber.log.Timber

// Состояние уровня приложения для обоих видов: тема, вид, снэкбары, оценка, обновление из Google Play
@Suppress("TooManyFunctions")
@HiltViewModel
class AppActivityViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val imagesRepository: ImagesRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val updateAnalyticsUserProperties: UpdateAnalyticsUserPropertiesUseCase,
    remoteConfigRepository: RemoteConfigRepository,
) : BaseViewModel() {

    private val _requestReviewFlow: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 13)
    val requestReviewFlow: SharedFlow<Unit> = _requestReviewFlow.asSharedFlow()

    private val _selectedTheme: MutableStateFlow<Theme> = MutableStateFlow(Theme.SYSTEM)
    val selectedTheme: StateFlow<Theme> = _selectedTheme

    private val _showSnackOnMainFlow = Channel<AppSnackMessage>(capacity = Channel.BUFFERED)
    val showSnackOnMainFlow: Flow<AppSnackMessage> = _showSnackOnMainFlow.receiveAsFlow()

    private val _uiLook: MutableStateFlow<UiLook?> = MutableStateFlow(null)

    /** Вид приложения, `null` пока не прочитан выбор пользователя: на это время держим сплэш. */
    val uiLook: StateFlow<UiLook?> = _uiLook

    private val _useWallpaperColors: MutableStateFlow<Boolean?> = MutableStateFlow(null)

    /** Настройка «Как обои» нового вида, `null` пока не прочитана: сплэш ждёт и её, чтобы не мелькнули другие цвета. */
    val useWallpaperColors: StateFlow<Boolean?> = _useWallpaperColors

    // Обновление из Google Play скачано и ждёт перезапуска: пока true, главный экран держит снэкбар
    // «Перезапустить». Сбрасывается, только когда человек сам закрыл снэкбар или перезапустил
    private val _isUpdateReadyToInstall = MutableStateFlow(false)
    val isUpdateReadyToInstall: StateFlow<Boolean> = _isUpdateReadyToInstall

    // Нажали «Перезапустить»: обновление ставит активити
    private val _completeUpdateFlow = Channel<Unit>(capacity = Channel.CONFLATED)
    val completeUpdateFlow: Flow<Unit> = _completeUpdateFlow.receiveAsFlow()

    private val countedNewWishIds: MutableSet<String> = mutableSetOf()

    init {
        launchSafe {
            // Режим читается один раз за жизнь ViewModel: новое значение из Remote Config применяется
            // со следующего запуска, чтобы интерфейс не менялся на глазах. Чтение может ждать диск
            try {
                val uiLookMode: UiLookMode = withContext(dispatcherProvider.io()) {
                    val activated = runCatching { remoteConfigRepository.activatedUiLookMode() }
                        .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
                        .getOrDefault(UiLookMode.OLD_ONLY)
                    remoteConfigRepository.fetchForNextLaunch()
                    activated
                }
                // Выбор пользователя меняет вид сразу: это его действие, а не флаг
                dataStoreRepository.uiLookChoiceFlow
                    .map { choice ->
                        resolveUiLook(
                            mode = uiLookMode,
                            userChoice = choice,
                            allowChoiceInOldOnly = BuildConfig.DEBUG,
                            isNewLookReleased = IS_NEW_LOOK_RELEASED,
                        )
                    }
                    .distinctUntilChanged()
                    .collect {
                        FirebaseCrashlytics.getInstance().setCustomKey(CRASHLYTICS_KEY_UI_LOOK, it.storedValue)
                        _uiLook.value = it
                    }
            } finally {
                // Сплэш ждёт вид: при любом сбое показываем прежний, а не висим на сплэше
                if (_uiLook.value == null) _uiLook.value = UiLook.OLD
            }
        }

        launchSafe {
            try {
                dataStoreRepository.useWallpaperColorsFlow.collect { _useWallpaperColors.value = it }
            } finally {
                if (_useWallpaperColors.value == null) _useWallpaperColors.value = false
            }
        }

        launchSafe {
            dataStoreRepository
                .selectedThemeFlow
                .collect {
                    _selectedTheme.value = it
                }
        }

        launchSafe(dispatcherProvider.io()) {
            combine(
                dataStoreRepository.selectedThemeFlow,
                dataStoreRepository.isBackupWasCreated,
            ) { theme, isBackupCreated -> theme to isBackupCreated }
                .distinctUntilChanged()
                .collectLatest { (theme, isBackupCreated) ->
                    updateAnalyticsUserProperties(theme, isBackupCreated)
                }
        }

        launchSafe {
            combine(
                dataStoreRepository.positiveActionsCountFlow,
                dataStoreRepository.reviewRequestShownCountFlow,
            ) { positiveActionsCount, reviewRequestShownCount ->
                val needShowReviewRequest = positiveActionsCount != 0 &&
                    reviewRequestShownCount != positiveActionsCount &&
                    positiveActionsCount % 5 == 0
                needShowReviewRequest to positiveActionsCount
            }
                .distinctUntilChanged()
                .collect { (needShowReviewRequest, positiveActionsCount) ->
                    if (needShowReviewRequest) {
                        dataStoreRepository.updateReviewRequestShownCount(positiveActionsCount)
                        _requestReviewFlow.emit(Unit)
                    }
                }
        }
    }

    fun onWishScreenExit(wishId: String, isNewWish: Boolean) {
        // Повторный «назад» во время анимации выхода не должен считаться вторым действием
        val isFirstExitOfNewWish = isNewWish && countedNewWishIds.add(wishId)
        launchSafe(dispatcherProvider.io()) {
            val wish: WishEntity = wishesRepository.getWishByIdOrNull(wishId) ?: return@launchSafe
            if (wish.isEmpty()) {
                wishesRepository.deleteWishesByIds(listOf(wishId))
                return@launchSafe
            }

            if (isFirstExitOfNewWish) {
                analyticsRepository.trackEvent(
                    WishCreatedEvent(
                        source = WishCreatedEvent.SOURCE_WISH_SCREEN,
                        hasTitle = wish.title.isNotBlank(),
                        linksCount = wish.links.size,
                        imagesCount = wish.images.size,
                        hasComment = wish.comment.isNotBlank(),
                        tagsCount = wish.tags.size,
                    ),
                )
                dataStoreRepository.incrementPositiveActionsCount()
            }
        }
    }

    /** Убирает пустые желания, когда WishDetailed закрылся без обычного выхода с экрана. */
    fun deleteEmptyWishes(): Job {
        return launchSafe(dispatcherProvider.io()) {
            val emptyWishIds = wishesRepository
                .getAllWishes(isCompleted = false)
                .filter { it.isEmpty() }
                .map { it.id }
            if (emptyWishIds.isNotEmpty()) {
                wishesRepository.deleteWishesByIds(emptyWishIds)
            }
        }
    }

    fun onDeleteWishConfirmed(wishId: String) {
        launchSafe {
            wishesRepository.deleteWishesByIds(listOf(wishId))
        }
    }

    fun onDeleteWishImageConfirmed(wishImageId: String) {
        analyticsRepository.trackEvent(WishImagesViewerDeleteImageConfirmedEvent)
        launchSafe {
            imagesRepository.deleteImageById(wishImageId)
        }
    }

    fun onCompleteWishButtonClicked(wishId: String, oldIsCompleted: Boolean) {
        analyticsRepository.trackEvent(WishDetailedChangeWishCompletenessClickedEvent)
        launchSafe {
            wishesRepository.updateWishIsCompleted(!oldIsCompleted, wishId)
            if (!oldIsCompleted) {
                dataStoreRepository.incrementPositiveActionsCount()
            }
        }
    }

    // Состояние во ViewModel, а не в активити: поворот и возврат из фона снэкбар не возвращают,
    // если его уже закрыли
    private var updateDownloadedReported = false

    fun onUpdateDownloaded() {
        if (updateDownloadedReported) return
        updateDownloadedReported = true
        analyticsRepository.trackEvent(InAppUpdateDownloadedEvent)
        _isUpdateReadyToInstall.value = true
    }

    fun onUpdateDownloadStarted(message: String) {
        showSnackMessageOnMain(message)
    }

    fun onUpdateSnackbarDismissed() {
        analyticsRepository.trackEvent(InAppUpdateRestartDismissedEvent)
        _isUpdateReadyToInstall.value = false
    }

    fun onRestartForUpdateClicked() {
        analyticsRepository.trackEvent(InAppUpdateRestartClickedEvent)
        _isUpdateReadyToInstall.value = false
        _completeUpdateFlow.trySend(Unit)
    }

    fun showSnackMessageOnMain(message: String) {
        showSnack(AppSnackMessage(text = message))
    }

    fun showSnack(message: AppSnackMessage) {
        launchSafe(Dispatchers.Main) {
            val result = _showSnackOnMainFlow.trySend(message)
            if (!result.isSuccess) {
                val throwable =
                    result.exceptionOrNull() ?: IllegalStateException("Unknown send message on main exception")
                FirebaseCrashlytics.getInstance().recordException(throwable)
                Timber.e(
                    throwable,
                    "Failed to send message on main ${message.text}",
                )
            }
        }
    }

    fun onCreateWithoutSavedInstanceState(context: Context) {
        launchSafe(dispatcherProvider.io()) {
            clearPdfFilesForShare(context)
        }
    }

    private fun clearPdfFilesForShare(context: Context) {
        val pdfDir = File(context.filesDir, WISHLIST_PDF_DIR_NAME)
        if (pdfDir.exists()) {
            pdfDir.listFiles()?.forEach {
                it.delete()
            }
        }
    }

    fun onUiLookChosen(uiLook: UiLook) {
        launchSafe { dataStoreRepository.updateUiLookChoice(uiLook) }
    }

    fun onThemeChosen(theme: Theme) {
        launchSafe { dataStoreRepository.updateSelectedTheme(theme) }
    }

    fun onUseWallpaperColorsChanged(newValue: Boolean) {
        launchSafe { dataStoreRepository.updateUseWallpaperColors(newValue) }
    }

    private companion object {
        /**
         * Пока новый вид не готов, релизная сборка показывает прежний при любом значении флага:
         * случайно включённый `choice` не откроет пользователям недоделанный экран.
         * Включить вместе с готовым новым видом, этап 2.7.
         */
        const val IS_NEW_LOOK_RELEASED = false

        const val CRASHLYTICS_KEY_UI_LOOK = "ui_look"
    }
}
