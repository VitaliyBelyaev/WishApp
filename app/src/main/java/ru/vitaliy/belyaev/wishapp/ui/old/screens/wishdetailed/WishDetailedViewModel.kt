package ru.vitaliy.belyaev.wishapp.ui.old.screens.wishdetailed

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import java.util.Optional
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.domain.model.ImageData
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.WishDetailedScreenShowEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.ageDays
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedAddImagesClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedAddLinkButtonClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteLinkClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteLinkConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedDeleteWishConfirmedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedImagesSelectedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedLinkClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.WishDetailedSaveAndExitClickedEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.LinkParser
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.SharedWishDraft
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.ui.core.viewmodel.BaseViewModel
import ru.vitaliy.belyaev.wishapp.ui.old.screens.wish_list.entity.WishItem
import ru.vitaliy.belyaev.wishapp.utils.toValueOfNull

@ExperimentalCoroutinesApi
@HiltViewModel(assistedFactory = WishDetailedViewModel.Factory::class)
class WishDetailedViewModel @AssistedInject constructor(
    @Assisted("wishId") wishId: String?,
    @Assisted("tagId") tagId: String?,
    @Assisted sharedDraft: SharedWishDraft?,
    private val savedStateHandle: SavedStateHandle,
    private val wishesRepository: WishesRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val imagesRepository: ImagesRepository,
    private val wishTagRelationRepository: WishTagRelationRepository,
) : BaseViewModel() {

    val inputWishId: String = wishId ?: ""
    val currentWishId: String = wishId?.takeIf { it.isNotBlank() }
        ?: savedStateHandle.get<String>(KEY_CURRENT_WISH_ID)?.takeIf { it.isNotBlank() }
        ?: createEmptyWish().id

    private val sharedDraftForNewWish: SharedWishDraft? = sharedDraft
    private val preselectedTagId: String? = tagId
    private val insertJob: Job? = if (inputWishId.isBlank()) {
        launchSafe { insertNewWishIfMissing() }
    } else {
        null
    }
    private var lastWriteJob: Job? = insertJob

    var linkInputString: String
        get() {
            return savedStateHandle[KEY_LINK_INPUT_STRING] ?: ""
        }
        private set(value) {
            savedStateHandle[KEY_LINK_INPUT_STRING] = value
        }

    val uiState: MutableStateFlow<Optional<WishItem>> = MutableStateFlow(Optional.empty())

    /** Ввод в поле ссылки, в котором не нашлось ссылки. Сбрасывается при правке поля. */
    val isLinkInputInvalid: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        savedStateHandle[KEY_CURRENT_WISH_ID] = currentWishId
        launchSafe {
            insertJob?.join()
            wishesRepository
                .observeWishById(currentWishId)
                .collect {
                    uiState.value = Optional.of(WishItem(it, false))
                }
        }
    }

    private suspend fun insertNewWishIfMissing() {
        if (wishesRepository.getWishByIdOrNull(currentWishId) != null) return

        val draft = sharedDraftForNewWish
        val newWish = createEmptyWish().copy(id = currentWishId)
        wishesRepository.insertWish(
            if (draft == null) {
                newWish
            } else {
                newWish.copy(
                    title = draft.title,
                    comment = draft.comment,
                    link = LinksAdapter.addLinksAndGetAccumulatedString(draft.links, emptyList()),
                    links = draft.links,
                )
            }
        )
        preselectedTagId?.let { tagId ->
            wishTagRelationRepository.insertWishTagRelation(
                wishId = currentWishId,
                tagId = tagId
            )
        }
    }

    /**
     * Записи идут строго по очереди: репозиторий пишет на IO, где порядок не гарантирован,
     * а UPDATE до завершения вставки нового желания не затронул бы ни одной строки.
     * Вызывается только с главного потока.
     */
    private fun launchWrite(block: suspend () -> Unit) {
        val previousWriteJob = lastWriteJob
        lastWriteJob = launchSafe {
            previousWriteJob?.join()
            block()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("wishId") wishId: String?,
            @Assisted("tagId") tagId: String?,
            @Assisted sharedDraft: SharedWishDraft?,
        ): WishDetailedViewModel
    }

    fun onAddImageClicked() {
        analyticsRepository.trackEvent(WishDetailedAddImagesClickedEvent)
    }

    fun onSaveAndExitClicked() {
        analyticsRepository.trackEvent(WishDetailedSaveAndExitClickedEvent)
    }

    fun onImagesSelected(uris: List<Uri>, contentResolver: ContentResolver) {
        analyticsRepository.trackEvent(WishDetailedImagesSelectedEvent(uris.size))

        launchSafe {
            withContext(Dispatchers.IO) {
                // Оригиналы обрабатываем по одному, чтобы не держать их в памяти все сразу
                val images = uris.mapNotNull { uri ->
                    readImageData(uri, contentResolver)?.let { imageRawData ->
                        val downscaledImageRawData = decreaseImageSizeIfNeeded(imageRawData)
                        ImageEntity(UUID.randomUUID().toString(), currentWishId, downscaledImageRawData)
                    }
                }
                imagesRepository.insertImages(images)
            }
        }
    }

    private fun readImageData(uri: Uri, contentResolver: ContentResolver): ImageData? {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            val rawData = inputStream.readBytes()
            val rotationDegrees = ByteArrayInputStream(rawData).use {
                ExifInterface(it).rotationDegrees
            }
            ImageData(
                rawData = rawData,
                rotationDegrees = rotationDegrees
            )
        }
    }

    fun trackScreenShow() {
        analyticsRepository.trackEvent(WishDetailedScreenShowEvent(isNewWish = inputWishId.isBlank()))
    }

    suspend fun prepareForExit(): String {
        savePendingLink()
        lastWriteJob?.join()
        return currentWishId
    }

    /**
     * Ссылка, оставленная в поле без нажатия «+», сохраняется при выходе, как название
     * и комментарий. Иначе она теряется, а новая хотелка только со ссылкой удаляется как пустая.
     * Если ссылки в поле не нашлось, ввод сохраняется как есть: при выходе лучше не терять текст.
     */
    private fun savePendingLink() {
        val input = linkInputString.trim()
        if (input.isEmpty()) return
        linkInputString = ""
        val links = LinkParser.parseLinkInput(input).ifEmpty { listOf(LinksAdapter.sanitizeLink(input)) }
        addLinks(links)
    }

    private fun addLinks(links: List<String>) {
        launchWrite {
            val currentLinks = wishesRepository.getWishByIdOrNull(currentWishId)?.links ?: return@launchWrite
            val newLinks = links.filter { it !in currentLinks }
            if (newLinks.isEmpty()) return@launchWrite
            val newLinkString = LinksAdapter.addLinksAndGetAccumulatedString(newLinks, currentLinks)
            wishesRepository.updateWishLink(newLinkString, currentWishId)
        }
    }

    fun onWishTitleChanged(newValue: String) {
        launchWrite {
            wishesRepository.updateWishTitle(newValue, currentWishId)
        }
    }

    fun onWishLinkChanged(newValue: String) {
        linkInputString = newValue
        isLinkInputInvalid.value = false
    }

    fun isAddLinkEnabled(input: String): Boolean {
        return input.isNotBlank()
    }

    /**
     * Ссылка проверяется при добавлении, а не только при открытии. Из текста со ссылкой
     * внутри берутся только адреса. Возвращает `true`, если ссылки добавлены и поле можно очистить.
     */
    fun onAddLinkClicked(input: String): Boolean {
        analyticsRepository.trackEvent(WishDetailedAddLinkButtonClickedEvent)
        val links = LinkParser.parseLinkInput(input)
        if (links.isEmpty()) {
            isLinkInputInvalid.value = true
            return false
        }
        linkInputString = ""
        isLinkInputInvalid.value = false
        addLinks(links)
        return true
    }

    fun onWishCommentChanged(newValue: String) {
        launchWrite {
            wishesRepository.updateWishComment(newValue, currentWishId)
        }
    }

    fun onDeleteWishConfirmed() {
        val wish = uiState.value.toValueOfNull()?.wish
        analyticsRepository.trackEvent(
            WishDetailedDeleteWishConfirmedEvent(
                wasDone = wish?.isCompleted ?: false,
                ageDays = wish?.ageDays(System.currentTimeMillis()) ?: 0,
                hasImages = wish?.images?.isNotEmpty() ?: false,
                hasLinks = wish?.links?.isNotEmpty() ?: false,
            )
        )
    }

    fun onDeleteWishLinkConfirmed(link: String) {
        analyticsRepository.trackEvent(WishDetailedDeleteLinkConfirmedEvent)
        val currentLinks = uiState.value.toValueOfNull()?.wish?.links ?: return
        launchWrite {
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