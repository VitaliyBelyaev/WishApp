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

    /** Текст в поле ссылки. Сохранится при уходе с экрана, если это ссылка. */
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

    /**
     * Экран хотелки закрыт: ссылка из поля сохраняется, все записи доходят до базы.
     * Возвращает хотелку, какой она стала, или `null`, если её нет.
     */
    suspend fun finish(wishId: String): WishEntity? {
        pendingLinks.remove(wishId)
            ?.let { LinkParser.parseLinkInput(it.trim()) }
            ?.takeIf { it.isNotEmpty() }
            ?.let { addLinks(wishId, it) }
        tails.remove(wishId)?.join()
        return wishesRepository.getWishByIdOrNull(wishId)
    }
}
