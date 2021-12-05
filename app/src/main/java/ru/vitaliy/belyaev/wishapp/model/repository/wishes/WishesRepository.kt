package ru.vitaliy.belyaev.wishapp.model.repository.wishes

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

interface WishesRepository {

    fun insertWish(wishWithTags: WishWithTags)

    fun updateWishTitle(newValue: String, wishId: String)
    fun updateWishLink(newValue: String, wishId: String)
    fun updateWishComment(newValue: String, wishId: String)
    fun updateWishIsCompleted(newValue: Boolean, wishId: String)

    fun observeWishById(id: String): Flow<WishWithTags>
    suspend fun getWishById(id: String): WishWithTags

    fun observeAllWishes(): Flow<List<WishWithTags>>
    suspend fun getAllWishes(): List<WishWithTags>

    fun observeWishesByTag(tagId: Long): Flow<List<WishWithTags>>

    suspend fun deleteWishesByIds(ids: List<String>)
    fun clearAllWishes()
}