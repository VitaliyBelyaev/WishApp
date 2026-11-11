package ru.vitaliy.belyaev.wishapp.profiling

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish

/**
 * Наполняет базу для замера памяти, сам ничего не меряет: см. scripts/profile_images_memory.py.
 *
 * Запускается только с аргументом `images`, чтобы не стирать данные при обычном прогоне тестов.
 */
@RunWith(AndroidJUnit4::class)
class ImageMemorySeedTest {

    private val arguments = InstrumentationRegistry.getArguments()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun seedDatabase() = runBlocking {
        val imagesCount = arguments.getString("images")?.toInt()
        assumeTrue(imagesCount != null)
        val repository = WishAppSdk(DatabaseDriverFactory(context)).getDatabaseRepository()

        // 300 кБ на картинку совпадает с оценкой в WishDetailedViewModel.BITMAP_COMPRESS_QUALITY
        val samples = List(SAMPLES_COUNT) { createJpeg(1280, 960, quality = 75, noise = 22, seed = it) }
        println("seed: sample sizes ${samples.map { it.size }}")

        var inserted = 0
        var wishIndex = 0
        while (inserted < imagesCount!!) {
            val wish = createEmptyWish().copy(
                title = "Желание $wishIndex",
                comment = "Комментарий к желанию $wishIndex",
            )
            repository.insertWish(wish)
            val images = List(minOf(IMAGES_PER_WISH, imagesCount - inserted)) {
                ImageEntity(UUID.randomUUID().toString(), wish.id, samples[(inserted + it) % samples.size])
            }
            repository.insertImages(images)
            inserted += images.size
            wishIndex++
        }
        println("seed: $wishIndex wishes, $inserted images")
    }

    /** Кладёт в галерею снимки размером как с камеры, чтобы выбирать их в системном пикере. */
    @Test
    fun seedGallery() {
        assumeTrue(arguments.getString("gallery") != null)
        val resolver = context.contentResolver
        repeat(GALLERY_PHOTOS_COUNT) { index ->
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "profiling_$index.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
            val jpeg = createJpeg(4000, 3000, quality = 90, noise = 30, seed = 100 + index)
            resolver.openOutputStream(uri)!!.use { it.write(jpeg) }
            println("seed: gallery photo $index, ${jpeg.size} bytes")
        }
    }

    private fun createJpeg(width: Int, height: Int, quality: Int, noise: Int, seed: Int): ByteArray {
        val random = Random(seed)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        repeat(BLOCKS_COUNT) {
            paint.color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
            val left = random.nextInt(width).toFloat()
            val top = random.nextInt(height).toFloat()
            canvas.drawRect(left, top, left + width / 8f, top + height / 8f, paint)
        }
        val row = IntArray(width)
        for (y in 0 until height) {
            bitmap.getPixels(row, 0, width, 0, y, width, 1)
            for (x in row.indices) {
                val delta = random.nextInt(-noise, noise + 1)
                val color = row[x]
                row[x] = Color.rgb(
                    (Color.red(color) + delta).coerceIn(0, 255),
                    (Color.green(color) + delta).coerceIn(0, 255),
                    (Color.blue(color) + delta).coerceIn(0, 255),
                )
            }
            bitmap.setPixels(row, 0, width, 0, y, width, 1)
        }
        val jpeg = ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
            it.toByteArray()
        }
        bitmap.recycle()
        return jpeg
    }

    private companion object {
        const val SAMPLES_COUNT = 8
        const val IMAGES_PER_WISH = 3
        const val GALLERY_PHOTOS_COUNT = 5
        const val BLOCKS_COUNT = 400
    }
}
