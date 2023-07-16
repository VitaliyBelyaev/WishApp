package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

data class WishItem(
    val wish: WishEntity,
    val linkPreviewState: LinkPreviewState
)

fun WishEntity.toDefaultWishItem(): WishItem {
    return if (link.isNotBlank()) {
        WishItem(this, Loading)
    } else {
        WishItem(this, None)
    }
}

fun WishEntity.toWishItem(linkPreviewState: LinkPreviewState): WishItem =
    WishItem(this, linkPreviewState)



