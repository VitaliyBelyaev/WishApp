package ru.vitaliy.belyaev.wishapp.ui.screens.wish

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.data.image.PickedImageReader
import ru.vitaliy.belyaev.wishapp.shared.domain.LinkParser
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import timber.log.Timber

/**
 * Правки хотелки с экрана нового вида, которые переживают сам экран.
 *
 * «Назад» системным жестом закрывает экран сразу, без ожидания: так работает предиктивный «назад».
 * Поэтому записи идут не в области ViewModel, а здесь, по очереди для каждой хотелки, и список,
 * получив хотелку обратно, дожидается их в [finish]. Ссылка, оставленная в поле, тоже ждёт здесь.
 * Вызывается только с главного потока.
 */
@Singleton
class WishWriteQueue @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val imagesRepository: ImagesRepository,
    private val dispatcherProvider: DispatcherProvider,
) {
    private val scope = CoroutineScope(
        SupervisorJob() + dispatcherProvider.main() + CoroutineExceptionHandler { _, throwable ->
            if (BuildConfig.DEBUG) throw throwable
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Timber.e(throwable.toString())
        },
    )
    private val tails = HashMap<String, Job>()
    private val pendingLinks = HashMap<String, String>()

    private val _hiddenImages = MutableStateFlow<Set<String>>(emptySet())

    /**
     * Фото, удалённые с «Вернуть»: пока снэкбар на экране, их не видно, но в базе они есть.
     * Удаление переживает просмотр: снэкбар остаётся и после его закрытия.
     */
    val hiddenImages: StateFlow<Set<String>> = _hiddenImages.asStateFlow()

    /**
     * Записи одной хотелки строго по очереди: репозиторий пишет на IO, где порядок не гарантирован,
     * и правка названия могла бы лечь раньше предыдущей.
     */
    fun write(wishId: String, block: suspend () -> Unit) {
        val previous = tails[wishId]
        tails[wishId] = scope.launch {
            previous?.join()
            block()
        }
    }

    /** Текст в поле ссылки. Сохранится при уходе с экрана, ссылка это или нет. */
    fun setPendingLink(wishId: String, text: String) {
        if (text.isBlank()) pendingLinks.remove(wishId) else pendingLinks[wishId] = text
    }

    /** Добавляет ссылки, которых у хотелки ещё нет, в конец. */
    fun addLinks(wishId: String, links: List<String>) {
        write(wishId) {
            val current = wishesRepository.getWishByIdOrNull(wishId)?.links ?: return@write
            val newLinks = links.filter { it !in current }
            if (newLinks.isEmpty()) return@write
            wishesRepository.updateWishLink(LinksAdapter.addLinksAndGetAccumulatedString(newLinks, current), wishId)
        }
    }

    /**
     * Фото читаются и пишутся вне очереди: они долгие, а ждать их при выходе не нужно.
     * [onFailed], если какое-то фото прочитать не вышло.
     */
    fun addImages(wishId: String, uris: List<Uri>, contentResolver: ContentResolver, onFailed: () -> Unit) {
        scope.launch {
            val images = withContext(dispatcherProvider.io()) {
                uris.mapNotNull { uri ->
                    runCatching { PickedImageReader.read(uri, contentResolver) }.getOrNull()
                        ?.let { ImageEntity(UUID.randomUUID().toString(), wishId, it) }
                }
            }
            if (images.isNotEmpty()) imagesRepository.insertImages(images)
            if (images.size < uris.size) onFailed()
        }
    }

    /** Делает фото главным: оно встаёт первым. */
    fun setMainImage(wishId: String, imageId: String) {
        write(wishId) { imagesRepository.setMainImage(wishId, imageId) }
    }

    /** Прячет фото до решения в снэкбаре: [restoreImage] или [deleteHiddenImage]. */
    fun hideImage(imageId: String) {
        _hiddenImages.update { it + imageId }
    }

    fun restoreImage(imageId: String) {
        _hiddenImages.update { it - imageId }
    }

    /** Снэкбар закрылся без «Вернуть»: фото удаляется, и только потом перестаёт числиться скрытым. */
    fun deleteHiddenImage(wishId: String, imageId: String) {
        write(wishId) {
            imagesRepository.deleteImageById(imageId)
            _hiddenImages.update { it - imageId }
        }
    }

    /**
     * Экран хотелки закрыт: текст из поля ссылки сохраняется, все записи доходят до базы.
     * Возвращает хотелку, какой она стала, или `null`, если её нет.
     */
    suspend fun finish(wishId: String): WishEntity? {
        // Текст из поля сохраняется, даже если не похож на ссылку: проверка только подсказывает
        pendingLinks.remove(wishId)
            ?.let { LinkParser.linksToSave(it) }
            ?.takeIf { it.isNotEmpty() }
            ?.let { addLinks(wishId, it) }
        tails.remove(wishId)?.join()
        return wishesRepository.getWishByIdOrNull(wishId)
    }
}
