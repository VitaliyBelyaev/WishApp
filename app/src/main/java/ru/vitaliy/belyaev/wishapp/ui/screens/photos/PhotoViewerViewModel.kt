package ru.vitaliy.belyaev.wishapp.ui.screens.photos

import android.content.ContentResolver
import android.net.Uri
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.WishImagesViewerScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wish.WishWriteQueue

/** Разовые события просмотра: всё это сообщения в общем снэкбаре. */
sealed interface PhotoViewerEvent {
    data object MainSet : PhotoViewerEvent
    data object AddFailed : PhotoViewerEvent
    class Deleted(val onUndo: () -> Unit, val onConfirm: () -> Unit) : PhotoViewerEvent
}

/**
 * Просмотр фото хотелки нового вида. Правки идут через [WishWriteQueue], как с экрана хотелки:
 * удаление с «Вернуть» переживает сам просмотр.
 */
@HiltViewModel(assistedFactory = PhotoViewerViewModel.Factory::class)
class PhotoViewerViewModel @AssistedInject constructor(
    @Assisted private val wishId: String,
    imagesRepository: ImagesRepository,
    private val writeQueue: WishWriteQueue,
    private val analyticsRepository: AnalyticsRepository,
) : BaseViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(wishId: String): PhotoViewerViewModel
    }

    private val _images = MutableStateFlow<List<ImageRef>?>(null)

    /** Фото по порядку, первое главное. `null`, пока не загрузилось. */
    val images: StateFlow<List<ImageRef>?> = _images.asStateFlow()

    private val eventChannel = Channel<PhotoViewerEvent>(Channel.BUFFERED)
    val events: Flow<PhotoViewerEvent> = eventChannel.receiveAsFlow()

    init {
        launchSafe {
            combine(imagesRepository.observeImageRefsByWishId(wishId), writeQueue.hiddenImages) { images, hidden ->
                images.filterNot { it.id in hidden }
            }.collect { images ->
                if (_images.value == null && images.isNotEmpty()) {
                    analyticsRepository.trackEvent(WishImagesViewerScreenShowEvent(images.size))
                }
                _images.value = images
            }
        }
    }

    fun onMakeMain(image: ImageRef) {
        if (_images.value?.firstOrNull()?.id == image.id) return
        writeQueue.setMainImage(wishId, image.id)
        eventChannel.trySend(PhotoViewerEvent.MainSet)
    }

    fun onDelete(image: ImageRef) {
        writeQueue.hideImage(image.id)
        eventChannel.trySend(
            PhotoViewerEvent.Deleted(
                onUndo = { writeQueue.restoreImage(image.id) },
                onConfirm = { writeQueue.deleteHiddenImage(wishId, image.id) },
            ),
        )
    }

    fun onImagesPicked(uris: List<Uri>, contentResolver: ContentResolver) {
        if (uris.isEmpty()) return
        writeQueue.addImages(wishId, uris, contentResolver) { eventChannel.trySend(PhotoViewerEvent.AddFailed) }
    }
}
