package ru.vitaliy.belyaev.wishapp.model.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.model.database.WishQueries
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb

class WishAppDatabaseRepository @Inject constructor(
    database: WishAppDb
) : DatabaseRepository {

    private val queries: WishQueries = database.wishQueries

    override fun insert(wish: Wish) {
        with(wish) {
            queries.insert(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp, tags)
        }
    }

    override fun updateTitle(newValue: String, wishId: String) {
        queries.updateTitle(title = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateLink(newValue: String, wishId: String) {
        queries.updateLink(link = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateComment(newValue: String, wishId: String) {
        queries.updateComment(comment = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateIsCompleted(newValue: Boolean, wishId: String) {
        queries.updateIsCompleted(isCompleted = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun updateTags(newValue: List<String>, wishId: String) {
        queries.updateTags(tags = newValue, updatedTimestamp = System.currentTimeMillis(), id = wishId)
    }

    override fun getByIdFlow(id: String): Flow<Wish> =
        queries
            .getById(id)
            .asFlow()
            .mapToOne()

    override suspend fun getById(id: String): Wish {
        return withContext(Dispatchers.IO) {
            queries
                .getById(id)
                .executeAsOne()
        }
    }

    override fun getAllFlow(): Flow<List<Wish>> =
        queries
            .getAll()
            .asFlow()
            .mapToList()

    override suspend fun getAll(): List<Wish> {
        return withContext(Dispatchers.IO) {
            queries
                .getAll()
                .executeAsList()
        }
    }

    override fun deleteByIds(ids: List<String>) {
        queries.deleteByIds(ids)
    }

    override fun clearAll() {
        queries.clear()
    }
}