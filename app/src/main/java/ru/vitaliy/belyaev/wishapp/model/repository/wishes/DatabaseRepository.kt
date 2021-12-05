package ru.vitaliy.belyaev.wishapp.model.repository.wishes

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
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.model.database.WishQueries
import ru.vitaliy.belyaev.model.database.WishTagRelation
import ru.vitaliy.belyaev.model.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider

@Singleton
class DatabaseRepository @Inject constructor(
    database: WishAppDb,
    private val dispatcherProvider: DispatcherProvider
) : WishesRepository {

    private val wishQueries: WishQueries = database.wishQueries
    private val wishTagRelationQueries: WishTagRelationQueries = database.wishTagRelationQueries

    override fun insertWish(wishWithTags: WishWithTags) {
        with(wishWithTags) {
            wishQueries.insert(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp)
        }
    }

    override fun updateWishTitle(newValue: String, wishId: String) {
        wishQueries.updateTitle(title = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateWishLink(newValue: String, wishId: String) {
        wishQueries.updateLink(link = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateWishComment(newValue: String, wishId: String) {
        wishQueries.updateComment(comment = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateWishIsCompleted(newValue: Boolean, wishId: String) {
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
            .mapToOne(dispatcherProvider.io())
        val tagsFlow: Flow<List<Tag>> = wishTagRelationQueries
            .getWishTags(id)
            .asFlow()
            .mapToList(dispatcherProvider.io())
            .map { tagDtos -> tagDtos.map { Tag(it.id, it.title) } }
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
                .map { Tag(it.id, it.title) }
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
        // We need this for reactive changes of tags
        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList(dispatcherProvider.io())

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
        return withContext(dispatcherProvider.io()) {
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
            .mapToList(dispatcherProvider.io())

        val wishTagRelationsFlow: Flow<List<WishTagRelation>> = wishTagRelationQueries
            .getAllRelations()
            .asFlow()
            .mapToList(dispatcherProvider.io())

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

    override suspend fun deleteWishesByIds(ids: List<String>) {
        withContext(dispatcherProvider.io()) {
            wishQueries.deleteByIds(ids)
        }
    }

    override fun clearAllWishes() {
        wishQueries.clear()
    }
}