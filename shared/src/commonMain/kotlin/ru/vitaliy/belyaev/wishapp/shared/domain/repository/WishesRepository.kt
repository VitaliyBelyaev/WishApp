package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

interface WishesRepository {

    @NativeCoroutines
    suspend fun insertWish(wish: WishEntity)

    @NativeCoroutines
    suspend fun updateWishTitle(newValue: String, wishId: String)

    @NativeCoroutines
    suspend fun updateWishLink(newValue: String, wishId: String)

    @NativeCoroutines
    suspend fun updateWishComment(newValue: String, wishId: String)

    @NativeCoroutines
    suspend fun updateWishIsCompleted(newValue: Boolean, wishId: String)

    @NativeCoroutines
    suspend fun updatePosition(newValue: Long, wishId: String)

    @NativeCoroutines
    suspend fun swapWishesPositions(
        wish1Id: String,
        wish1Position: Long,
        wish2Id: String,
        wish2Position: Long,
    )

    @NativeCoroutines
    suspend fun swapMovedWishPositionWithPassedWishes(
        wishId: String,
        wishPosition: Long,
        passedWishes: List<WishEntity>,
    )

    @NativeCoroutines
    suspend fun updatePositionsOnItemMove(startIndex: Int, endIndex: Int, wishId: String, isMoveDown: Boolean)

    @NativeCoroutines
    fun observeWishById(id: String): Flow<WishEntity>

    @NativeCoroutines
    suspend fun getWishById(id: String): WishEntity

    @NativeCoroutines
    fun observeAllWishes(isCompleted: Boolean): Flow<List<WishEntity>>

    @NativeCoroutines
    suspend fun getAllWishes(isCompleted: Boolean): List<WishEntity>

    @NativeCoroutines
    fun observeWishesCount(isCompleted: Boolean): Flow<Long>

    @NativeCoroutines
    suspend fun getWishesCount(isCompleted: Boolean): Long

    @NativeCoroutines
    fun observeWishesByTag(tagId: String): Flow<List<WishEntity>>

    @NativeCoroutines
    suspend fun deleteWishesByIds(ids: List<String>)

    @NativeCoroutines
    suspend fun clearAllWishes()
}