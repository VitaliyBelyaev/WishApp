package ru.vitaliy.belyaev.wishapp.data

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish

/** Работает с отдельным файлом базы и не затрагивает данные приложения. */
class DatabaseRepositoryImagesTest {

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseFile = File(targetContext.cacheDir, "images_test.db")
    private val repository = WishAppSdk(
        DatabaseDriverFactory(
            object : ContextWrapper(targetContext) {
                override fun getDatabasePath(name: String): File = databaseFile
            },
        ),
    ).getDatabaseRepository()

    @After
    fun tearDown() {
        targetContext.deleteDatabase(databaseFile.absolutePath)
    }

    @Test
    fun wishesContainImageRefsInInsertionOrder() = runBlocking {
        val wish = createEmptyWish().copy(title = "With images")
        repository.insertWish(wish)
        repository.insertWish(createEmptyWish().copy(title = "Without images"))
        repository.insertImages(
            listOf(
                ImageEntity("z", wish.id, byteArrayOf(1)),
                ImageEntity("a", wish.id, byteArrayOf(2)),
            ),
        )
        val expected = listOf(ImageRef("z", wish.id), ImageRef("a", wish.id))

        assertEquals(expected, repository.getWishById(wish.id).images)
        assertEquals(expected, repository.observeWishById(wish.id).first().images)
        assertEquals(expected, repository.observeImageRefsByWishId(wish.id).first())
        val allWishes = repository.observeAllWishes(isCompleted = false).first()
        assertEquals(expected, allWishes.first { it.id == wish.id }.images)
        assertEquals(emptyList<ImageRef>(), allWishes.first { it.id != wish.id }.images)
    }

    @Test
    fun getImageRawData_returnsBytesUntilImageIsDeleted() = runBlocking {
        val wish = createEmptyWish()
        val rawData = byteArrayOf(1, 2, 3)
        repository.insertWish(wish)
        repository.insertImage(ImageEntity("image", wish.id, rawData))

        assertArrayEquals(rawData, repository.getImageRawData("image"))

        repository.deleteImageById("image")

        assertNull(repository.getImageRawData("image"))
    }
}
