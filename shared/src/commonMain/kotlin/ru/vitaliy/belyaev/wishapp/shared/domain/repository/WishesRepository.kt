package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

interface WishesRepository {

    @NativeCoroutines
    suspend fun insertWish(wish: WishEntity)

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

    fun observeWishById(id: String): Flow<WishEntity>
    suspend fun getWishById(id: String): WishEntity

    @NativeCoroutines
    fun observeAllWishes(isCompleted: Boolean): Flow<List<WishEntity>>
    suspend fun getAllWishes(isCompleted: Boolean): List<WishEntity>

    fun observeWishesCount(isCompleted: Boolean): Flow<Long>

    suspend fun getWishesCount(isCompleted: Boolean): Long

    fun observeWishesByTag(tagId: String): Flow<List<WishEntity>>

    suspend fun deleteWishesByIds(ids: List<String>)
    fun clearAllWishes()
}