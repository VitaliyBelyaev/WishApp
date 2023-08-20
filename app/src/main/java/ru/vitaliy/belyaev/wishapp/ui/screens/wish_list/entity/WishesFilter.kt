package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

sealed class WishesFilter {

    data class ByTag(val tag: TagEntity) : WishesFilter()
    object All : WishesFilter()
    object Completed : WishesFilter()
}