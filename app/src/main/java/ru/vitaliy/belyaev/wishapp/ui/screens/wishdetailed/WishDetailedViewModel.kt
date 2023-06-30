package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Optional
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.GetLinkPreviewInteractor
import ru.vitaliy.belyaev.wishapp.entity.toValueOfNull
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.LinkPreviewState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.Loading
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.None
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishItem
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.toWishItem

@ExperimentalCoroutinesApi
@HiltViewModel
class WishDetailedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val getLinkPreviewInteractor: GetLinkPreviewInteractor,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    val inputWishId: String = savedStateHandle[ARG_WISH_ID] ?: ""
    lateinit var wishId: String

    val uiState: MutableStateFlow<Optional<WishItem>> = MutableStateFlow(Optional.empty())
    private var cachedLinkPreviewState: LinkPreviewState = None

    init {
        analyticsRepository.trackEvent(AnalyticsNames.Event.SCREEN_VIEW) {
            param(AnalyticsNames.Param.SCREEN_NAME, "WishDetailed")
        }

        launchSafe {
            wishId = inputWishId.ifBlank {
                val wish = createEmptyWish()
                wishesRepository.insertWish(wish)
                wish.id
            }

            wishesRepository
                .observeWishById(wishId)
                .collect {
                    val previousLink: String = uiState.value.toValueOfNull()?.wish?.link ?: ""
                    val currentLink = it.link
                    if (previousLink == currentLink) {
                        val wishItem = it.toWishItem(cachedLinkPreviewState)
                        uiState.value = Optional.of(wishItem)
                    } else {
                        tryLoadLinkPreview(currentLink, it)
                    }
                }
        }
    }

    private suspend fun tryLoadLinkPreview(link: String, wish: WishEntity) {
        if (link.isBlank()) {
            cachedLinkPreviewState = None
            val wishItem = wish.toWishItem(cachedLinkPreviewState)
            uiState.value = Optional.of(wishItem)
        } else {
            val wishItemLoading = wish.toWishItem(Loading)
            uiState.value = Optional.of(wishItemLoading)
            cachedLinkPreviewState = getLinkPreviewInteractor(link)
            val wishItem = wish.toWishItem(cachedLinkPreviewState)
            uiState.value = Optional.of(wishItem)
        }
    }

    fun onBackPressed() {
        viewModelScope.cancel()
    }

    fun onWishTitleChanged(newValue: String) {
        launchSafe {
            wishesRepository.updateWishTitle(newValue, wishId)
        }
    }

    fun onWishLinkChanged(newValue: String) {
        launchSafe {
            wishesRepository.updateWishLink(newValue, wishId)
        }
    }

    fun onWishCommentChanged(newValue: String) {
        launchSafe {
            wishesRepository.updateWishComment(newValue, wishId)
        }
    }

    fun onDeleteWishClicked() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.DELETE_WISH_FROM_WISH_DETAILED)
        viewModelScope.cancel()
    }

    fun onLinkPreviewClick() {
        analyticsRepository.trackEvent(AnalyticsNames.Event.WISH_LINK_CLICK)
    }
}