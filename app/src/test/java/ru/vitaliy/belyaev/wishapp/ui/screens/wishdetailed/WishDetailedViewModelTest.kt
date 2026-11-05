package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.createEmptyWish
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class WishDetailedViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun existingWish_usesRouteWishId() = runTest(dispatcher) {
        val wish = createEmptyWish().copy(id = "wish-1", title = "Existing wish")
        val repository = FakeWishesRepository(mapOf(wish.id to wish))
        val savedStateHandle = SavedStateHandle()

        val viewModel = createViewModel(
            wishId = wish.id,
            savedStateHandle = savedStateHandle,
            wishesRepository = repository,
        )

        assertEquals(wish.id, viewModel.currentWishId)
        assertEquals(wish.id, savedStateHandle.get<String>(KEY_CURRENT_WISH_ID))

        advanceUntilIdle()

        assertTrue(repository.insertedIds.isEmpty())
        assertEquals(wish.title, viewModel.uiState.value.get().wish.title)
    }

    @Test
    fun newWish_generatesIdSynchronously_andSavesIt() = runTest(dispatcher) {
        val repository = FakeWishesRepository()
        val savedStateHandle = SavedStateHandle()

        val viewModel = createViewModel(
            wishId = null,
            savedStateHandle = savedStateHandle,
            wishesRepository = repository,
        )

        val generatedId = viewModel.currentWishId
        assertTrue(generatedId.isNotBlank())
        assertEquals(generatedId, savedStateHandle.get<String>(KEY_CURRENT_WISH_ID))

        advanceUntilIdle()

        assertEquals(listOf(generatedId), repository.insertedIds)
    }

    @Test
    fun restoredNewWish_doesNotCreateSecondWish() = runTest(dispatcher) {
        val restoredWish = createEmptyWish().copy(id = "restored-id", title = "Restored draft")
        val repository = FakeWishesRepository(mapOf(restoredWish.id to restoredWish))
        val savedStateHandle = SavedStateHandle(
            mapOf(KEY_CURRENT_WISH_ID to restoredWish.id)
        )

        val viewModel = createViewModel(
            wishId = null,
            savedStateHandle = savedStateHandle,
            wishesRepository = repository,
        )

        assertEquals(restoredWish.id, viewModel.currentWishId)

        advanceUntilIdle()

        assertTrue(repository.insertedIds.isEmpty())
        assertEquals(restoredWish.title, viewModel.uiState.value.get().wish.title)
    }

    @Test
    fun restoredMissingWish_isRecreatedWithSameId() = runTest(dispatcher) {
        val repository = FakeWishesRepository()
        val savedStateHandle = SavedStateHandle(
            mapOf(KEY_CURRENT_WISH_ID to "restored-id")
        )

        val viewModel = createViewModel(
            wishId = null,
            savedStateHandle = savedStateHandle,
            wishesRepository = repository,
        )

        assertEquals("restored-id", viewModel.currentWishId)

        advanceUntilIdle()

        assertEquals(listOf("restored-id"), repository.insertedIds)
    }

    @Test
    fun newWish_insertsPreselectedTag() = runTest(dispatcher) {
        val relationsRepository = FakeWishTagRelationRepository()

        val viewModel = createViewModel(
            wishId = null,
            tagId = "tag-1",
            wishTagRelationRepository = relationsRepository,
        )

        advanceUntilIdle()

        assertEquals(
            listOf(viewModel.currentWishId to "tag-1"),
            relationsRepository.insertedRelations,
        )
    }

    @Test
    fun prepareForExit_waitsForInsertAndReturnsWishId() = runTest(dispatcher) {
        val repository = FakeWishesRepository()

        val viewModel = createViewModel(
            wishId = null,
            wishesRepository = repository,
        )

        val wishId = viewModel.prepareForExit()
        advanceUntilIdle()

        assertEquals(viewModel.currentWishId, wishId)
        assertEquals(listOf(wishId), repository.insertedIds)
    }

    @Test
    fun titleChangedBeforeInsertFinished_isAppliedAfterInsert() = runTest(dispatcher) {
        val insertGate = CompletableDeferred<Unit>()
        val repository = FakeWishesRepository(insertGate = insertGate)

        val viewModel = createViewModel(
            wishId = null,
            wishesRepository = repository,
        )
        viewModel.onWishTitleChanged("New title")
        advanceUntilIdle()

        insertGate.complete(Unit)
        advanceUntilIdle()

        assertEquals("New title", viewModel.uiState.value.get().wish.title)
    }

    @Test
    fun titleWrites_runInOrder() = runTest(dispatcher) {
        val wish = createEmptyWish().copy(id = "wish-1")
        val firstWriteGate = CompletableDeferred<Unit>()
        val repository = FakeWishesRepository(mapOf(wish.id to wish), firstTitleWriteGate = firstWriteGate)
        val viewModel = createViewModel(wishId = wish.id, wishesRepository = repository)
        advanceUntilIdle()

        viewModel.onWishTitleChanged("First")
        viewModel.onWishTitleChanged("Second")
        advanceUntilIdle()
        firstWriteGate.complete(Unit)
        advanceUntilIdle()

        assertEquals("Second", viewModel.uiState.value.get().wish.title)
    }

    @Test
    fun prepareForExit_waitsForPendingWrite() = runTest(dispatcher) {
        val wish = createEmptyWish().copy(id = "wish-1")
        val firstWriteGate = CompletableDeferred<Unit>()
        val repository = FakeWishesRepository(mapOf(wish.id to wish), firstTitleWriteGate = firstWriteGate)
        val viewModel = createViewModel(wishId = wish.id, wishesRepository = repository)
        advanceUntilIdle()

        viewModel.onWishTitleChanged("Typed before exit")
        val exitJob = launch { viewModel.prepareForExit() }
        advanceUntilIdle()
        assertTrue(exitJob.isActive)

        firstWriteGate.complete(Unit)
        advanceUntilIdle()

        assertFalse(exitJob.isActive)
        assertEquals("Typed before exit", repository.getWishById(wish.id).title)
    }

    private fun createViewModel(
        wishId: String?,
        wishLink: String? = null,
        tagId: String? = null,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        wishesRepository: WishesRepository = FakeWishesRepository(),
        wishTagRelationRepository: WishTagRelationRepository = FakeWishTagRelationRepository(),
    ): WishDetailedViewModel {
        return WishDetailedViewModel(
            wishId = wishId,
            wishLink = wishLink,
            tagId = tagId,
            savedStateHandle = savedStateHandle,
            wishesRepository = wishesRepository,
            analyticsRepository = FakeAnalyticsRepository(),
            imagesRepository = FakeImagesRepository(),
            wishTagRelationRepository = wishTagRelationRepository,
        )
    }

    private class FakeWishesRepository(
        initialWishes: Map<String, WishEntity> = emptyMap(),
        private val insertGate: CompletableDeferred<Unit>? = null,
        private val firstTitleWriteGate: CompletableDeferred<Unit>? = null,
    ) : WishesRepository {

        private val wishes: MutableMap<String, WishEntity> = initialWishes.toMutableMap()
        private val wishesFlow = MutableStateFlow(initialWishes)
        val insertedIds: MutableList<String> = mutableListOf()
        private var titleWritesCount = 0

        override suspend fun insertWish(wish: WishEntity) {
            insertGate?.await()
            insertedIds += wish.id
            wishes[wish.id] = wish
            wishesFlow.value = wishes.toMap()
        }

        override fun observeWishById(id: String): Flow<WishEntity> {
            return wishesFlow.mapNotNull { it[id] }.distinctUntilChanged()
        }

        override suspend fun getWishById(id: String): WishEntity {
            return wishes.getValue(id)
        }

        override suspend fun getWishByIdOrNull(id: String): WishEntity? {
            return wishes[id]
        }

        override suspend fun deleteWishesByIds(ids: List<String>) {
            ids.forEach(wishes::remove)
            wishesFlow.value = wishes.toMap()
        }

        override suspend fun updateWishTitle(newValue: String, wishId: String) {
            titleWritesCount++
            if (titleWritesCount == 1) {
                firstTitleWriteGate?.await()
            }
            // Как UPDATE в SQL: для несуществующей строки ничего не делает
            wishes[wishId]?.let { wishes[wishId] = it.copy(title = newValue) }
            wishesFlow.value = wishes.toMap()
        }

        override suspend fun updateWishLink(newValue: String, wishId: String) = notUsed()

        override suspend fun updateWishComment(newValue: String, wishId: String) = notUsed()

        override suspend fun updateWishIsCompleted(newValue: Boolean, wishId: String) = notUsed()

        override suspend fun updatePosition(newValue: Long, wishId: String) = notUsed()

        override suspend fun swapWishesPositions(
            wish1Id: String,
            wish1Position: Long,
            wish2Id: String,
            wish2Position: Long,
        ) = notUsed()

        override suspend fun swapMovedWishPositionWithPassedWishes(
            wishId: String,
            wishPosition: Long,
            passedWishes: List<WishEntity>,
        ) = notUsed()

        override suspend fun updatePositionsOnItemMove(
            startIndex: Int,
            endIndex: Int,
            wishId: String,
            isMoveDown: Boolean,
        ) = notUsed()

        override fun observeAllWishes(isCompleted: Boolean): Flow<List<WishEntity>> = notUsed()

        override fun observeAllWishesSorted(
            isCompleted: Boolean,
            sortMode: WishSortMode,
        ): Flow<List<WishEntity>> = notUsed()

        override suspend fun getAllWishes(isCompleted: Boolean): List<WishEntity> = notUsed()

        override fun observeWishesCount(isCompleted: Boolean): Flow<Long> = notUsed()

        override suspend fun getWishesCount(isCompleted: Boolean): Long = notUsed()

        override fun observeWishesByTag(tagId: String): Flow<List<WishEntity>> = notUsed()

        override fun observeWishesByTagSorted(
            tagId: String,
            sortMode: WishSortMode,
        ): Flow<List<WishEntity>> = notUsed()

        override suspend fun clearAllWishes() = notUsed()

        private fun notUsed(): Nothing = error("Not used in this test")
    }

    private class FakeWishTagRelationRepository : WishTagRelationRepository {

        val insertedRelations: MutableList<Pair<String, String>> = mutableListOf()

        override suspend fun insertWishTagRelation(wishId: String, tagId: String) {
            insertedRelations += wishId to tagId
        }

        override suspend fun deleteWishTagRelation(wishId: String, tagId: String) = Unit
    }

    private class FakeAnalyticsRepository : AnalyticsRepository {
        override fun trackEvent(event: AnalyticsEvent) = Unit
    }

    private class FakeImagesRepository : ImagesRepository {
        override suspend fun insertImage(image: ImageEntity) = notUsed()

        override suspend fun insertImages(images: List<ImageEntity>) = notUsed()

        override suspend fun getImageById(id: String): ImageEntity = notUsed()

        override suspend fun getAllImages(): List<ImageEntity> = notUsed()

        override suspend fun getImagesByWishId(wishId: String): List<ImageEntity> = notUsed()

        override suspend fun getImageRawData(id: String): ByteArray? = notUsed()

        override fun observeImageRefsByWishId(wishId: String): Flow<List<ImageRef>> = notUsed()

        override suspend fun deleteImageById(id: String) = notUsed()

        override suspend fun deleteImagesByIds(ids: List<String>) = notUsed()

        private fun notUsed(): Nothing = error("Not used in this test")
    }

    private companion object {
        const val KEY_CURRENT_WISH_ID = "KEY_CURRENT_WISH_ID"
    }
}
