package ru.vitaliy.belyaev.wishapp.ui.screens.wish_images

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.WishImagesViewerScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel

@HiltViewModel(assistedFactory = WishImagesViewerViewModel.Factory::class)
class WishImagesViewerViewModel @AssistedInject constructor(
    @Assisted wishId: String,
    private val analyticsRepository: AnalyticsRepository,
    private val imagesRepository: ImagesRepository,
) : BaseViewModel() {

    private val _images: MutableStateFlow<List<ImageEntity>> = MutableStateFlow(emptyList())
    val images: StateFlow<List<ImageEntity>> = _images.asStateFlow()

    init {
        launchSafe {
            imagesRepository
                .observeImagesByWishId(wishId)
                .collect {
                    if (images.value.isEmpty() && it.isNotEmpty()) {
                        trackScreenShow(it.size)
                    }

                    _images.value = it
                }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(wishId: String): WishImagesViewerViewModel
    }

    private fun trackScreenShow(imagesCount: Int) {
        analyticsRepository.trackEvent(WishImagesViewerScreenShowEvent(imagesCount))
    }
}