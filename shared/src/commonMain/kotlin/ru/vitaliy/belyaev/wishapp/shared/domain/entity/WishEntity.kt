package ru.vitaliy.belyaev.wishapp.shared.domain.entity

import com.benasher44.uuid.uuid4
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ru.vitaliy.belyaev.wishapp.shared.utils.nowEpochMillis

data class WishEntity(
    val id: String,
    val title: String,
    val link: String,
    val links: List<String>,
    val comment: String,
    val isCompleted: Boolean,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val position: Long,
    val tags: List<TagEntity> = emptyList(),
    val images: List<ImageRef> = emptyList(),
    /** Когда отмечена «Сбылось». `null` у несбывшихся и у старых сбывшихся, у которых даты нет. */
    val completedTimestamp: Long? = null,
    /** Ярлык на карточке: выбранный в «Показывать на карточке», иначе первый добавленный. */
    val shownTagId: String? = null,
)

@OptIn(ExperimentalTime::class)
fun createEmptyWish(): WishEntity {
    val currentMillis = Clock.System.nowEpochMillis()
    return WishEntity(
        id = uuid4().toString(),
        title = "",
        link = "",
        links = emptyList(),
        comment = "",
        isCompleted = false,
        createdTimestamp = currentMillis,
        updatedTimestamp = currentMillis,
        position = 0,
        tags = emptyList(),
        images = emptyList(),
        completedTimestamp = null,
        shownTagId = null,
    )
}

fun WishEntity.isEmpty(): Boolean = title.isBlank() && link.isBlank() && comment.isBlank()

// For iOS, as Kotlin extension functions are not supported in Swift
fun isWishEmpty(wish: WishEntity): Boolean = wish.isEmpty()
