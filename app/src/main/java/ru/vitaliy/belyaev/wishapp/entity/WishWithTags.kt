package ru.vitaliy.belyaev.wishapp.entity

import ru.vitaliy.belyaev.model.database.Tag

data class WishWithTags(
    val id: String,
    val title: String,
    val link: String,
    val comment: String,
    val isCompleted: Boolean,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val tags: List<Tag>
)