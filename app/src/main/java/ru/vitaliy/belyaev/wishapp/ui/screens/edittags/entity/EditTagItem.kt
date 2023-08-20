package ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

data class EditTagItem(
    val tag: TagEntity,
    val isEditMode: Boolean
)