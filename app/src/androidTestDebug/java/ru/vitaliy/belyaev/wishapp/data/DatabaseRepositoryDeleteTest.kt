package ru.vitaliy.belyaev.wishapp.data

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish

/** Работает с отдельным файлом базы и не затрагивает данные приложения. */
class DatabaseRepositoryDeleteTest {

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseFile = File(targetContext.cacheDir, "delete_test.db")
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
    fun deleteWishesByIds_skipsMissingWishes() = runBlocking {
        val deletedWish = createEmptyWish().copy(title = "Deleted")
        val existingWish = createEmptyWish().copy(title = "Existing")
        repository.insertWish(deletedWish)
        repository.insertWish(existingWish)

        repository.deleteWishesByIds(listOf(deletedWish.id))
        repository.deleteWishesByIds(listOf(deletedWish.id, existingWish.id))

        assertEquals(0L, repository.getWishesCount(isCompleted = false))
    }
}
