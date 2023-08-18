package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Optional
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.entity.analytics.WishDetailedScreenShowEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishDetailedDeleteWishConfirmedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.WishDetailedWishLinkClickedEvent
import ru.vitaliy.belyaev.wishapp.entity.toValueOfNull
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_LINK
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem

@ExperimentalCoroutinesApi
@HiltViewModel
class WishDetailedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    val inputWishId: String = savedStateHandle[ARG_WISH_ID] ?: ""
    lateinit var wishId: String

    private val sharedLinkForNewWish: String = savedStateHandle[ARG_WISH_LINK] ?: ""

    var linkInputString: String
        get() {
            return savedStateHandle[KEY_LINK_INPUT_STRING] ?: sharedLinkForNewWish
        }
        private set(value) {
            savedStateHandle[KEY_LINK_INPUT_STRING] = value
        }

    val uiState: MutableStateFlow<Optional<WishItem>> = MutableStateFlow(Optional.empty())

    init {
        launchSafe {
            wishId = inputWishId.ifBlank {
                val wish = createEmptyWish()
                wishesRepository.insertWish(wish)
                wish.id
            }

            wishesRepository
                .observeWishById(wishId)
                .collect {
                    uiState.value = Optional.of(WishItem(it, false))
                }
        }
    }

    fun trackScreenShow() {
        analyticsRepository.trackEvent(WishDetailedScreenShowEvent(isNewWish = inputWishId.isBlank()))
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
        linkInputString = newValue
    }

    fun isLinkValid(link: String): Boolean {
        return Patterns.WEB_URL.matcher(link).matches()
    }

    fun onAddLinkClicked(link: String) {
        val currentLinks = uiState.value.toValueOfNull()?.wish?.links ?: return

        launchSafe {
            val newLinkString = LinksAdapter.addLinkAndGetAccumulatedString(link, currentLinks)
            wishesRepository.updateWishLink(newLinkString, wishId)
        }
    }

    fun onWishCommentChanged(newValue: String) {
        launchSafe {
            wishesRepository.updateWishComment(newValue, wishId)
        }
    }

    fun onDeleteWishConfirmed() {
        analyticsRepository.trackEvent(WishDetailedDeleteWishConfirmedEvent)
        viewModelScope.cancel()
    }

    fun onDeleteWishLinkConfirmed(link: String) {
        val currentLinks = uiState.value.toValueOfNull()?.wish?.links ?: return
        launchSafe {
            val newLinkString = LinksAdapter.removeLinkAndGetAccumulatedString(link, currentLinks)
            wishesRepository.updateWishLink(newLinkString, wishId)
        }
    }

    fun onLinkClicked() {
        analyticsRepository.trackEvent(WishDetailedWishLinkClickedEvent)
    }

    companion object {

        private const val KEY_LINK_INPUT_STRING = "KEY_LINK_INPUT_STRING"
    }
}