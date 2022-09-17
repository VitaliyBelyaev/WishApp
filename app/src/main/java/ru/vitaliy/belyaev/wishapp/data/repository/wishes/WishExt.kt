package ru.vitaliy.belyaev.wishapp.data.repository.wishes

import java.util.UUID
import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.data.database.Wish
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

fun createEmptyWish(): WishWithTags {
    val currentMillis = System.currentTimeMillis()
    return WishWithTags(
        id = UUID.randomUUID().toString(),
        title = "",
        link = "",
        comment = "",
        isCompleted = false,
        createdTimestamp = currentMillis,
        updatedTimestamp = currentMillis,
        tags = emptyList()
    )
}

fun WishWithTags.toWishDto(): Wish {
    return Wish(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp)
}

fun Wish.toWishWithTags(tags: List<Tag>): WishWithTags {
    return WishWithTags(wishId, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp, tags)
}

fun WishWithTags.isEmpty(): Boolean = title.isBlank() && link.isBlank() && comment.isBlank()
