package ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity

import ru.vitaliy.belyaev.model.database.Tag

data class EditTagItem(
    val tag: Tag,
    val isEditMode: Boolean
)