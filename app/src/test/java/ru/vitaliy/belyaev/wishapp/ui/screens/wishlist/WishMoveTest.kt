package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

/**
 * Порядок и позиции после переноса должны совпасть с тем, что запишет
 * `swapMovedWishPositionWithPassedWishes`: иначе после ответа базы карточки прыгнут.
 */
class WishMoveTest {

    // Раздел отсортирован по position DESC, как «Мой порядок»
    private val wishes = listOf(wish("a", 10), wish("b", 7), wish("c", 5), wish("d", 2))

    @Test
    fun moveDown_takesPositionOfLastPassed() {
        val passed = wishes.subList(1, 3)
        val result = wishes.withMove(fromIndex = 0, toIndex = 2, passed = passed)

        assertEquals(listOf("b", "c", "a", "d"), result.map { it.id })
        assertEquals(listOf(10L, 7L, 5L, 2L), result.map { it.position })
    }

    @Test
    fun moveUp_passedGoInTravelOrder() {
        val passed = wishes.subList(1, 3).asReversed()
        val result = wishes.withMove(fromIndex = 3, toIndex = 1, passed = passed)

        assertEquals(listOf("a", "d", "b", "c"), result.map { it.id })
        assertEquals(listOf(10L, 7L, 5L, 2L), result.map { it.position })
    }

    @Test
    fun tagFilter_gapsInPositionsStay() {
        // В ярлыке позиции идут с пропусками: чужие хотелки между ними не трогаются
        val tagWishes = listOf(wish("a", 9), wish("b", 4), wish("c", 1))
        val result = tagWishes.withMove(fromIndex = 2, toIndex = 0, passed = tagWishes.subList(0, 2).asReversed())

        assertEquals(listOf("c", "a", "b"), result.map { it.id })
        assertEquals(listOf(9L, 4L, 1L), result.map { it.position })
    }

    private fun wish(id: String, position: Long) = WishEntity(
        id = id,
        title = id,
        link = "",
        links = emptyList(),
        comment = "",
        isCompleted = false,
        createdTimestamp = 0,
        updatedTimestamp = 0,
        position = position,
    )
}
