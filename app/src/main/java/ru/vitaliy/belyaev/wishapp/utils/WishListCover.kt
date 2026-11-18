package ru.vitaliy.belyaev.wishapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.annotation.FontRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import java.io.File
import kotlin.math.max
import kotlin.math.min
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.ShareItem
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository

/**
 * Картинка-обложка для «Поделиться картинкой»: заголовок, первые четыре хотелки списка сеткой
 * два на два с главным фото и номером, внизу «и ещё N · Сделано в Хотелках». Весь список
 * со ссылками уходит подписью к картинке, поэтому здесь только то, что цепляет глаз в ленте чата.
 *
 * Квадрат 1080 × 1080: WhatsApp показывает превью квадратом, Telegram близко к нему, и вытянутую
 * картинку оба обрезают. Всегда светлая, как PDF. Фото целиком, без обрезки, на мягкой подложке.
 * У хотелки без фото на подложке её название. Для одной хотелки — [writeWish].
 */
class WishListCover(private val context: Context, private val imagesRepository: ImagesRepository) {

    suspend fun write(kicker: String, title: String, wishes: List<WishEntity>): File = square { canvas ->
        val picked = wishes.take(MAX_TILES)
        val shown = picked.size
        val columns = if (shown == 1) 1 else 2
        val rows = (shown + columns - 1) / columns

        val gridTop = drawHeader(canvas, kicker, title, titlePaint)
        val tileWidth = (CONTENT_WIDTH - (columns - 1) * TILE_GAP) / columns
        // Сетка занимает всё место между шапкой и подписью внизу, но фото не выше квадрата
        val labelHeight = LABEL_GAP + labelPaint.fontSpacing
        val gridRoom = footerTop() - FOOTER_GAP - gridTop
        val photoHeight = min((gridRoom - (rows - 1) * ROW_GAP) / rows - labelHeight, tileWidth)
        val tileHeight = photoHeight + labelHeight

        picked.forEachIndexed { index, wish ->
            val item = ShareItem.of(wish, photoOnly)
            val row = index / columns
            // Последняя хотелка из нечётного числа занимает всю ширину
            val isWide = columns == 2 && index == shown - 1 && shown % 2 == 1
            val left = PADDING + (index % columns) * (tileWidth + TILE_GAP)
            val width = if (isWide) CONTENT_WIDTH.toFloat() else tileWidth
            val top = gridTop + row * (tileHeight + ROW_GAP)
            val photo = RectF(left, top, left + width, top + photoHeight)
            drawTile(canvas, photo, wish.images.firstOrNull()?.id, item.title)
            val label = TextUtils.ellipsize("${index + 1}. ${item.title}", labelPaint, width, TextUtils.TruncateAt.END)
            canvas.drawText(label.toString(), left, photo.bottom + LABEL_GAP - labelPaint.ascent(), labelPaint)
        }

        val rest = wishes.size - shown
        drawFooter(
            canvas,
            buildString {
                if (rest > 0) append(context.getString(R.string.share_image_more, rest)).append(" · ")
                append(context.getString(R.string.share_footer))
            },
        )
    }

    /**
     * Одна хотелка: шапка с её названием и фото крупно. Одно фото на всё место, два рядом,
     * три и больше — главное слева, два справа столбиком, на последнем «+N», если фото ещё есть.
     */
    suspend fun writeWish(kicker: String, wish: WishEntity): File = square { canvas ->
        val top = drawHeader(canvas, kicker, ShareItem.of(wish, photoOnly).title, wishTitlePaint)
        val area = RectF(PADDING, top, SIZE - PADDING, footerTop() - FOOTER_GAP)
        val ids = wish.images.map { it.id }
        when (ids.size) {
            1 -> drawTile(canvas, area, ids[0], "")
            2 -> {
                val half = (area.width() - TILE_GAP) / 2
                drawTile(canvas, RectF(area.left, area.top, area.left + half, area.bottom), ids[0], "")
                drawTile(canvas, RectF(area.right - half, area.top, area.right, area.bottom), ids[1], "")
            }
            else -> {
                val mainWidth = (area.width() - TILE_GAP) * MAIN_PHOTO_PART
                val sideHeight = (area.height() - TILE_GAP) / 2
                val sideLeft = area.left + mainWidth + TILE_GAP
                drawTile(canvas, RectF(area.left, area.top, area.left + mainWidth, area.bottom), ids[0], "")
                drawTile(canvas, RectF(sideLeft, area.top, area.right, area.top + sideHeight), ids[1], "")
                val last = RectF(sideLeft, area.bottom - sideHeight, area.right, area.bottom)
                drawTile(canvas, last, ids[2], "")
                if (ids.size > SHOWN_WISH_PHOTOS) drawMore(canvas, last, ids.size - SHOWN_WISH_PHOTOS)
            }
        }
        drawFooter(canvas, context.getString(R.string.share_footer))
    }

    /** Квадратная светлая картинка: рисует [draw], сохраняет в JPEG, прежнюю удаляет. */
    private suspend fun square(draw: suspend (Canvas) -> Unit): File {
        val bitmap = createBitmap(SIZE, SIZE)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(CoverBackground.toArgb())
            draw(canvas)
            val dir = File(context.filesDir, SHARE_IMAGE_DIR_NAME).apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val file = File(dir, "WishApp_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            return file
        } finally {
            bitmap.recycle()
        }
    }

    /** Мелкая строка и заголовок сверху; возвращает, где под ними начинается содержимое. */
    private fun drawHeader(canvas: Canvas, kicker: String, title: String, paint: TextPaint): Float {
        val kickerLayout = layout(kicker, kickerPaint, CONTENT_WIDTH, 1)
        val titleLayout = layout(title, paint, CONTENT_WIDTH, 2)
        var y = PADDING
        drawLayout(canvas, kickerLayout, PADDING, y)
        y += kickerLayout.height + KICKER_GAP
        drawLayout(canvas, titleLayout, PADDING, y)
        return y + titleLayout.height + HEADER_GAP
    }

