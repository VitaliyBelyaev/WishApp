package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.domain.GetLinkPreviewInteractor
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.entity.toValueOfNull
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.createEmptyWish
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
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
    private val getLinkPreviewInteractor: GetLinkPreviewInteractor
) : ViewModel() {

    val inputWishId: String = savedStateHandle[ARG_WISH_ID] ?: ""
    lateinit var wishId: String

    val uiState: MutableStateFlow<Optional<WishItem>> = MutableStateFlow(Optional.empty())
    private var cachedLinkPreviewState: LinkPreviewState = None

    init {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "WishDetailed")
        }

        viewModelScope.launch {
            wishId = if (inputWishId.isNotBlank()) {
                inputWishId
            } else {
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

    private suspend fun tryLoadLinkPreview(link: String, wish: WishWithTags) {
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
        viewModelScope.launch {
            wishesRepository.updateWishTitle(newValue, wishId)
        }
    }

    fun onWishLinkChanged(newValue: String) {
        viewModelScope.launch {
            wishesRepository.updateWishLink(newValue, wishId)
        }
    }

    fun onWishCommentChanged(newValue: String) {
        viewModelScope.launch {
            wishesRepository.updateWishComment(newValue, wishId)
        }
    }

    fun onDeleteWishClicked() {
        Firebase.analytics.logEvent("delete_tag_from_wish_detailed", null)
        viewModelScope.cancel()
    }
}