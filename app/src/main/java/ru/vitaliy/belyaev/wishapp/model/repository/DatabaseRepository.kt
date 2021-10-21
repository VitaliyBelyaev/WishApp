package ru.vitaliy.belyaev.wishapp.model.repository

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.model.database.Wish

interface DatabaseRepository {

    fun insert(wish: Wish)

    fun getAll(): Flow<List<Wish>>

    fun deleteByIds(ids: List<String>)

    fun clearAll()
}