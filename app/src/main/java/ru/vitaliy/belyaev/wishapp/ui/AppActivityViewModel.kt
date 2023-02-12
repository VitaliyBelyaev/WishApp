package ru.vitaliy.belyaev.wishapp.ui

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.isEmpty
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.utils.SingleLiveEvent
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import timber.log.Timber

@HiltViewModel
class AppActivityViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val dispatcherProvider: DispatcherProvider
) : BaseViewModel() {

    val wishListToShareLiveData: SingleLiveEvent<List<WishEntity>> = SingleLiveEvent()
    val requestReviewLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
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
                        requestReviewLiveData.call()
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

    fun onDeleteWishClicked(wishId: String) {
        launchSafe {
            wishesRepository.deleteWishesByIds(listOf(wishId))
        }
    }

    fun onCompleteWishButtonClicked(wishId: String, oldIsCompleted: Boolean) {
        launchSafe {
            wishesRepository.updateWishIsCompleted(!oldIsCompleted, wishId)
        }
    }

    fun onShareWishListClicked(wishes: List<WishEntity>) {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SHARE) {
            param(AnalyticsNames.Param.QUANTITY, wishes.size.toString())
        }
        wishListToShareLiveData.postValue(wishes)
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
}