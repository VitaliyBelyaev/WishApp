package ru.vitaliy.belyaev.wishapp.data.repository.wishes

import java.util.UUID
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.data.database.Wish
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

fun createEmptyWish(): Wish {
    val currentMillis = System.currentTimeMillis()
    return Wish(
        wishId = UUID.randomUUID().toString(),
        title = "",
        link = "",
        comment = "",
        isCompleted = false,
        createdTimestamp = currentMillis,
        updatedTimestamp = currentMillis,
        position = 0,
    )
}

fun Wish.toWishWithTags(tags: List<Tag>): WishWithTags {
    return WishWithTags(wishId, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp, position, tags)
}

fun WishWithTags.isEmpty(): Boolean = title.isBlank() && link.isBlank() && comment.isBlank()
