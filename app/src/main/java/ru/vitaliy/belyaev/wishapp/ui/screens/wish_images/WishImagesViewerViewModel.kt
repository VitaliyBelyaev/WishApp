package ru.vitaliy.belyaev.wishapp.ui.screens.wish_images

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_ID
import ru.vitaliy.belyaev.wishapp.navigation.ARG_WISH_IMAGE_INDEX
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel
class WishImagesViewerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val imagesRepository: ImagesRepository,
) : BaseViewModel() {

    private val wishId: String = requireNotNull(savedStateHandle[ARG_WISH_ID])

    private val _images: MutableStateFlow<List<ImageEntity>> = MutableStateFlow(emptyList())
    val images: StateFlow<List<ImageEntity>> = _images.asStateFlow()

    val initialWishImageIndex: Int = requireNotNull(savedStateHandle[ARG_WISH_IMAGE_INDEX])

    init {
        launchSafe {
            imagesRepository
                .observeImagesByWishId(wishId)
                .collect { _images.value = it }
        }
    }

    fun trackScreenShow() {
        // todo: Track
//        analyticsRepository.trackEvent(AboutAppScreenShowEvent)
    }
}