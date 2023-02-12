package ru.vitaliy.belyaev.wishapp.shared.domain.entity

data class TagWithWishCount(
    val tag: TagEntity,
    val wishesCount: Long
)