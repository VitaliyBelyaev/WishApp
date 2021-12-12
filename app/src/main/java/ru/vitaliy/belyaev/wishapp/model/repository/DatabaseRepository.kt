package ru.vitaliy.belyaev.wishapp.model.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.model.database.GetAllWishesByTag
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.model.database.TagQueries
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.model.database.WishQueries
import ru.vitaliy.belyaev.model.database.WishTagRelation
import ru.vitaliy.belyaev.model.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.model.repository.tags.TagsRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.model.repository.wishes.toWishWithTags
import ru.vitaliy.belyaev.wishapp.model.repository.wishtagrelation.WishTagRelationRepository
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
    override suspend fun insertWish(wishWithTags: WishWithTags) {
        withContext(dispatcherProvider.io()) {
            with(wishWithTags) {
                wishQueries.insert(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp)
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
            wishQueries.updateIsCompleted(
                isCompleted = newValue,
                updatedTimestamp = System.currentTimeMillis(),
                wishId = wishId
            )
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
            .map { tagDtos -> tagDtos.map { Tag(it.tagId, it.title) } }
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
                .map { Tag(it.tagId, it.title) }
            wishDto.toWishWithTags(tags)
        }
    }

    override fun observeAllWishes(): Flow<List<WishWithTags>> {
        // Вот это работает в тестах, а когда делаю combine, то This job has not completed yet
//        return wishQueries
//            .getAll()
//            .asFlow()
//            .mapToList(dispatcherProvider.io())
//            .map { wishesDto ->
//                val wishesWithTags = mutableListOf<WishWithTags>()
//                for (wishDto in wishesDto) {
//                    wishesWithTags.add(wishDto.toWishWithTags(emptyList()))
//                }
//                wishesWithTags.toList()
//            }

        val wishesDtoFlow: Flow<List<Wish>> = wishQueries
            .getAll()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags in wish
        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        // We need this for reactive changes of tags
        val tagFlow: Flow<List<Tag>> = tagQueries
            .getAll()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        return combine(wishesDtoFlow, wishTagRelationsFlow, tagFlow) { wishesDto, _, _ ->
            val wishesWithTags = mutableListOf<WishWithTags>()
            for (wishDto in wishesDto) {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(wishDto.wishId)
                    .executeAsList()
                    .map { Tag(it.tagId, it.title) }
                wishesWithTags.add(wishDto.toWishWithTags(tags))
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
                        .map { Tag(it.tagId, it.title) }
                    wishDto.toWishWithTags(tags)
                }
        }
    }

    override fun observeWishesByTag(tagId: String): Flow<List<WishWithTags>> {
        val wishesDtoFlow: Flow<List<GetAllWishesByTag>> = wishTagRelationQueries
            .getAllWishesByTag(tagId)
            .asFlow()
            .mapToList(dispatcherProvider.io())

        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList(dispatcherProvider.io())

        return wishesDtoFlow.combine(wishTagRelationsFlow) { wishesDto, _ ->
            val wishesWithTags = mutableListOf<WishWithTags>()
            for (wishDto in wishesDto) {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(wishDto.wishId_)
                    .executeAsList()
                    .map { Tag(it.tagId, it.title) }
                wishesWithTags.add(wishDto.toWishWithTags(tags))
            }
            wishesWithTags.toList()
        }
    }

    override suspend fun deleteWishesByIds(ids: List<String>) {
        withContext(dispatcherProvider.io()) {
            wishQueries.deleteByIds(ids)
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
            .map { list -> list.map { Tag(it.tagId_, it.title) } }
    }

    override fun deleteTagsByIds(ids: List<String>) {
        tagQueries.deleteByIds(ids)
    }

    override fun clearAllTags() {
        tagQueries.clear()
    }

    // end region TagsRepository
}