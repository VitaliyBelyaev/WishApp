package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.ShareText
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.utils.WishListCover
import ru.vitaliy.belyaev.wishapp.utils.WishListPdf
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider
import ru.vitaliy.belyaev.wishapp.utils.createShareImageWithTextIntent
import ru.vitaliy.belyaev.wishapp.utils.createSharePdfDocumentIntent
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent
import timber.log.Timber

/**
 * Собирает окно «Поделиться» для раздела списка. Заголовок отделяет «Сбылось» от хотелок, а ярлык
 * не называет: что за ярлыки у человека, получателю знать незачем. Часть списка — по тому же
 * разделу. Работает с контекстом приложения, запускает окно экран.
 */
class WishListSharer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val imagesRepository: ImagesRepository,
    private val dispatcherProvider: DispatcherProvider,
) {

    fun textIntent(section: ListSection, wishes: List<WishEntity>): Intent =
        createSharePlainTextIntent(text(section, wishes))

    suspend fun pdfIntent(section: ListSection, wishes: List<WishEntity>): Intent {
        val header = WishListPdf.Header(kicker = kicker(), title = title(section))
        val file = WishListPdf(context, imagesRepository).write(header, wishes)
        return createSharePdfDocumentIntent(file, context)
    }

    /** Обложка с первыми хотелками, а весь список со ссылками — подписью к ней. */
    suspend fun imageIntent(section: ListSection, wishes: List<WishEntity>): Intent {
        val file = WishListCover(context, imagesRepository).write(kicker(), title(section), wishes)
        return createShareImageWithTextIntent(file, text(section, wishes), context)
    }

    /**
     * Одна хотелка из её «⋮»: с фото — картинка с её фото и текст подписью, без фото — только текст.
     * Мелкая строка на картинке — тот же заголовок «Вот что я хочу», без двоеточия.
     * Картинка не собралась — уходит текст.
     */
    suspend fun wishIntent(wish: WishEntity): Intent = withContext(dispatcherProvider.default()) {
        val header = context.getString(R.string.share_header_wish)
        val text = ShareText.singleWishText(
            header = header,
            wish = wish,
            photoOnlyLabel = context.getString(R.string.card_photo_only),
            footer = context.getString(R.string.share_footer),
        )
        if (wish.images.isEmpty()) return@withContext createSharePlainTextIntent(text)
        try {
            val file = WishListCover(context, imagesRepository).writeWish(header.trimEnd(':', ' ', '\u00A0'), wish)
            createShareImageWithTextIntent(file, text, context)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.e(error)
            createSharePlainTextIntent(text)
        }
    }

    private fun text(section: ListSection, wishes: List<WishEntity>): String = ShareText.listText(
        header = context.getString(
            if (section == ListSection.Done) R.string.share_header_done else R.string.wish_list_title,
        ),
        wishes = wishes,
        photoOnlyLabel = context.getString(R.string.card_photo_only),
        footer = context.getString(R.string.share_footer),
    )

    private fun kicker() = context.getString(R.string.share_pdf_kicker_list)

    private fun title(section: ListSection) =
        context.getString(if (section == ListSection.Done) R.string.nav_done else R.string.nav_wishes)
}
