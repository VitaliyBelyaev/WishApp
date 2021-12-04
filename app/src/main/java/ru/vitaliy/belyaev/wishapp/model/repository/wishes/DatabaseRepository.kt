package ru.vitaliy.belyaev.wishapp.model.repository.wishes

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.model.database.GetAllWishesByTag
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.model.database.WishQueries
import ru.vitaliy.belyaev.model.database.WishTagRelation
import ru.vitaliy.belyaev.model.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb

class DatabaseRepository @Inject constructor(
    database: WishAppDb
) : WishesRepository {

    private val wishQueries: WishQueries = database.wishQueries
    private val wishTagRelationQueries: WishTagRelationQueries = database.wishTagRelationQueries

    override fun insertWish(wishWithTags: WishWithTags) {
        with(wishWithTags) {
            wishQueries.insert(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp)
        }
    }

    override fun updateTitle(newValue: String, wishId: String) {
        wishQueries.updateTitle(title = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateLink(newValue: String, wishId: String) {
        wishQueries.updateLink(link = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateComment(newValue: String, wishId: String) {
        wishQueries.updateComment(comment = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateIsCompleted(newValue: Boolean, wishId: String) {
        wishQueries.updateIsCompleted(
            isCompleted = newValue,
            updatedTimestamp = System.currentTimeMillis(),
            id = wishId
        )
    }

    override fun observeWishById(id: String): Flow<WishWithTags> {
        val wishDtoFlow: Flow<Wish> = wishQueries
            .getById(id)
            .asFlow()
            .mapToOne()
        val tagsFlow: Flow<List<Tag>> = wishTagRelationQueries
            .getWishTags(id)
            .asFlow()
            .mapToList()
            .map { tagDtos -> tagDtos.map { Tag(it.id, it.title) } }
        return wishDtoFlow.combine(tagsFlow) { wishDto, tags ->
            wishDto.toWishWithTags(tags)
        }
    }

    override suspend fun getWishById(id: String): WishWithTags {
        return withContext(Dispatchers.IO) {
            val wishDto: Wish = wishQueries
                .getById(id)
                .executeAsOne()
            val tags: List<Tag> = wishTagRelationQueries
                .getWishTags(id)
                .executeAsList()
                .map { Tag(it.id, it.title) }
            wishDto.toWishWithTags(tags)
        }
    }

    override fun observeAllWishes(): Flow<List<WishWithTags>> {
        val wishesDtoFlow: Flow<List<Wish>> = wishQueries
            .getAll()
            .asFlow()
            .mapToList()
        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList()

        return wishesDtoFlow.combine(wishTagRelationsFlow) { wishesDto, _ ->
            val wishesWithTags = mutableListOf<WishWithTags>()
            for (wishDto in wishesDto) {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(wishDto.id)
                    .executeAsList()
                    .map { Tag(it.id, it.title) }
                wishesWithTags.add(wishDto.toWishWithTags(tags))
            }
            wishesWithTags.toList()
        }
    }

    override suspend fun getAllWishes(): List<WishWithTags> {
        return withContext(Dispatchers.IO) {
            wishQueries
                .getAll()
                .executeAsList()
                .map { wishDto ->
                    val tags: List<Tag> = wishTagRelationQueries
                        .getWishTags(wishDto.id)
                        .executeAsList()
                        .map { Tag(it.id, it.title) }
                    wishDto.toWishWithTags(tags)
                }
        }
    }

    override fun observeWishesByTag(tagId: Long): Flow<List<WishWithTags>> {
        val wishesDtoFlow: Flow<List<GetAllWishesByTag>> = wishTagRelationQueries
            .getAllWishesByTag(tagId)
            .asFlow()
            .mapToList()

        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList()

        return wishesDtoFlow.combine(wishTagRelationsFlow) { wishesDto, _ ->
            val wishesWithTags = mutableListOf<WishWithTags>()
            for (wishDto in wishesDto) {
                val tags: List<Tag> = wishTagRelationQueries
                    .getWishTags(wishDto.id)
                    .executeAsList()
                    .map { Tag(it.id, it.title) }
                wishesWithTags.add(wishDto.toWishWithTags(tags))
            }
            wishesWithTags.toList()
        }


        return wishTagRelationQueries
            .getAllWishesByTag(tagId)
            .asFlow()
            .mapToList()
            .map { list ->
                val wishesWithTags = mutableListOf<WishWithTags>()
                for (wishDto in list) {
                    val tags: List<Tag> = wishTagRelationQueries
                        .getWishTags(wishDto.id)
                        .executeAsList()
                        .map { Tag(it.id, it.title) }
                    wishesWithTags.add(wishDto.toWishWithTags(tags))
                }
                wishesWithTags.toList()
            }
    }

    override suspend fun deleteWishesByIds(ids: List<String>) {
        withContext(Dispatchers.IO) {
            wishQueries.deleteByIds(ids)
        }
    }

    override fun clearAllWishes() {
        wishQueries.clear()
    }
}