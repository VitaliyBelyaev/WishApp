package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

data class TagItem(
    val tag: TagEntity,
    val isSelected: Boolean
)

fun TagEntity.toTagItem(isSelected: Boolean): TagItem {
    return TagItem(this, isSelected)
}
