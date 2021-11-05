package ru.vitaliy.belyaev.wishapp.model.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.model.database.WishAppDb

class WishAppDatabaseRepository @Inject constructor(
    private val database: WishAppDb
) : DatabaseRepository {

    override fun insert(wish: Wish) {
        with(wish) {
            database.wishQueries.insert(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp, tags)
        }
    }

    override fun getById(id: String): Flow<Wish> {
        return database
            .wishQueries
            .getById(id)
            .asFlow()
            .mapToOne()
    }

    override fun getAll(): Flow<List<Wish>> {
        return database.wishQueries
            .getAll()
            .asFlow()
            .mapToList()
    }

    override fun deleteByIds(ids: List<String>) {
        database.wishQueries.deleteByIds(ids)
    }

    override fun clearAll() {
        database.wishQueries.clear()
    }
}