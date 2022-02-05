package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import androidx.annotation.StringRes
import ru.vitaliy.belyaev.wishapp.data.database.Tag

sealed class NavigationMenuItem(
    open val isSelected: Boolean
)

class AllTagsMenuItem(
    @StringRes val titleRes: Int,
    override val isSelected: Boolean
) : NavigationMenuItem(isSelected)

class TagMenuItem(
    val tag: Tag,
    override val isSelected: Boolean
) : NavigationMenuItem(isSelected)