    private fun footerTop() = SIZE - PADDING - footerPaint.fontSpacing

    private fun drawFooter(canvas: Canvas, text: String) {
        canvas.drawText(text, PADDING, footerTop() - footerPaint.ascent(), footerPaint)
    }

    /** Затемнение с «+N» поверх последнего фото: столько ещё у хотелки. */
    private fun drawMore(canvas: Canvas, rect: RectF, count: Int) {
        canvas.drawRoundRect(rect, CORNER, CORNER, dimPaint)
        val text = "+$count"
        val baseline = rect.centerY() - (morePaint.ascent() + morePaint.descent()) / 2
        canvas.drawText(text, rect.centerX() - morePaint.measureText(text) / 2, baseline, morePaint)
    }

    private val photoOnly = context.getString(R.string.card_photo_only)

    private fun font(@FontRes id: Int): Typeface = ResourcesCompat.getFont(context, id) ?: Typeface.DEFAULT

    private val kickerPaint = textPaint(font(R.font.onest_500), KICKER_SIZE, CoverMuted.toArgb())
    private val titlePaint = textPaint(font(R.font.unbounded_700), TITLE_SIZE, CoverInk.toArgb())
    private val wishTitlePaint = textPaint(font(R.font.unbounded_700), WISH_TITLE_SIZE, CoverInk.toArgb())
    private val morePaint = textPaint(font(R.font.unbounded_700), WISH_TITLE_SIZE, android.graphics.Color.WHITE)
    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CoverInk.copy(alpha = DIM_ALPHA).toArgb() }
    private val labelPaint = textPaint(font(R.font.onest_600), LABEL_SIZE, CoverInk.toArgb())
    private val placeholderPaint = textPaint(font(R.font.onest_600), PLACEHOLDER_SIZE, CoverMuted.toArgb())
    private val footerPaint = textPaint(font(R.font.onest_500), FOOTER_SIZE, CoverMuted.toArgb())
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CoverPlaceholder.toArgb() }
    private val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    private fun textPaint(typeface: Typeface, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = size
        this.color = color
    }

    private fun layout(text: String, paint: TextPaint, width: Int, maxLines: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

    private fun drawLayout(canvas: Canvas, layout: StaticLayout, x: Float, top: Float) {
        canvas.save()
        canvas.translate(x, top)
        layout.draw(canvas)
        canvas.restore()
    }

    /** Подложка, на ней фото [imageId] целиком или, если фото нет, [title] по центру. */
    private suspend fun drawTile(canvas: Canvas, rect: RectF, imageId: String?, title: String) {
        canvas.save()
        canvas.clipPath(Path().apply { addRoundRect(rect, CORNER, CORNER, Path.Direction.CW) })
        canvas.drawRect(rect, fillPaint)
        val bitmap = imageId?.let { decode(it, rect) }
        if (bitmap != null) {
            val scale = min(rect.width() / bitmap.width, rect.height() / bitmap.height)
            val width = bitmap.width * scale
            val height = bitmap.height * scale
            val target = RectF(0f, 0f, width, height).apply {
                offset(rect.centerX() - width / 2, rect.centerY() - height / 2)
            }
            canvas.drawBitmap(bitmap, null, target, photoPaint)
            bitmap.recycle()
        } else if (title.isNotEmpty()) {
            val inner = (rect.width() - 2 * PLACEHOLDER_PADDING).toInt()
            val text = layout(title, placeholderPaint, inner, PLACEHOLDER_MAX_LINES)
            val textWidth = (0 until text.lineCount).maxOf { text.getLineWidth(it) }
            drawLayout(canvas, text, rect.centerX() - textWidth / 2, rect.centerY() - text.height / 2f)
        }
        canvas.restore()
    }

    private suspend fun decode(imageId: String, rect: RectF): Bitmap? {
        val raw = imagesRepository.getImageRawData(imageId) ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(raw, 0, raw.size, bounds)
        val target = max(rect.width(), rect.height())
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= target) sample *= 2
        return BitmapFactory.decodeByteArray(raw, 0, raw.size, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private companion object {
        const val SIZE = 1080
        const val PADDING = 64f
        const val CONTENT_WIDTH = (SIZE - 2 * PADDING).toInt()

        const val KICKER_SIZE = 34f
        const val TITLE_SIZE = 68f
        const val WISH_TITLE_SIZE = 56f
        const val LABEL_SIZE = 34f
        const val PLACEHOLDER_SIZE = 40f
        const val FOOTER_SIZE = 30f

        const val KICKER_GAP = 8f
        const val HEADER_GAP = 32f
        const val TILE_GAP = 24f
        const val ROW_GAP = 24f
        const val LABEL_GAP = 14f
        const val FOOTER_GAP = 28f
        const val CORNER = 28f
        const val PLACEHOLDER_PADDING = 32f
        const val PLACEHOLDER_MAX_LINES = 3
        const val JPEG_QUALITY = 90

        const val MAX_TILES = 4
        const val SHOWN_WISH_PHOTOS = 3
        const val MAIN_PHOTO_PART = 0.62f
        const val DIM_ALPHA = 0.55f
    }
}

const val SHARE_IMAGE_DIR_NAME = "share_image"

// Светлые цвета темы: картинка всегда светлая
private val CoverBackground = Color(0xFFFFF9F5)
private val CoverInk = Color(0xFF241A15)
private val CoverMuted = Color(0xFF66574E)
private val CoverPlaceholder = Color(0xFFF4E6DC)
