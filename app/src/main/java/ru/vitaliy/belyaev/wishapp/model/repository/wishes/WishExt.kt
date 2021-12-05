package ru.vitaliy.belyaev.wishapp.model.repository.wishes

import java.util.*
import ru.vitaliy.belyaev.model.database.GetAllWishesByTag
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.model.database.Wish
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
    return WishWithTags(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp, tags)
}

fun GetAllWishesByTag.toWishWithTags(tags: List<Tag>): WishWithTags {
    return WishWithTags(id, title, link, comment, isCompleted, createdTimestamp, updatedTimestamp, tags)
}

fun WishWithTags.isEmpty(): Boolean = title.isBlank() && link.isBlank() && comment.isBlank()