package ru.vitaliy.belyaev.wishapp.entity

import java.util.UUID
import ru.vitaliy.belyaev.model.database.Wish

fun createEmptyWish(): Wish {
    val currentMillis = System.currentTimeMillis()
    return Wish(
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

fun Wish.isEmpty(): Boolean = title.isBlank() && link.isBlank() && comment.isBlank()
