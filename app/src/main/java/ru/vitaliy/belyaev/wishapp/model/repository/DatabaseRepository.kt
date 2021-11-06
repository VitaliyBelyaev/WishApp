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

    fun getById(id: String): Flow<Wish>

    fun getAll(): Flow<List<Wish>>

    fun deleteByIds(ids: List<String>)

    fun clearAll()
}