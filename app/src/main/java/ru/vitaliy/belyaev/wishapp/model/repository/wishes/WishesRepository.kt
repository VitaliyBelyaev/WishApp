package ru.vitaliy.belyaev.wishapp.model.repository.wishes

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

interface WishesRepository {

    fun insertWish(wishWithTags: WishWithTags)

    fun updateTitle(newValue: String, wishId: String)
    fun updateLink(newValue: String, wishId: String)
    fun updateComment(newValue: String, wishId: String)
    fun updateIsCompleted(newValue: Boolean, wishId: String)

    fun observeWishById(id: String): Flow<WishWithTags>
    suspend fun getWishById(id: String): WishWithTags

    fun observeAllWishes(): Flow<List<WishWithTags>>
    suspend fun getAllWishes(): List<WishWithTags>

    fun observeWishesByTag(tagId: Long): Flow<List<WishWithTags>>

    suspend fun deleteWishesByIds(ids: List<String>)
    fun clearAllWishes()
}