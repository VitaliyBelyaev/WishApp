package ru.vitaliy.belyaev.wishapp.shared.domain.entity

sealed class WishSortMode {

    data object Default : WishSortMode()

    data object CreatedDateNewest : WishSortMode()

    data object CreatedDateOldest : WishSortMode()

    data object TitleAZ : WishSortMode()

    data object TitleZA : WishSortMode()
}
