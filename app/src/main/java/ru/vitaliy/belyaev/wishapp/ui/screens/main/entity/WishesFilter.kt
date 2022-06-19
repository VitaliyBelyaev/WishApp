package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import ru.vitaliy.belyaev.wishapp.data.database.Tag

sealed class WishesFilter {

    data class ByTag(val tag: Tag) : WishesFilter()
    object All : WishesFilter()
    object Completed : WishesFilter()
}