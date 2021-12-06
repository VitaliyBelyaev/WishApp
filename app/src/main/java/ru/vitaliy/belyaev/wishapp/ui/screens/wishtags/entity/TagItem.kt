package ru.vitaliy.belyaev.wishapp.ui.screens.wishtags.entity

import ru.vitaliy.belyaev.model.database.Tag

data class TagItem(
    val tag: Tag,
    val isSelected: Boolean
)

fun Tag.toTagItem(isSelected: Boolean): TagItem {
    return TagItem(this, isSelected)
}
