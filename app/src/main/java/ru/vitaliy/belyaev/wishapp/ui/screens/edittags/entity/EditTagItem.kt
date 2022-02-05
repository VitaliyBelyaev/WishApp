package ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity

import ru.vitaliy.belyaev.wishapp.data.database.Tag

data class EditTagItem(
    val tag: Tag,
    val isEditMode: Boolean
)