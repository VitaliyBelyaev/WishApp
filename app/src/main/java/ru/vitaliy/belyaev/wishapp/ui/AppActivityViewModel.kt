package ru.vitaliy.belyaev.wishapp.ui

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedChangeWishCompletenessClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishImagesViewerDeleteImageConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.isEmpty
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.WISHLIST_PDF_DIR_NAME
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import timber.log.Timber

@HiltViewModel
class AppActivityViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val imagesRepository: ImagesRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val dispatcherProvider: DispatcherProvider
) : BaseViewModel() {

    private val _requestReviewFlow: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 2)
    val requestReviewFlow: SharedFlow<Unit> = _requestReviewFlow.asSharedFlow()

    private val _selectedTheme: MutableStateFlow<Theme> = MutableStateFlow(Theme.SYSTEM)
    val selectedTheme: StateFlow<Theme> = _selectedTheme

    private val _showSnackOnMainFlow = Channel<String>(capacity = Channel.BUFFERED)
    val showSnackOnMainFlow: Flow<String> = _showSnackOnMainFlow.receiveAsFlow()

    init {
        launchSafe {
            dataStoreRepository
                .selectedThemeFlow
                .collect {
                    _selectedTheme.value = it
                }
        }

        launchSafe {
            combine(
                dataStoreRepository.positiveActionsCountFlow,
                dataStoreRepository.reviewRequestShownCountFlow
            ) { positiveActionsCount, reviewRequestShownCount ->
                val needShowReviewRequest = positiveActionsCount != 0 &&
                        reviewRequestShownCount != positiveActionsCount &&
                        positiveActionsCount % 10 == 0
                needShowReviewRequest to positiveActionsCount
            }
                .collect { (needShowReviewRequest, positiveActionsCount) ->
                    if (needShowReviewRequest) {
                        dataStoreRepository.updateReviewRequestShownCount(positiveActionsCount)
                        _requestReviewFlow.emit(Unit)
                    }
                }
        }
    }

    fun onWishScreenExit(wishId: String, isNewWish: Boolean) {
        launchSafe(dispatcherProvider.io()) {
            val wish: WishEntity = wishesRepository.getWishById(wishId)
            if (wish.isEmpty()) {
                wishesRepository.deleteWishesByIds(listOf(wishId))
                return@launchSafe
            }

            if (isNewWish) {
                dataStoreRepository.incrementPositiveActionsCount()
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
        }
    }

    fun showSnackMessageOnMain(message: String) {
        launchSafe(Dispatchers.Main) {
            val result = _showSnackOnMainFlow.trySend(message)
            if (!result.isSuccess) {
                val throwable =
                    result.exceptionOrNull() ?: IllegalStateException("Unknown send message on main exception")
                FirebaseCrashlytics.getInstance().recordException(throwable)
                Timber.e(
                    throwable,
                    "Failed to send message on main $message"
                )
            }
        }
    }

    fun onCreateWithoutSavedInstanceState(context: Context) {
        launchSafe(Dispatchers.IO) {
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
}