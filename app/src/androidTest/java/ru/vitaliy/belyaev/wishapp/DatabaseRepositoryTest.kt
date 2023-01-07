package ru.vitaliy.belyaev.wishapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.squareup.sqldelight.android.AndroidSqliteDriver
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.data.repository.DatabaseRepository
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DatabaseRepositoryTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var db: WishAppDb
    private lateinit var driver: AndroidSqliteDriver
    private lateinit var databaseRepository: DatabaseRepository

    private val testWishes = createTestWishes()
    private val testTags = createTestTags()

    @Before
    fun initDb() {
        val context = InstrumentationRegistry.getInstrumentation().context
        driver = AndroidSqliteDriver(
            schema = WishAppDb.Schema,
            context = context,
            name = null
        )
        db = WishAppDb(driver)
        databaseRepository = DatabaseRepository(db, coroutinesTestRule.testDispatcherProvider)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        driver.close()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun insert_one_wish() = runTest {
        val wish = WishWithTags(
            id = "1",
            title = "Шуруповерт",
            link = "link",
            comment = "Не душманский, качественный, чтобы кейс был",
            isCompleted = false,
            createdTimestamp = 0L,
            updatedTimestamp = 0L,
            tags = emptyList()
        )

        databaseRepository.insertWish(wish)

        val result: WishWithTags? = databaseRepository.getAllWishes().firstOrNull()
        Assert.assertEquals(wish, result)

        clearWishes()
    }

    @Test
    fun observe_wishes_change() = runTest {
        populateDBWithWishes()
        populateDBWithTags()
        db.wishTagRelationQueries.insert("1", "1")
        db.wishTagRelationQueries.insert("1", "2")
        db.wishTagRelationQueries.insert("3", "1")
        val expected1 = testWishes.map { wish ->
            when (wish.id) {
                "1" -> {
                    wish.copy(tags = testTags.filter { it.tagId == "1" || it.tagId == "2" })
                }
                "3" -> {
                    wish.copy(tags = testTags.filter { it.tagId == "1" })
                }
                else -> wish
            }

        }
        val expected2 = testWishes.map { wish ->
            when (wish.id) {
                "1" -> {
                    wish.copy(tags = testTags.filter { it.tagId == "1" || it.tagId == "2" })
                }
                "3" -> {
                    wish.copy(tags = testTags.filter { it.tagId == "1" })
                }
                "2" -> {
                    wish.copy(tags = testTags.filter { it.tagId == "3" })
                }
                else -> wish
            }

        }

        val wishesFlow = databaseRepository.observeAllWishes()
        wishesFlow.take(1).collect {
            Assert.assertEquals(expected1, it)
        }

        db.wishTagRelationQueries.insert("2", "3")

        wishesFlow.drop(1).collect {
            Assert.assertEquals(expected2, it)
        }
        clearWishes()
    }

    private suspend fun populateDBWithWishes() {
        for (wish in testWishes) {
            databaseRepository.insertWish(wish)
        }
    }

    private fun populateDBWithTags() {
        for (tag in testTags) {
            db.tagQueries.insert(tag.tagId, tag.title)
        }
    }

    private fun clearWishes() {
        db.wishQueries.clear()
    }

    private fun createTestTags(): List<Tag> {
        return listOf(
            Tag("1", "ДР"),
            Tag("2", "Эмоции"),
            Tag("3", "Крупные")
        )
    }

    private fun createTestWishes(): List<WishWithTags> {
        val currentMillis = System.currentTimeMillis()
        return listOf(
            WishWithTags(
                id = "1",
                title = "Шуруповерт",
                link = "https://www.citilink.ru/product/drel-shurupovert-bosch-universaldrill-18v-akkum-patron-bystrozazhimnoi-1492081/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3Aom3wUbhHlBUu-9OPINzsyF9rM0Q2rBUgp1jFV68iT7IUaAoTA-1xoCzPcQAvD_BwE",
                comment = "Не душманский, качественный, чтобы кейс был, головки разные и запасной аккумулятор и фонарик включался когда начинаешь крутить",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            WishWithTags(
                id = "2",
                title = "Айфон",
                link = "link",
                comment = "",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
            WishWithTags(
                id = "3",
                title = "Робот",
                link = "",
                comment = "",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                tags = emptyList()
            ),
        )
    }
}