package ru.vitaliy.belyaev.wishapp.entity

import ru.vitaliy.belyaev.wishapp.data.database.Tag

data class TagWithWishCount(
    val tag: Tag,
    val wishesCount: Long
)