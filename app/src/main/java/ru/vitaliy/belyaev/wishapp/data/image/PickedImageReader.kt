package ru.vitaliy.belyaev.wishapp.data.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import ru.vitaliy.belyaev.wishapp.domain.model.ImageData

/**
 * Фото из галереи или другого приложения: читает файл по `content://`, поворачивает по EXIF
 * и уменьшает до 1280 px по большей стороне. Одно на оба вида, чтобы фото были одинаковыми.
 * Читать сразу и по одному: доступ к чужому файлу временный, а оригиналы большие.
 */
object PickedImageReader {

    private const val IMAGE_BIGGER_SIDE_PIXELS_LIMIT = 1280

    // Выбрано такое значения из рассчета, что семплированная картинка
    // с размером по большей строне 1280 будет в итоге занимать места в диапазоне 250-300 кБ
    private const val BITMAP_COMPRESS_QUALITY = 75

    /** Уменьшенное фото в JPEG или `null`, если файл не открылся. Вызывать не в главном потоке. */
    fun read(uri: Uri, contentResolver: ContentResolver): ByteArray? {
        val imageData = readImageData(uri, contentResolver) ?: return null
        return decreaseImageSizeIfNeeded(imageData)
    }

    private fun readImageData(uri: Uri, contentResolver: ContentResolver): ImageData? {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            val rawData = inputStream.readBytes()
            val rotationDegrees = ByteArrayInputStream(rawData).use {
                ExifInterface(it).rotationDegrees
            }
            ImageData(
                rawData = rawData,
                rotationDegrees = rotationDegrees,
            )
        }
    }

    private fun decreaseImageSizeIfNeeded(imageRawData: ImageData): ByteArray? {
        val options = BitmapFactory.Options().apply {
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
        val downsampledAndRotatedBitmap = createBitmap(imageRawData, options) ?: return null
        val downsampledImageRawData = ByteArrayOutputStream().use {
            downsampledAndRotatedBitmap.compress(Bitmap.CompressFormat.JPEG, BITMAP_COMPRESS_QUALITY, it)
            it.toByteArray()
        }
        downsampledAndRotatedBitmap.recycle()
        return downsampledImageRawData
    }

    /** `null`, если формат не декодируется. */
    private fun createBitmap(imageRawData: ImageData, options: BitmapFactory.Options): Bitmap? {
        val downsampledBitmap = BitmapFactory.decodeByteArray(
            imageRawData.rawData,
            0,
            imageRawData.rawData.size,
            options,
        )
        if (downsampledBitmap == null || imageRawData.rotationDegrees == 0) return downsampledBitmap

        val matrix = Matrix()
        matrix.postRotate(imageRawData.rotationDegrees.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(
            downsampledBitmap,
            0,
            0,
            downsampledBitmap.width,
            downsampledBitmap.height,
            matrix,
            true,
        )
        downsampledBitmap.recycle()
        return rotatedBitmap
    }
}
