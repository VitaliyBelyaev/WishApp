package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity

import androidx.annotation.StringRes
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

class AllTagsMenuItem(
    @StringRes val titleRes: Int,
    val isSelected: Boolean
)

class TagMenuItem(
    val tag: TagEntity,
    val isSelected: Boolean
)