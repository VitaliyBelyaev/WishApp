package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import ru.vitaliy.belyaev.wishapp.data.database.Tag
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

data class MainScreenState(
    val wishes: List<WishWithTags> = emptyList(),
    val selectedIds: List<String> = emptyList(),
    val selectedTag: Tag? = null
)
