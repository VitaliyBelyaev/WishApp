package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

data class MainScreenState(
    val wishes: List<WishItem> = emptyList(),
    val selectedIds: List<String> = emptyList()
)
