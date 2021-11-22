package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

data class MainScreenState(
    val mode: Mode = View,
    val wishes: List<WishItem> = emptyList()
)
