package ru.vitaliy.belyaev.wishapp.data.repository.wishes

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

interface WishesRepository {

    suspend fun insertWish(wishWithTags: WishWithTags)

    suspend fun updateWishTitle(newValue: String, wishId: String)
    suspend fun updateWishLink(newValue: String, wishId: String)
    suspend fun updateWishComment(newValue: String, wishId: String)
    suspend fun updateWishIsCompleted(newValue: Boolean, wishId: String)

    fun observeWishById(id: String): Flow<WishWithTags>
    suspend fun getWishById(id: String): WishWithTags

    fun observeAllWishes(): Flow<List<WishWithTags>>
    suspend fun getAllWishes(): List<WishWithTags>

    fun observeWishesByTag(tagId: String): Flow<List<WishWithTags>>

    suspend fun deleteWishesByIds(ids: List<String>)
    fun clearAllWishes()
}