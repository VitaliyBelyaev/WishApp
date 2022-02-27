package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import androidx.annotation.StringRes
import ru.vitaliy.belyaev.wishapp.data.database.Tag

class AllTagsMenuItem(
    @StringRes val titleRes: Int,
    val isSelected: Boolean
)

class TagMenuItem(
    val tag: Tag,
    val isSelected: Boolean
)