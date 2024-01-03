package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Optional
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.WishDetailedScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedAddLinkButtonClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteLinkClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteLinkConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteWishConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedLinkClickedEvent
import ru.vitaliy.belyaev.wishapp.utils.toValueOfNull
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_LINK
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem
import timber.log.Timber

@ExperimentalCoroutinesApi
@HiltViewModel
class WishDetailedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val imagesRepository: ImagesRepository,
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

    val images: MutableStateFlow<List<ImageEntity>> = MutableStateFlow(emptyList())

    init {
        val wishIdSetJob = launchSafe {
            wishId = inputWishId.ifBlank {
                val wish = createEmptyWish()
                wishesRepository.insertWish(wish)
                wish.id
            }
        }

        launchSafe {
            wishIdSetJob.join()
            wishesRepository
                .observeWishById(wishId)
                .collect {
                    uiState.value = Optional.of(WishItem(it, false))
                }
        }

        launchSafe {
            wishIdSetJob.join()
            imagesRepository
                .observeImagesByWishId(wishId)
                .collect {

                    Timber.tag("RTRT").d("imagesRepository event: ${it.size}")

                    images.value = it
                }
        }
    }

    fun onImageSelected(imageRawData: ByteArray) {

        Timber.tag("RTRT").d("onImageSelected: ${imageRawData.size}")
        launchSafe {
            withContext(Dispatchers.IO) {
                imagesRepository.insertImage(ImageEntity(UUID.randomUUID().toString(), wishId, imageRawData))
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
        analyticsRepository.trackEvent(WishDetailedAddLinkButtonClickedEvent)
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
        analyticsRepository.trackEvent(WishDetailedDeleteLinkConfirmedEvent)
        val currentLinks = uiState.value.toValueOfNull()?.wish?.links ?: return
        launchSafe {
            val newLinkString = LinksAdapter.removeLinkAndGetAccumulatedString(link, currentLinks)
            wishesRepository.updateWishLink(newLinkString, wishId)
        }
    }

    fun onLinkClicked() {
        analyticsRepository.trackEvent(WishDetailedLinkClickedEvent)
    }

    fun onDeleteLinkClicked() {
        analyticsRepository.trackEvent(WishDetailedDeleteLinkClickedEvent)
    }

    companion object {

        private const val KEY_LINK_INPUT_STRING = "KEY_LINK_INPUT_STRING"
    }
}