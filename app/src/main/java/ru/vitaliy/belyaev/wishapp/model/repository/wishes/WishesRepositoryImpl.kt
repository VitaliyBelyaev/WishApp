package ru.vitaliy.belyaev.wishapp.model.repository.wishes

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.model.database.GetAllWishesByTag
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.model.database.WishQueries
import ru.vitaliy.belyaev.model.database.WishTagRelationQueries
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb

class WishesRepositoryImpl @Inject constructor(
    database: WishAppDb
) : WishesRepository {

    private val wishQueries: WishQueries = database.wishQueries
    private val wishTagRelationQueries: WishTagRelationQueries = database.wishTagRelationQueries

    override fun insert(wish: Wish) {
        with(wish) {
            wishQueries.insert(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp, tags)
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

    override fun updateTags(newValue: List<String>, wishId: String) {
        wishQueries.updateTags(tags = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun getByIdFlow(id: String): Flow<Wish> =
        wishQueries
            .getById(id)
            .asFlow()
            .mapToOne()

    override suspend fun getById(id: String): Wish {
        return withContext(Dispatchers.IO) {
            wishQueries
                .getById(id)
                .executeAsOne()
        }
    }

    override fun getByAllWishesByTag(tagId: Long): Flow<List<GetAllWishesByTag>> {
        return wishTagRelationQueries
            .getAllWishesByTag(tagId)
            .asFlow()
            .mapToList()
    }

    override fun getAllTags(): Flow<List<List<String>>> {
        return wishQueries
            .getAllTags()
            .asFlow()
            .mapToList()
    }

    override fun getAllFlow(): Flow<List<Wish>> =
        wishQueries
            .getAll()
            .asFlow()
            .mapToList()

    override suspend fun getAll(): List<Wish> {
        return withContext(Dispatchers.IO) {
            wishQueries
                .getAll()
                .executeAsList()
        }
    }

    override suspend fun deleteByIds(ids: List<String>) {
        withContext(Dispatchers.IO) {
            wishQueries.deleteByIds(ids)
        }
    }

    override fun clearAll() {
        wishQueries.clear()
    }
}