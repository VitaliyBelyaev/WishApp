package ru.vitaliy.belyaev.wishapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Size
import androidx.annotation.FontRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import java.io.File
import kotlin.math.max
import kotlin.math.min
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.ShareItem
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.linkHost
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository

/**
 * PDF нового вида: листы A4, всегда светлые, чтобы читались и при чёрно-белой печати.
 * Сверху фирменный заголовок. У пункта главное фото слева от текста, остальные рядом под ним,
 * все целиком, без обрезки. Пункты пронумерованы, как в тексте: удобно сказать «мне третье».
 * Ярлыки не печатаются: какие они у человека, получателю знать незачем.
 * Внизу каждого листа мелкая подпись «Сделано в Хотелках». Ссылки нажимаются, их дописывает [PdfLinks].
 *
 * Рисуется прямо на холсте страницы, по одному пункту. Фото декодируется уменьшенным, только когда
 * до него дошла очередь, и сразу освобождается: память не растёт с длиной списка.
 */
class WishListPdf(private val context: Context, private val imagesRepository: ImagesRepository) {

    /** [kicker] — мелкая строка над заголовком, [title] — сам заголовок: «Вот мой список» и «Хотелки». */
    class Header(val kicker: String, val title: String)

    suspend fun write(header: Header, wishes: List<WishEntity>): File {
        val dir = File(context.filesDir, WISHLIST_PDF_DIR_NAME).apply { mkdirs() }
        val file = File(dir, "WishApp_wishlist_${System.currentTimeMillis()}.pdf")
        val document = PdfDocument()
        var links: List<List<PdfLinks.Link>>
        try {
            val pages = Pages(document)
            pages.drawHeader(header)
            wishes.forEachIndexed { index, wish ->
                pages.drawWish(wish, number = index + 1)
            }
            pages.finish()
            file.outputStream().use { document.writeTo(it) }
            links = pages.links
        } finally {
            document.close()
        }
        PdfLinks.append(file, links, PAGE_HEIGHT.toFloat())
        return file
    }

    private val ink = PdfInk.toArgb()
    private val muted = PdfMuted.toArgb()
    private val photoOnly = context.getString(R.string.card_photo_only)

    private fun font(@FontRes id: Int): Typeface = ResourcesCompat.getFont(context, id) ?: Typeface.DEFAULT

    private val kickerPaint = textPaint(font(R.font.onest_500), KICKER_SIZE, muted)
    private val titlePaint = textPaint(font(R.font.unbounded_700), TITLE_SIZE, ink)
    private val wishTitlePaint = textPaint(font(R.font.onest_600), WISH_TITLE_SIZE, ink)
    private val commentPaint = textPaint(font(R.font.onest_400), COMMENT_SIZE, muted)
    private val domainPaint = textPaint(font(R.font.onest_600), DOMAIN_SIZE, PdfAccent.toArgb())

    // Подчёркнута, чтобы было видно, что на неё можно нажать; цвет остаётся спокойным
    private val urlPaint = textPaint(font(R.font.onest_400), URL_SIZE, muted).apply { isUnderlineText = true }
    private val footerPaint = textPaint(font(R.font.onest_500), FOOTER_SIZE, muted)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfOutline.toArgb()
        strokeWidth = HAIRLINE
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    private fun textPaint(typeface: Typeface, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = size
        this.color = color
    }

