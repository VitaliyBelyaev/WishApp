package ru.vitaliy.belyaev.wishapp.shared.data.repository

import com.benasher44.uuid.uuid4
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import ru.vitaliy.belyaev.wishapp.shared.data.coroutines.DispatcherProvider
import ru.vitaliy.belyaev.wishapp.shared.data.database.Tag
import ru.vitaliy.belyaev.wishapp.shared.data.database.TagQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.Wish
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishQueries
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishTagRelation
import ru.vitaliy.belyaev.wishapp.shared.data.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.shared.data.mapper.TagMapper
import ru.vitaliy.belyaev.wishapp.shared.data.mapper.WishMapper
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.TagsRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.WishesRepository
import ru.vitaliy.belyaev.wishapp.shared.utils.nowEpochMillis

class DatabaseRepository(
    database: WishAppDb,
    private val dispatcherProvider: DispatcherProvider
) : WishesRepository, WishTagRelationRepository, TagsRepository {

    private val wishQueries: WishQueries = database.wishQueries
    private val wishTagRelationQueries: WishTagRelationQueries = database.wishTagRelationQueries
    private val tagQueries: TagQueries = database.tagQueries

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
                        position
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
                wishId = wishId
            )
        }
    }

    @NativeCoroutines
    override suspend fun updateWishLink(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateLink(
                link = newValue,
                updatedTimestamp = Clock.System.nowEpochMillis(),
                wishId = wishId
            )
        }
    }

    @NativeCoroutines
    override suspend fun updateWishComment(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateComment(
                comment = newValue,
                updatedTimestamp = Clock.System.nowEpochMillis(),
                wishId = wishId
            )
        }
    }

    @NativeCoroutines
    override suspend fun updateWishIsCompleted(newValue: Boolean, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                wishQueries.updateIsCompleted(
                    isCompleted = newValue,
                    updatedTimestamp = Clock.System.nowEpochMillis(),
                    wishId = wishId
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
                wishId = wishId
            )
        }
    }

    @NativeCoroutines
    override suspend fun swapWishesPositions(
        wish1Id: String,
        wish1Position: Long,
        wish2Id: String,
        wish2Position: Long
    ) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                wishQueries.updatePosition(
                    position = wish2Position,
                    wishId = wish1Id
                )
                wishQueries.updatePosition(
                    position = wish1Position,
                    wishId = wish2Id
                )
            }
        }
    }

    @NativeCoroutines
    override suspend fun swapMovedWishPositionWithPassedWishes(
        wishId: String,
        wishPosition: Long,
        passedWishes: List<WishEntity>
    ) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                passedWishes.forEachIndexed { index, passedWish ->
                    val prevPassedWishPosition = passedWishes.getOrNull(index - 1)?.position ?: wishPosition
                    wishQueries.updatePosition(
                        position = passedWish.position,
                        wishId = wishId
                    )
                    wishQueries.updatePosition(
                        position = prevPassedWishPosition,
                        wishId = passedWish.id
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
        isMoveDown: Boolean
    ) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                if (isMoveDown) {
                    wishQueries.updatePositionsOnItemMoveDown(
                        position = startIndex.toLong(),
                        position_ = endIndex.toLong()
                    )
                } else {
                    wishQueries.updatePositionsOnItemMoveUp(
                        position = startIndex.toLong(),
                        position_ = endIndex.toLong()
                    )
                }
                wishQueries.updatePosition(
                    position = endIndex.toLong(),
                    wishId = wishId
                )
            }
        }
    }

    @NativeCoroutines
    override fun observeWishById(id: String): Flow<WishEntity> {
        val wishDtoFlow: Flow<Wish> = wishQueries
            .getById(id)
            .asFlow()
            .mapToOne(dispatcherProvider.io())

        val tagsFlow: Flow<List<Tag>> = wishTagRelationQueries
            .getWishTags(id)
            .asFlow()
            .mapToList(dispatcherProvider.io())
        return wishDtoFlow.combine(tagsFlow) { wishDto, tags ->
            WishMapper.mapToDomain(wishDto, tags)
        }
    }

    @NativeCoroutines
    override suspend fun getWishById(id: String): WishEntity {
        return withContext(dispatcherProvider.io()) {
            val wishDto: Wish = wishQueries
                .getById(id)
                .executeAsOne()
            val tags: List<Tag> = wishTagRelationQueries
                .getWishTags(id)
                .executeAsList()

            WishMapper.mapToDomain(wishDto, tags)
        }
    }

    @NativeCoroutines
    override fun observeAllWishes(isCompleted: Boolean): Flow<List<WishEntity>> {
        val wishesFlow: Flow<List<Wish>> = wishQueries
            .getAll(isCompleted)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags count in wish
        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags content in wish
        val tagsFlow: Flow<List<TagEntity>> = observeAllTags()

        return combine(wishesFlow, wishTagRelationsFlow, tagsFlow) { wishes, _, _ ->
            wishes.map {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(it.wishId)
                    .executeAsList()
                WishMapper.mapToDomain(it, tags)
            }
        }
    }

    @NativeCoroutines
    override suspend fun getAllWishes(isCompleted: Boolean): List<WishEntity> {
        return withContext(dispatcherProvider.io()) {
            wishQueries
                .getAll(isCompleted)
                .executeAsList()
                .map {
                    val tags: List<Tag> = wishTagRelationQueries
                        .getWishTags(it.wishId)
                        .executeAsList()
                    WishMapper.mapToDomain(it, tags)
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

        val wishesByTagFlow: Flow<List<Wish>> = wishTagRelationQueries
            .getAllWishesByTag(tagId)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tag content
        val tagFlow: Flow<Tag> = tagQueries.getById(tagId).asFlow().mapToOne(dispatcherProvider.io())

        return combine(wishesByTagFlow, tagFlow) { wishes, _ ->
            wishes.map {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(it.wishId)
                    .executeAsList()
                WishMapper.mapToDomain(it, tags)
            }
        }
    }

    @NativeCoroutines
    override suspend fun deleteWishesByIds(ids: List<String>) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                for (id in ids) {
                    val wishToDelete = wishQueries.getById(id).executeAsOne()
                    if (wishToDelete.position != -1L) {
                        wishQueries.updatePositionsOnDelete(wishToDelete.position)
                    }
                    wishQueries.deleteById(id)
                }
                wishTagRelationQueries.deleteByWishIds(ids)
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

    // end region WishTagRelationRepository

    // region TagsRepository

    @NativeCoroutines
    override suspend fun insertTag(title: String): String {
        val tagId = uuid4().toString()
        tagQueries.insert(tagId, title)
        return tagId
    }

    @NativeCoroutines
    override suspend fun updateTagTitle(title: String, tagId: String) {
        tagQueries.updateTitle(title, tagId)
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
                .getWishTags(wishId, TagMapper::mapToDomain)
                .executeAsList()
        }
    }

    @NativeCoroutines
    override fun observeTagsByWishId(wishId: String): Flow<List<TagEntity>> {
        return wishTagRelationQueries
            .getWishTags(wishId, TagMapper::mapToDomain)
            .asFlow()
            .mapToList(dispatcherProvider.io())
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
}