package ru.vitaliy.belyaev.wishapp.data.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.data.database.TagQueries
import ru.vitaliy.belyaev.wishapp.data.database.Wish
import ru.vitaliy.belyaev.wishapp.data.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.data.database.WishQueries
import ru.vitaliy.belyaev.wishapp.data.database.WishTagRelation
import ru.vitaliy.belyaev.wishapp.data.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.data.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.data.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.data.repository.wishes.toWishWithTags
import ru.vitaliy.belyaev.wishapp.data.repository.wishtagrelation.WishTagRelationRepository
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider

@Singleton
class DatabaseRepository @Inject constructor(
    database: WishAppDb,
    private val dispatcherProvider: DispatcherProvider
) : WishesRepository, WishTagRelationRepository, TagsRepository {

    private val wishQueries: WishQueries = database.wishQueries
    private val wishTagRelationQueries: WishTagRelationQueries = database.wishTagRelationQueries
    private val tagQueries: TagQueries = database.tagQueries

    // region WishesRepository
    override suspend fun insertWish(wish: Wish) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                val position = wishQueries.getWishesCountWithValidPosition().executeAsOne()
                with(wish) {
                    wishQueries.insert(
                        wishId,
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

    override suspend fun updateWishTitle(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateTitle(title = newValue, updatedTimestamp = System.currentTimeMillis(), wishId = wishId)
        }
    }

    override suspend fun updateWishLink(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateLink(link = newValue, updatedTimestamp = System.currentTimeMillis(), wishId = wishId)
        }
    }

    override suspend fun updateWishComment(newValue: String, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updateComment(
                comment = newValue,
                updatedTimestamp = System.currentTimeMillis(),
                wishId = wishId
            )
        }
    }

    override suspend fun updateWishIsCompleted(newValue: Boolean, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.transaction {
                wishQueries.updateIsCompleted(
                    isCompleted = newValue,
                    updatedTimestamp = System.currentTimeMillis(),
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

    override suspend fun updatePosition(newValue: Long, wishId: String) {
        withContext(dispatcherProvider.io()) {
            wishQueries.updatePosition(
                position = newValue,
                wishId = wishId
            )
        }
    }

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

    override fun observeWishById(id: String): Flow<WishWithTags> {
        val wishDtoFlow: Flow<Wish> = wishQueries
            .getById(id)
            .asFlow()
            .mapToOne(dispatcherProvider.io())
        val tagsFlow: Flow<List<Tag>> = wishTagRelationQueries
            .getWishTags(id)
            .asFlow()
            .mapToList(dispatcherProvider.io())
        return wishDtoFlow.combine(tagsFlow) { wishDto, tags ->
            wishDto.toWishWithTags(tags)
        }
    }

    override suspend fun getWishById(id: String): WishWithTags {
        return withContext(dispatcherProvider.io()) {
            val wishDto: Wish = wishQueries
                .getById(id)
                .executeAsOne()
            val tags: List<Tag> = wishTagRelationQueries
                .getWishTags(id)
                .executeAsList()
            wishDto.toWishWithTags(tags)
        }
    }

    override fun observeAllWishes(): Flow<List<WishWithTags>> {
        val wishesFlow: Flow<List<Wish>> = wishQueries
            .getAll()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags count in wish
        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags content in wish
        val tagsFlow: Flow<List<Tag>> = observeAllTags()

        return combine(wishesFlow, wishTagRelationsFlow, tagsFlow) { wishes, _, _ ->
            val wishesWithTags = mutableListOf<WishWithTags>()
            for (wish in wishes) {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(wish.wishId)
                    .executeAsList()
                wishesWithTags.add(wish.toWishWithTags(tags))
            }
            wishesWithTags.toList()
        }
    }

    override suspend fun getAllWishes(): List<WishWithTags> {
        return withContext(dispatcherProvider.io()) {
            wishQueries
                .getAll()
                .executeAsList()
                .map { wishDto ->
                    val tags: List<Tag> = wishTagRelationQueries
                        .getWishTags(wishDto.wishId)
                        .executeAsList()
                    wishDto.toWishWithTags(tags)
                }
        }
    }

    override fun observeWishesByTag(tagId: String): Flow<List<WishWithTags>> {

        val wishesByTagFlow: Flow<List<Wish>> = wishTagRelationQueries
            .getAllWishesByTag(tagId)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags content in wish
        val tagsFlow: Flow<List<Tag>> = observeAllTags()

        return combine(wishesByTagFlow, tagsFlow) { wishes, _ ->
            val wishesWithTags = mutableListOf<WishWithTags>()
            for (wish in wishes) {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(wish.wishId)
                    .executeAsList()
                wishesWithTags.add(wish.toWishWithTags(tags))
            }
            wishesWithTags
        }
    }

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

    override fun clearAllWishes() {
        wishQueries.clear()
    }
    // end region WishesRepository

    // region WishTagRelationRepository
    override fun insertWishTagRelation(wishId: String, tagId: String) {
        wishTagRelationQueries.insert(wishId, tagId)
    }

    override fun deleteWishTagRelation(wishId: String, tagId: String) {
        wishTagRelationQueries.delete(wishId, tagId)
    }

    // end region WishTagRelationRepository

    // region TagsRepository
    override fun insertTag(tag: Tag) {
        tagQueries.insert(tag.tagId, tag.title)
    }

    override fun updateTagTitle(title: String, tagId: String) {
        tagQueries.updateTitle(title, tagId)
    }

    override suspend fun getAllTags(): List<Tag> {
        return withContext(dispatcherProvider.io()) {
            tagQueries
                .getAll()
                .executeAsList()
        }
    }

    override fun observeAllTags(): Flow<List<Tag>> {
        return tagQueries
            .getAll()
            .asFlow()
            .mapToList(dispatcherProvider.io())
    }

    override fun observeTagsByWishId(wishId: String): Flow<List<Tag>> {
        return wishTagRelationQueries
            .getWishTags(wishId)
            .asFlow()
            .mapToList(dispatcherProvider.io())
    }

    override fun deleteTagsByIds(ids: List<String>) {
        tagQueries.transaction {
            tagQueries.deleteByIds(ids)
            wishTagRelationQueries.deleteByTagIds(ids)
        }
    }

    override fun clearAllTags() {
        tagQueries.clear()
    }

    // end region TagsRepository
}