    private fun layout(text: String, paint: TextPaint, width: Int, maxLines: Int = Int.MAX_VALUE): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, LINE_SPACING)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

    private inner class Pages(private val document: PdfDocument) {
        private var number = 0

        /** Нажимаемые места каждого листа, в координатах холста. */
        val links = mutableListOf<MutableList<PdfLinks.Link>>()
        private var page: PdfDocument.Page? = null
        private var y = 0f
        private val canvas: Canvas get() = requireNotNull(page).canvas

        init {
            newPage()
        }

        private fun newPage() {
            page?.let { finishPage(it) }
            number += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create())
            links += mutableListOf<PdfLinks.Link>()
            canvas.drawColor(PdfPaper.toArgb())
            y = MARGIN
        }

        private fun finishPage(current: PdfDocument.Page) {
            drawFooter(current.canvas)
            document.finishPage(current)
        }

        fun finish() {
            page?.let { finishPage(it) }
            page = null
        }

        private val bottom get() = PAGE_HEIGHT - MARGIN - FOOTER_ROOM

        fun drawHeader(header: Header) {
            val width = CONTENT_WIDTH.toInt()
            val kicker = layout(header.kicker, kickerPaint, width, maxLines = 1)
            val title = layout(header.title, titlePaint, width, maxLines = 2)
            drawLayout(kicker, MARGIN, y)
            y += kicker.height + KICKER_GAP
            drawLayout(title, MARGIN, y)
            y += title.height + HEADER_GAP
        }

        suspend fun drawWish(wish: WishEntity, number: Int) {
            val isFirst = number == 1
            val item = ShareItem.of(wish, photoOnly)
            val photos = wish.images.mapNotNull { image -> photoSize(image.id)?.let { image.id to it } }
            val main = photos.firstOrNull()?.let { (id, size) -> id to fit(size, MAIN_PHOTO_WIDTH, MAIN_PHOTO_HEIGHT) }
            val textX = MARGIN + if (main != null) main.second.width() + PHOTO_GAP else 0f
            val textWidth = (PAGE_WIDTH - MARGIN - textX).toInt()

            val blocks = textBlocks(item, number, textWidth)
            val textHeight = blocks.sumOf { (it.layout.height + it.gap).toDouble() }.toFloat()
            val height = max(textHeight, main?.second?.height() ?: 0f)

            // Между пунктами линия посередине отступа. Не влез — новый лист, там пункт сразу сверху
            if (!isFirst) y += ITEM_GAP / 2
            if (y + height > bottom && y > MARGIN + HEADER_ROOM_ON_PAGE) {
                newPage()
            } else if (!isFirst) {
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
                y += ITEM_GAP / 2
            }

            main?.let { (id, rect) -> drawPhoto(id, RectF(rect).apply { offset(MARGIN, y) }) }
            drawBlocks(blocks, textX)
            y += height
            drawOtherPhotos(photos.drop(1))
        }

        /** Текст пункта с [y] вниз; у ссылок запоминает нажимаемое место по ширине строк. */
        private fun drawBlocks(blocks: List<TextBlock>, x: Float) {
            var top = y
            blocks.forEach { block ->
                top += block.gap
                drawLayout(block.layout, x, top)
                if (block.link != null) {
                    val width = (0 until block.layout.lineCount).maxOf { block.layout.getLineWidth(it) }
                    links.last() += PdfLinks.Link(x, top, x + width, top + block.layout.height, block.link)
                }
                top += block.layout.height
            }
        }

        /** Остальные фото рядами под пунктом, одной высоты, ширина по пропорциям. Ряд не влез — на новый лист. */
        private suspend fun drawOtherPhotos(photos: List<Pair<String, Size>>) {
            var x = MARGIN
            var rowStarted = false
            photos.forEach { (id, size) ->
                val rect = fit(size, CONTENT_WIDTH, OTHER_PHOTO_HEIGHT)
                if (rowStarted && x + rect.width() > PAGE_WIDTH - MARGIN) {
                    y += OTHER_PHOTO_HEIGHT
                    x = MARGIN
                    rowStarted = false
                }
                if (!rowStarted) {
                    y += PHOTO_ROW_GAP
                    if (y + OTHER_PHOTO_HEIGHT > bottom) newPage()
                    rowStarted = true
                }
                drawPhoto(id, RectF(rect).apply { offset(x, y) })
                x += rect.width() + PHOTO_ROW_GAP
            }
            if (rowStarted) y += OTHER_PHOTO_HEIGHT
        }

        private suspend fun drawPhoto(imageId: String, rect: RectF) {
            val bitmap = decode(imageId, rect)
            val clip = Path().apply { addRoundRect(rect, PHOTO_CORNER, PHOTO_CORNER, Path.Direction.CW) }
            canvas.save()
            canvas.clipPath(clip)
            if (bitmap == null) {
                fillPaint.color = PdfPlaceholder.toArgb()
                canvas.drawRect(rect, fillPaint)
            } else {
                canvas.drawBitmap(bitmap, null, rect, photoPaint)
                bitmap.recycle()
            }
            canvas.restore()
        }

        private fun drawLayout(layout: StaticLayout, x: Float, top: Float) {
            canvas.save()
            canvas.translate(x, top)
            layout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawFooter(canvas: Canvas) {
        canvas.drawText(context.getString(R.string.share_footer), MARGIN, PAGE_HEIGHT - MARGIN, footerPaint)
    }

    /** Номер с названием, комментарий, и у каждой ссылки домен и полный адрес. */
    private fun textBlocks(item: ShareItem, number: Int, width: Int): List<TextBlock> = buildList {
        add(TextBlock(layout("$number. ${item.title}", wishTitlePaint, width, TITLE_MAX_LINES), 0f))
        if (item.comment.isNotEmpty()) {
            add(TextBlock(layout(item.comment, commentPaint, width, COMMENT_MAX_LINES), SMALL_GAP))
        }
        item.links.forEach { link ->
            val target = PdfLinks.target(link)
            add(TextBlock(layout(linkHost(link), domainPaint, width, maxLines = 1), SMALL_GAP, target))
            add(TextBlock(layout(link, urlPaint, width, URL_MAX_LINES), 1f, target))
        }
    }

    /** Кусок текста пункта: [gap] — отступ над ним, [link] — куда ведёт нажатие. */
    private class TextBlock(val layout: StaticLayout, val gap: Float, val link: String? = null)

    /** Размер фото без декодирования: по нему раскладывается пункт до отрисовки. */
    private suspend fun photoSize(imageId: String): Size? {
        val raw = imagesRepository.getImageRawData(imageId) ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(raw, 0, raw.size, bounds)
        return if (bounds.outWidth > 0 && bounds.outHeight > 0) Size(bounds.outWidth, bounds.outHeight) else null
    }

    /** Фото целиком в рамке [maxWidth] × [maxHeight], от левого верхнего угла. */
    private fun fit(size: Size, maxWidth: Float, maxHeight: Float): RectF {
        val scale = min(maxWidth / size.width, maxHeight / size.height)
        return RectF(0f, 0f, size.width * scale, size.height * scale)
    }

    /** Уменьшенное фото: с запасом под печать, примерно 300 точек на дюйм. */
    private suspend fun decode(imageId: String, rect: RectF): Bitmap? {
        val raw = imagesRepository.getImageRawData(imageId) ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(raw, 0, raw.size, bounds)
        val target = max(rect.width(), rect.height()) * PRINT_PIXELS_PER_POINT
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= target) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(raw, 0, raw.size, options)
    }

    private companion object {
        // Лист A4 в пунктах
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f
        const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
        const val FOOTER_ROOM = 28f
        const val HEADER_ROOM_ON_PAGE = 1f

        const val KICKER_SIZE = 11f
        const val TITLE_SIZE = 24f
        const val WISH_TITLE_SIZE = 13f
        const val COMMENT_SIZE = 10.5f
        const val DOMAIN_SIZE = 10f
        const val URL_SIZE = 7.5f
        const val FOOTER_SIZE = 8.5f
        const val LINE_SPACING = 1.15f

        const val KICKER_GAP = 4f
        const val HEADER_GAP = 22f
        const val ITEM_GAP = 28f
        const val SMALL_GAP = 4f
        const val HAIRLINE = 0.6f

        const val MAIN_PHOTO_WIDTH = 120f
        const val MAIN_PHOTO_HEIGHT = 140f
        const val OTHER_PHOTO_HEIGHT = 80f
        const val PHOTO_GAP = 14f
        const val PHOTO_ROW_GAP = 6f
        const val PHOTO_CORNER = 8f
        const val PRINT_PIXELS_PER_POINT = 4f

        const val TITLE_MAX_LINES = 4
        const val COMMENT_MAX_LINES = 12
        const val URL_MAX_LINES = 2
    }
}

// Светлые цвета темы: лист всегда светлый
private val PdfInk = Color(0xFF241A15)
private val PdfMuted = Color(0xFF66574E)
private val PdfAccent = Color(0xFFC2410C)
private val PdfOutline = Color(0xFFE3D3C8)
private val PdfPaper = Color(0xFFFFFFFF)
private val PdfPlaceholder = Color(0xFFF4E6DC)
