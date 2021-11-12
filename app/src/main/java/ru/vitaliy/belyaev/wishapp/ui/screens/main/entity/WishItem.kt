package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import ru.vitaliy.belyaev.model.database.Wish

data class WishItem(
    val wish: Wish,
    val linkPreviewState: LinkPreviewState
)

fun Wish.toDefaultWishItem(): WishItem {
    return if (link.isNotBlank()) {
        WishItem(this, Loading)
    } else {
        WishItem(this, None)
    }
}

