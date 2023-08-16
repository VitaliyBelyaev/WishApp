package ru.vitaliy.belyaev.wishapp.data.repository.wishes

import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.data.database.Wish
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

interface WishesRepository {

    suspend fun insertWish(wish: Wish)

    suspend fun updateWishTitle(newValue: String, wishId: String)
    suspend fun updateWishLink(newValue: String, wishId: String)
    suspend fun updateWishComment(newValue: String, wishId: String)
    suspend fun updateWishIsCompleted(newValue: Boolean, wishId: String)
    suspend fun updatePosition(newValue: Long, wishId: String)
    suspend fun swapWishesPositions(
        wish1Id: String,
        wish1Position: Long,
        wish2Id: String,
        wish2Position: Long,
    )

    suspend fun updatePositionsOnItemMove(startIndex: Int, endIndex: Int, wishId: String, isMoveDown: Boolean)

    fun observeWishById(id: String): Flow<WishWithTags>
    suspend fun getWishById(id: String): WishWithTags

    fun observeAllWishes(): Flow<List<WishWithTags>>
    suspend fun getAllWishes(): List<WishWithTags>

    fun observeWishesCount(isCompleted: Boolean): Flow<Long>

    suspend fun getWishesCount(isCompleted: Boolean): Long

    fun observeWishesByTag(tagId: String): Flow<List<WishWithTags>>

    suspend fun deleteWishesByIds(ids: List<String>)
    fun clearAllWishes()
}