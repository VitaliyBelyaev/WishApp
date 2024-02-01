package ru.vitaliy.belyaev.wishapp.shared.domain.entity

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
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
    val images: List<ImageEntity> = emptyList(),
)

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
    )
}

fun WishEntity.isEmpty(): Boolean = title.isBlank() && link.isBlank() && comment.isBlank()

// For iOS, as Kotlin extension functions are not supported in Swift
fun isWishEmpty(wish: WishEntity): Boolean = wish.isEmpty()