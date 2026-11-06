package ru.vitaliy.belyaev.wishapp.shared.data.repository

import app.cash.sqldelight.db.SqlDriver
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.DispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.database.createTestDatabaseDriver
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish

class DatabaseRepositoryRulesTest {

    private val driver: SqlDriver = createTestDatabaseDriver()
    private val repository = DatabaseRepository(WishAppDb(driver), TestDispatcherProvider)

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun insertTag_withSameTitleIgnoringCaseAndSpaces_returnsExistingTag() = runTest {
        val homeId = repository.insertTag("Дом")

        assertEquals(homeId, repository.insertTag(" дом "))
        assertEquals(listOf("Дом"), repository.getAllTags().map { it.title })
    }

    @Test
    fun insertTag_takesLeastUsedColor() = runTest {
        val firstId = repository.insertTag("Первый")
        val secondId = repository.insertTag("Второй")
        repository.updateTagColor(TagColor.BUTTER, secondId)
        val thirdId = repository.insertTag("Третий")

        assertEquals(TagColor.BUTTER, repository.getTagById(firstId).color)
        assertEquals(TagColor.BUTTER, repository.getTagById(secondId).color)
        assertEquals(TagColor.MINT, repository.getTagById(thirdId).color)
    }

    @Test
    fun updateTagTitle_toTitleOfOtherTag_isRejected() = runTest {
        repository.insertTag("Дом")
        val gardenId = repository.insertTag("Сад")

        assertFalse(repository.updateTagTitle("ДОМ", gardenId))
        assertEquals("Сад", repository.getTagById(gardenId).title)
    }

    @Test
    fun updateTagTitle_changingCaseOfSameTag_isAllowed() = runTest {
        val gardenId = repository.insertTag("сад")

        assertTrue(repository.updateTagTitle("Сад", gardenId))
        assertEquals("Сад", repository.getTagById(gardenId).title)
    }

    @Test
    fun completion_setsDate_andWantAgainClearsIt() = runTest {
        val wish = insertWish()
        assertNull(repository.getWishById(wish.id).completedTimestamp)

        repository.updateWishIsCompleted(true, wish.id)
        assertNotNull(repository.getWishById(wish.id).completedTimestamp)

        repository.updateWishIsCompleted(false, wish.id)
        assertNull(repository.getWishById(wish.id).completedTimestamp)
    }

    @Test
    fun editingCompletedWish_keepsCompletionDate() = runTest {
        val wish = insertWish()
        repository.updateWishIsCompleted(true, wish.id)
        val completedAt = repository.getWishById(wish.id).completedTimestamp

        repository.updateWishTitle("Новое название", wish.id)

        assertEquals(completedAt, repository.getWishById(wish.id).completedTimestamp)
    }

    @Test
    fun insertImages_appendsToEndOfWish_mainPhotoStaysFirst() = runTest {
        val wish = insertWish()
        repository.insertImages(listOf(image("a", wish.id), image("b", wish.id)))
        repository.insertImage(image("c", wish.id))
        repository.deleteImageById("a")
        repository.insertImage(image("d", wish.id))

        assertEquals(listOf("b", "c", "d"), repository.getWishById(wish.id).images.map { it.id })
    }

    @Test
    fun shownTag_byDefaultFirstAdded_thenChosen_thenNextAfterRemoval() = runTest {
        val wish = insertWish()
        val kitchenId = repository.insertTag("Кухня")
        val homeId = repository.insertTag("Дом")
        repository.insertWishTagRelation(wish.id, kitchenId)
        repository.insertWishTagRelation(wish.id, homeId)
        assertEquals(kitchenId, repository.getWishById(wish.id).shownTagId)

        repository.setShownTag(wish.id, homeId)
        assertEquals(homeId, repository.getWishById(wish.id).shownTagId)

        repository.deleteWishTagRelation(wish.id, homeId)
        assertEquals(kitchenId, repository.getWishById(wish.id).shownTagId)
    }

    @Test
    fun insertWishTagRelation_twice_keepsOneRelation() = runTest {
        val wish = insertWish()
        val homeId = repository.insertTag("Дом")

        repository.insertWishTagRelation(wish.id, homeId)
        repository.insertWishTagRelation(wish.id, repository.insertTag("дом"))

        assertEquals(listOf(homeId), repository.getWishById(wish.id).tags.map { it.id })
    }

    private suspend fun insertWish(): WishEntity = createEmptyWish().also { repository.insertWish(it) }

    private fun image(id: String, wishId: String) = ImageEntity(id, wishId, byteArrayOf(1))

    private object TestDispatcherProvider : DispatcherProvider {
        override fun main(): CoroutineDispatcher = Dispatchers.Unconfined
        override fun default(): CoroutineDispatcher = Dispatchers.Unconfined
        override fun io(): CoroutineDispatcher = Dispatchers.Unconfined
        override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
    }
}
