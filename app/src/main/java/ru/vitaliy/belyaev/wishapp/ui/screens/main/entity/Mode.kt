package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

sealed class Mode

object View : Mode()
data class Edit(val selectedIds: List<String>) : Mode()