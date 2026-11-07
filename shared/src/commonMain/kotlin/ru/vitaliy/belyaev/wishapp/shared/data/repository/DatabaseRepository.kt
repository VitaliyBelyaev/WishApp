package ru.vitaliy.belyaev.wishapp.shared.data.repository

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.DispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.GetWishTags
import ru.vitaliy.belyaev.wishapp.shared.data.database.ImageQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.Tag
import ru.vitaliy.belyaev.wishapp.shared.data.database.TagQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.Wish
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishTagRelation
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.shared.data.mapper.ImageMapper
import ru.vitaliy.belyaev.wishapp.shared.data.mapper.TagMapper
import ru.vitaliy.belyaev.wishapp.shared.data.mapper.WishMapper
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.normalizeTagTitle
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.shared.utils.nowEpochMillis

@OptIn(ExperimentalTime::class)
class DatabaseRepository(
    database: WishAppDb,
    private val dispatcherProvider: DispatcherProvider,
) : WishesRepository, WishTagRelationRepository, TagsRepository, ImagesRepository {

    private val wishQueries: WishQueries = database.wishQueries
    private val wishTagRelationQueries: WishTagRelationQueries = database.wishTagRelationQueries
    private val tagQueries: TagQueries = database.tagQueries
    private val imageQueries: ImageQueries = database.imageQueries

    // region WishesRepository
    @NativeCoroutines
    override suspend fun insertWish(wish: WishEntity) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                val position = wishQueries.getWishesCountWithValidPosition().executeAsOne()
                with(wish) {
                    wishQueries.insert(
                        id,
                        title,
                        link,
                        comment,
                        isCompleted,
                        createdTimestamp,
                        updatedTimestamp,
                        position,
                        completedTimestamp,
                    )
                }
            }
        }
    }

    @NativeCoroutines
    override suspend fun updateWishTitle(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateTitle(
                title = newValue,
                updatedTimestamp = Clock.System.nowEpochMillis(),
                wishId = wishId,
            )
        }
    }

    @NativeCoroutines
    override suspend fun updateWishLink(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateLink(
                link = newValue,
                updatedTimestamp = Clock.System.nowEpochMillis(),
                wishId = wishId,
            )
        }
    }

    @NativeCoroutines
    override suspend fun updateWishComment(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateComment(
                comment = newValue,
                updatedTimestamp = Clock.System.nowEpochMillis(),
                wishId = wishId,
            )
        }
    }

    @NativeCoroutines
    override suspend fun updateWishIsCompleted(newValue: Boolean, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                val now = Clock.System.nowEpochMillis()
                // «Снова хочу» убирает дату: она бы осталась от прошлого исполнения
                wishQueries.updateIsCompleted(
                    isCompleted = newValue,
                    updatedTimestamp = now,
                    completedTimestamp = if (newValue) now else null,
                    wishId = wishId,
                )

                if (newValue) {
                    val wish = wishQueries.getById(wishId).executeAsOne()
                    wishQueries.updatePositionsOnDelete(wish.position)
                    wishQueries.updatePosition(-1L, wishId)
                } else {
                    val position = wishQueries.getWishesCountWithValidPosition().executeAsOne()
                    wishQueries.updatePosition(position, wishId)
                }
            }
        }
    }

    @NativeCoroutines
    override suspend fun updatePosition(newValue: Long, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updatePosition(
                position = newValue,
                wishId = wishId,
            )
        }
    }

    @NativeCoroutines
    override suspend fun swapWishesPositions(
        wish1Id: String,
        wish1Position: Long,
        wish2Id: String,
        wish2Position: Long,
    ) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                wishQueries.updatePosition(
                    position = wish2Position,
                    wishId = wish1Id,
                )
                wishQueries.updatePosition(
                    position = wish1Position,
                    wishId = wish2Id,
                )
            }
        }
    }

    @NativeCoroutines
    override suspend fun swapMovedWishPositionWithPassedWishes(
        wishId: String,
        wishPosition: Long,
        passedWishes: List<WishEntity>,
    ) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                passedWishes.forEachIndexed { index, passedWish ->
                    val prevPassedWishPosition = passedWishes.getOrNull(index - 1)?.position ?: wishPosition
                    wishQueries.updatePosition(
                        position = passedWish.position,
                        wishId = wishId,
                    )
                    wishQueries.updatePosition(
                        position = prevPassedWishPosition,
                        wishId = passedWish.id,
                    )
                }
            }
        }
    }

    @NativeCoroutines
    override suspend fun updatePositionsOnItemMove(
        startIndex: Int,
        endIndex: Int,
        wishId: String,
        isMoveDown: Boolean,
    ) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                if (isMoveDown) {
                    wishQueries.updatePositionsOnItemMoveDown(
                        position = startIndex.toLong(),
                        position_ = endIndex.toLong(),
                    )
                } else {
                    wishQueries.updatePositionsOnItemMoveUp(
                        position = startIndex.toLong(),
                        position_ = endIndex.toLong(),
                    )
                }
                wishQueries.updatePosition(
                    position = endIndex.toLong(),
                    wishId = wishId,
                )
            }
        }
    }

    @NativeCoroutines
    override fun observeWishById(id: String): Flow<WishEntity> {
        // Желание могут удалить, пока на него ещё подписан закрывающийся экран
        val wishDtoFlow: Flow<Wish> = wishQueries
            .getById(id)
            .asFlow()
            .mapToOneOrNull(dispatcherProvider.io())
            .filterNotNull()

        val tagsFlow: Flow<List<GetWishTags>> = wishTagRelationQueries
            .getWishTags(id)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        val imagesFlow: Flow<List<ImageRef>> = imageQueries
            .getRefsByWishId(id, ImageMapper::mapToRef)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        return combine(wishDtoFlow, tagsFlow, imagesFlow) { wishDto, tags, images ->
            WishMapper.mapToDomain(wishDto, tags, images)
        }
    }

    @NativeCoroutines
    override suspend fun getWishById(id: String): WishEntity {
        return getWishByIdOrNull(id) ?: error("Wish not found: $id")
    }

    @NativeCoroutines
    override suspend fun getWishByIdOrNull(id: String): WishEntity? {
        return withContext(dispatcherProvider.io()) {
            val wishDto: Wish = wishQueries
                .getById(id)
                .executeAsOneOrNull()
                ?: return@withContext null

            val tags: List<GetWishTags> = wishTagRelationQueries
                .getWishTags(id)
                .executeAsList()

            val images: List<ImageRef> = imageQueries
                .getRefsByWishId(id, ImageMapper::mapToRef)
                .executeAsList()

            WishMapper.mapToDomain(wishDto, tags, images)
        }
    }

    @NativeCoroutines
    override fun observeAllWishes(isCompleted: Boolean): Flow<List<WishEntity>> {
        return observeAllWishesSorted(isCompleted, WishSortMode.Default)
    }

    @NativeCoroutines
    override fun observeAllWishesSorted(
        isCompleted: Boolean,
        sortMode: WishSortMode,
    ): Flow<List<WishEntity>> {
        val wishesQuery: Query<Wish> = when (sortMode) {
            WishSortMode.Default,
            WishSortMode.TitleAZ,
            WishSortMode.TitleZA,
            -> wishQueries.getAll(isCompleted)
            WishSortMode.CreatedDateNewest -> wishQueries.getAllByCreatedTimestampDesc(isCompleted)
            WishSortMode.CreatedDateOldest -> wishQueries.getAllByCreatedTimestampAsc(isCompleted)
        }
        return observeWishesWithTagsAndImages(wishesQuery, sortMode)
    }

    @NativeCoroutines
    override fun observeCompletedWishesByCompletionDate(newestFirst: Boolean): Flow<List<WishEntity>> {
        val wishesQuery: Query<Wish> = if (newestFirst) {
            wishQueries.getAllCompletedByCompletionDesc()
        } else {
            wishQueries.getAllCompletedByCompletionAsc()
        }
        return observeWishesWithTagsAndImages(wishesQuery, WishSortMode.Default)
    }

    private fun observeWishesWithTagsAndImages(
        wishesQuery: Query<Wish>,
        sortMode: WishSortMode,
    ): Flow<List<WishEntity>> {
        val wishesFlow: Flow<List<Wish>> = wishesQuery
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags count in wish
        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags content in wish
        val tagsFlow: Flow<List<TagEntity>> = observeAllTags()

        // We need this for reactive changes of images content in wish
        val imagesFlow: Flow<List<ImageRef>> = imageQueries
            .getAllRefs(ImageMapper::mapToRef)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        return combine(wishesFlow, wishTagRelationsFlow, tagsFlow, imagesFlow) { wishes, _, _, allImages ->
            val imagesByWishId = allImages.groupBy { it.wishId }
            val wishEntities = wishes.map {
                val tags: List<GetWishTags> = wishTagRelationQueries
                    .getWishTags(it.wishId)
                    .executeAsList()

                WishMapper.mapToDomain(it, tags, imagesByWishId[it.wishId].orEmpty())
            }
            sortWishesByTitle(wishEntities, sortMode)
            // Запросы тегов выше синхронные, а combine работает в контексте подписчика
        }.flowOn(dispatcherProvider.io())
    }

    @NativeCoroutines
    override suspend fun getAllWishes(isCompleted: Boolean): List<WishEntity> {
        return withContext(dispatcherProvider.io()) {
            val imagesByWishId = imageQueries
                .getAllRefs(ImageMapper::mapToRef)
                .executeAsList()
                .groupBy { it.wishId }

            wishQueries
                .getAll(isCompleted)
                .executeAsList()
                .map {
                    val tags: List<GetWishTags> = wishTagRelationQueries
                        .getWishTags(it.wishId)
                        .executeAsList()

                    WishMapper.mapToDomain(it, tags, imagesByWishId[it.wishId].orEmpty())
                }
        }
    }

    @NativeCoroutines
    override fun observeWishesCount(isCompleted: Boolean): Flow<Long> {
        return wishQueries
            .getWishesCount(isCompleted)
            .asFlow()
            .mapToOne(dispatcherProvider.io())
    }

    @NativeCoroutines
    override suspend fun getWishesCount(isCompleted: Boolean): Long {
        return wishQueries
            .getWishesCount(isCompleted)
            .executeAsOne()
    }

    @NativeCoroutines
    override fun observeWishesByTag(tagId: String): Flow<List<WishEntity>> {
        return observeWishesByTagSorted(tagId, WishSortMode.Default)
    }

    @NativeCoroutines
    override fun observeWishesByTagSorted(
        tagId: String,
        sortMode: WishSortMode,
    ): Flow<List<WishEntity>> {
        val wishesByTagQuery: Query<Wish> = when (sortMode) {
            WishSortMode.Default,
            WishSortMode.TitleAZ,
            WishSortMode.TitleZA,
            -> wishTagRelationQueries.getAllWishesByTag(tagId)
            WishSortMode.CreatedDateNewest -> wishTagRelationQueries.getAllWishesByTagCreatedDesc(tagId)
            WishSortMode.CreatedDateOldest -> wishTagRelationQueries.getAllWishesByTagCreatedAsc(tagId)
        }

        val wishesByTagFlow: Flow<List<Wish>> = wishesByTagQuery
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tag content
        val tagFlow: Flow<Tag> = tagQueries.getById(tagId).asFlow().mapToOne(dispatcherProvider.io())

        // We need this for reactive changes of images content in wish
        val imagesFlow: Flow<List<ImageRef>> = imageQueries
            .getAllRefs(ImageMapper::mapToRef)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        return combine(wishesByTagFlow, tagFlow, imagesFlow) { wishes, _, allImages ->
            val imagesByWishId = allImages.groupBy { it.wishId }
            val wishEntities = wishes.map {
                val tags: List<GetWishTags> = wishTagRelationQueries
                    .getWishTags(it.wishId)
                    .executeAsList()

                WishMapper.mapToDomain(it, tags, imagesByWishId[it.wishId].orEmpty())
            }
            sortWishesByTitle(wishEntities, sortMode)
            // Запросы тегов выше синхронные, а combine работает в контексте подписчика
        }.flowOn(dispatcherProvider.io())
    }

    // SQLite COLLATE NOCASE сравнивает без регистра только латиницу. Для
    // кириллицы сравнение идёт по коду символа, поэтому заглавные буквы
    // оказываются раньше строчных: "Яблоко" встаёт перед "арбуз". Поэтому порядок
    // по названию задаём только здесь, по lowercase, без сортировки в SQL.
    // Это не полноценная locale-aware сортировка, но убирает основной
    // практический баг.
    private fun sortWishesByTitle(
        wishes: List<WishEntity>,
        sortMode: WishSortMode,
    ): List<WishEntity> {
        val comparator = compareBy<WishEntity> { it.title.lowercase() }
            .thenBy { it.title }

        return when (sortMode) {
            WishSortMode.TitleAZ -> wishes.sortedWith(comparator)
            WishSortMode.TitleZA -> wishes.sortedWith(comparator.reversed())
            else -> wishes
        }
    }

    @NativeCoroutines
    override suspend fun deleteWishesByIds(ids: List<String>) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                for (id in ids) {
                    // Желание могли удалить параллельно: выход с экрана и очистка пустых желаний
                    val wishToDelete = wishQueries.getById(id).executeAsOneOrNull() ?: continue
                    if (wishToDelete.position != -1L) {
                        wishQueries.updatePositionsOnDelete(wishToDelete.position)
                    }
                    wishQueries.deleteById(id)
                }
                wishTagRelationQueries.deleteByWishIds(ids)
                imageQueries.deleteByWishIds(ids)
            }
        }
    }

    @NativeCoroutines
    override suspend fun clearAllWishes() {
        wishQueries.clear()
    }
    // end region WishesRepository

    // region WishTagRelationRepository

    @NativeCoroutines
    override suspend fun insertWishTagRelation(wishId: String, tagId: String) {
        wishTagRelationQueries.insert(wishId, tagId)
    }

    @NativeCoroutines
    override suspend fun deleteWishTagRelation(wishId: String, tagId: String) {
        wishTagRelationQueries.delete(wishId, tagId)
    }

    @NativeCoroutines
    override suspend fun setShownTag(wishId: String, tagId: String) {
        withContext(dispatcherProvider.io()) {
            wishTagRelationQueries.transaction {
                wishTagRelationQueries.clearShownForWish(wishId)
                wishTagRelationQueries.markShown(wishId, tagId)
            }
        }
    }

    // end region WishTagRelationRepository

    // region TagsRepository

    @NativeCoroutines
    override suspend fun insertTag(title: String): String {
        return withContext(dispatcherProvider.io()) {
            tagQueries.transactionWithResult {
                findTagIdWithSameTitle(title)?.let { return@transactionWithResult it }

                val tagId = uuid4().toString()
                val countsByColor = tagQueries.countByColor().executeAsList()
                    .associate { it.color to it.tagsCount }
                tagQueries.insert(tagId, title, TagColor.leastUsed(countsByColor).storedValue)
                tagId
            }
        }
    }

    @NativeCoroutines
    override suspend fun updateTagTitle(title: String, tagId: String): Boolean {
        return withContext(dispatcherProvider.io()) {
            tagQueries.transactionWithResult {
                val sameTitleTagId = findTagIdWithSameTitle(title, exceptTagId = tagId)
                if (sameTitleTagId != null) {
                    return@transactionWithResult false
                }
                tagQueries.updateTitle(title, tagId)
                true
            }
        }
    }

    @NativeCoroutines
    override suspend fun updateTagColor(color: TagColor, tagId: String) {
        withContext(dispatcherProvider.io()) {
            tagQueries.updateColor(color.storedValue, tagId)
        }
    }

    // SQLite lower() не знает кириллицу, поэтому названия сравниваем в Kotlin
    private fun findTagIdWithSameTitle(title: String, exceptTagId: String? = null): String? {
        val key = normalizeTagTitle(title)
        return tagQueries.getAll().executeAsList()
            .firstOrNull { it.tagId != exceptTagId && normalizeTagTitle(it.title) == key }
            ?.tagId
    }

    @NativeCoroutines
    override suspend fun getTagById(id: String): TagEntity {
        return withContext(dispatcherProvider.io()) {
            tagQueries
                .getById(id, TagMapper::mapToDomain)
                .executeAsOne()
        }
    }

    @NativeCoroutines
    override suspend fun getAllTags(): List<TagEntity> {
        return withContext(dispatcherProvider.io()) {
            tagQueries
                .getAll(TagMapper::mapToDomain)
                .executeAsList()
        }
    }

    @NativeCoroutines
    override fun observeAllTags(): Flow<List<TagEntity>> {
        return tagQueries
            .getAll(TagMapper::mapToDomain)
            .asFlow()
            .mapToList(dispatcherProvider.io())
    }

    @NativeCoroutines
    override suspend fun getTagsByWishId(wishId: String): List<TagEntity> {
        return withContext(dispatcherProvider.io()) {
            wishTagRelationQueries
                .getWishTags(wishId)
                .executeAsList()
                .map(TagMapper::mapToDomain)
        }
    }

    @NativeCoroutines
    override fun observeTagsByWishId(wishId: String): Flow<List<TagEntity>> {
        return wishTagRelationQueries
            .getWishTags(wishId)
            .asFlow()
            .mapToList(dispatcherProvider.io())
            .map { tags -> tags.map(TagMapper::mapToDomain) }
    }

    @NativeCoroutines
    override fun observeAllTagsWithWishesCount(): Flow<List<TagWithWishCount>> {

        val tagsFlow: Flow<List<TagEntity>> = observeAllTags()

        val relationsWithCurrentWishesFlow = wishTagRelationQueries
            .getRelationsWithCurrentWishes()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        return combine(tagsFlow, relationsWithCurrentWishesFlow) { tags, relationsCountWithCurrentWishes ->
            val tagsWithWishCount = mutableListOf<TagWithWishCount>()
            for (tag in tags) {
                val wishesCount = relationsCountWithCurrentWishes.count { it.tagId == tag.id }
                tagsWithWishCount.add(TagWithWishCount(tag, wishesCount.toLong()))
            }
            tagsWithWishCount.toList()
        }
    }

    @NativeCoroutines
    override suspend fun deleteTagsByIds(ids: List<String>) {
        tagQueries.transaction {
            tagQueries.deleteByIds(ids)
            wishTagRelationQueries.deleteByTagIds(ids)
        }
    }

    @NativeCoroutines
    override suspend fun clearAllTags() {
        tagQueries.clear()
    }

    // end region TagsRepository

    // region ImagesRepository

    @NativeCoroutines
    override suspend fun insertImage(image: ImageEntity) {
        insertImages(listOf(image))
    }

    /** Новые фото встают в конец своей хотелки, главное фото не меняется. */
    @NativeCoroutines
    override suspend fun insertImages(images: List<ImageEntity>) {
        imageQueries.transaction {
            for (image in images) {
                val position = imageQueries.nextPositionInWish(image.wishId).executeAsOne()
                imageQueries.insert(
                    image.id,
                    image.wishId,
                    image.rawData,
                    position,
                )
            }
        }
    }

    @NativeCoroutines
    override suspend fun getImageRawData(id: String): ByteArray? {
        return withContext(dispatcherProvider.io()) {
            imageQueries.getRawDataById(id).executeAsOneOrNull()
        }
    }

    @NativeCoroutines
    override fun observeImageRefsByWishId(wishId: String): Flow<List<ImageRef>> {
        return imageQueries.getRefsByWishId(wishId, ImageMapper::mapToRef)
            .asFlow()
            .mapToList(dispatcherProvider.io())
    }

    @NativeCoroutines
    override suspend fun deleteImageById(id: String) {
        imageQueries.deleteById(id)
    }

    @NativeCoroutines
    override suspend fun deleteImagesByIds(ids: List<String>) {
        imageQueries.deleteByIds(ids)
    }

    // end region ImagesRepository
}
