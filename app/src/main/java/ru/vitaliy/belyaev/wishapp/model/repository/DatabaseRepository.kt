package ru.vitaliy.belyaev.wishapp.model.repository

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.model.database.Wish

interface DatabaseRepository {

    fun insert(wish: Wish)

    fun updateTitle(newValue: String, wishId: String)
    fun updateLink(newValue: String, wishId: String)
    fun updateComment(newValue: String, wishId: String)
    fun updateIsCompleted(newValue: Boolean, wishId: String)
    fun updateTags(newValue: List<String>, wishId: String)

    fun getByIdFlow(id: String): Flow<Wish>

    suspend fun getById(id: String): Wish

    fun getAllFlow(): Flow<List<Wish>>

    suspend fun getAll(): List<Wish>

    suspend fun deleteByIds(ids: List<String>)

    fun clearAll()
}