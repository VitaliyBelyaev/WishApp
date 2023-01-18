package ru.vitaliy.belyaev.wishapp.domain

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vitaliy.belyaev.wishapp.data.repository.wishes.WishesRepository
import ru.vitaliy.belyaev.wishapp.data.repository.wishes.isEmpty
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

class WishesInteractor @Inject constructor(
    private val wishesRepository: WishesRepository
) {

    fun observeNotCompletedWishes(): Flow<List<WishWithTags>> {
        return wishesRepository
            .observeAllWishes()
            .map { wishes -> wishes.filter { !it.isEmpty() && !it.isCompleted } }
    }

    fun observeCompletedWishes(): Flow<List<WishWithTags>> {
        return wishesRepository
            .observeAllWishes()
            .map { wishes -> wishes.filter { !it.isEmpty() && it.isCompleted } }
    }

    fun observeNotCompletedWishesByTag(tagId: String): Flow<List<WishWithTags>> {
        return wishesRepository
            .observeWishesByTag(tagId)
            .map { wishes -> wishes.filter { !it.isEmpty() && !it.isCompleted } }
    }

    fun observeWishesCount(isCompleted: Boolean): Flow<Long> {
        return wishesRepository.observeWishesCount(isCompleted = isCompleted)
    }
}