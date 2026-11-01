package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import java.util.Optional
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.domain.model.ImageData
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.WishDetailedScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedAddImagesClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedAddLinkButtonClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteLinkClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteLinkConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteWishConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedImagesSelectedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedLinkClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedSaveAndExitClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishItem
import ru.vitaliy.belyaev.wishapp.utils.toValueOfNull

@ExperimentalCoroutinesApi
@HiltViewModel(assistedFactory = WishDetailedViewModel.Factory::class)
class WishDetailedViewModel @AssistedInject constructor(
    @Assisted("wishId") wishId: String?,
    @Assisted("wishLink") wishLink: String?,
    @Assisted("tagId") tagId: String?,
    private val savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val imagesRepository: ImagesRepository,
    private val wishTagRelationRepository: WishTagRelationRepository,
) : BaseViewModel() {

    val inputWishId: String = wishId ?: ""
    var currentWishId: String = ""
        private set

    private val sharedLinkForNewWish: String = wishLink ?: ""
    private val preselectedTagId: String? = tagId
    private val isNewWish: Boolean = wishId.isNullOrBlank()

    private val restoredWishId: String? =
        savedStateHandle.get<String>(KEY_CURRENT_WISH_ID)?.takeIf { it.isNotBlank() }
    private val newWish = if (isNewWish && restoredWishId == null) {
        createEmptyWish()
    } else {
        null
    }
    private var insertJob: Job? = null

    var linkInputString: String
        get() {
            return savedStateHandle[KEY_LINK_INPUT_STRING] ?: sharedLinkForNewWish
        }
        private set(value) {
            savedStateHandle[KEY_LINK_INPUT_STRING] = value
        }

    val uiState: MutableStateFlow<Optional<WishItem>> = MutableStateFlow(Optional.empty())

    init {
        currentWishId = when {
            !wishId.isNullOrBlank() -> wishId
            restoredWishId != null -> restoredWishId
            else -> checkNotNull(newWish).id
        }
        savedStateHandle[KEY_CURRENT_WISH_ID] = currentWishId
        launchObserving()
    }

    private fun launchObserving() {
        insertJob = if (isNewWish) {
            launchSafe {
                if (newWish != null) {
                    wishesRepository.insertWish(newWish)
                    insertPreselectedTag()
                } else if (wishesRepository.getWishByIdOrNull(currentWishId) == null) {
                    wishesRepository.insertWish(createEmptyWish().copy(id = currentWishId))
                    insertPreselectedTag()
                }
            }
        } else {
            null
        }

        launchSafe {
            insertJob?.join()
            wishesRepository
                .observeWishById(currentWishId)
                .collect {
                    uiState.value = Optional.of(WishItem(it, false))
                }
        }
    }

    private suspend fun insertPreselectedTag() {
        preselectedTagId?.let { tagId ->
            wishTagRelationRepository.insertWishTagRelation(
                wishId = currentWishId,
                tagId = tagId
            )
        }
    }

    suspend fun awaitWishPersisted() {
        insertJob?.join()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("wishId") wishId: String?,
            @Assisted("wishLink") wishLink: String?,
            @Assisted("tagId") tagId: String?
        ): WishDetailedViewModel
    }

    fun onAddImageClicked() {
        analyticsRepository.trackEvent(WishDetailedAddImagesClickedEvent)
    }

    fun onSaveAndExitClicked() {
        analyticsRepository.trackEvent(WishDetailedSaveAndExitClickedEvent)
    }

    fun onImagesSelected(imagesRawData: List<ImageData>) {
        analyticsRepository.trackEvent(WishDetailedImagesSelectedEvent(imagesRawData.size))

        launchSafe {
            withContext(Dispatchers.IO) {
                val images = imagesRawData.map { imageRawData ->
                    val downscaledImageRawData = decreaseImageSizeIfNeeded(imageRawData)
                    ImageEntity(UUID.randomUUID().toString(), currentWishId, downscaledImageRawData)
                }
                imagesRepository.insertImages(images)
            }
        }
    }

    fun trackScreenShow() {
        analyticsRepository.trackEvent(WishDetailedScreenShowEvent(isNewWish = inputWishId.isBlank()))
    }

    fun onBackPressed() {
        viewModelScope.cancel()
    }

    suspend fun prepareForExit(): String {
        awaitWishPersisted()
        val wishId = currentWishId
        onBackPressed()
        return wishId
    }

    fun onWishTitleChanged(newValue: String) {
        launchSafe {
            wishesRepository.updateWishTitle(newValue, currentWishId)
        }
    }

    fun onWishLinkChanged(newValue: String) {
        linkInputString = newValue
    }

    fun isLinkValid(link: String): Boolean {
        return link.isNotBlank()
    }

    fun onAddLinkClicked(link: String) {
        analyticsRepository.trackEvent(WishDetailedAddLinkButtonClickedEvent)
        val currentLinks = uiState.value.toValueOfNull()?.wish?.links ?: return

        launchSafe {
            val newLinkString = LinksAdapter.addLinkAndGetAccumulatedString(link, currentLinks)
            wishesRepository.updateWishLink(newLinkString, currentWishId)
        }
    }

    fun onWishCommentChanged(newValue: String) {
        launchSafe {
            wishesRepository.updateWishComment(newValue, currentWishId)
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
            wishesRepository.updateWishLink(newLinkString, currentWishId)
        }
    }

    fun onLinkClicked() {
        analyticsRepository.trackEvent(WishDetailedLinkClickedEvent)
    }

    fun onDeleteLinkClicked() {
        analyticsRepository.trackEvent(WishDetailedDeleteLinkClickedEvent)
    }

    private fun decreaseImageSizeIfNeeded(imageRawData: ImageData): ByteArray {
        val options = Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageRawData.rawData, 0, imageRawData.rawData.size, options)

        val biggerSidePixelSize = maxOf(options.outWidth, options.outHeight)
        val sampleSize = if (biggerSidePixelSize <= IMAGE_BIGGER_SIDE_PIXELS_LIMIT) {
            1
        } else {
            (biggerSidePixelSize.toDouble() / IMAGE_BIGGER_SIDE_PIXELS_LIMIT.toDouble())
                .toBigDecimal()
                .setScale(0, RoundingMode.HALF_UP).toInt()
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = sampleSize
        val downsampledAndRotatedBitmap = createBitmap(imageRawData, options)
        val downsampledImageRawData = ByteArrayOutputStream().use {
            downsampledAndRotatedBitmap.compress(Bitmap.CompressFormat.JPEG, BITMAP_COMPRESS_QUALITY, it)
            it.toByteArray()
        }
        downsampledAndRotatedBitmap.recycle()
        return downsampledImageRawData
    }

    private fun createBitmap(
        imageRawData: ImageData,
        options: Options,
    ): Bitmap {
        val downsampledBitmap = BitmapFactory.decodeByteArray(
            imageRawData.rawData,
            0,
            imageRawData.rawData.size,
            options
        )
        return if (imageRawData.rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(imageRawData.rotationDegrees.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(
                downsampledBitmap,
                0,
                0,
                downsampledBitmap.width,
                downsampledBitmap.height,
                matrix,
                true
            )
            downsampledBitmap.recycle()
            rotatedBitmap
        } else {
            downsampledBitmap
        }
    }

    companion object {

        private const val KEY_LINK_INPUT_STRING = "KEY_LINK_INPUT_STRING"
        private const val KEY_CURRENT_WISH_ID = "KEY_CURRENT_WISH_ID"
        private const val IMAGE_BIGGER_SIDE_PIXELS_LIMIT = 1280

        // Выбрано такое значения из рассчета, что семплированная картинка
        // с размером по большей строне 1280 будет в итоге занимать места в диапазоне 250-300 кБ
        private const val BITMAP_COMPRESS_QUALITY = 75
    }
}