package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

data class WishItem(
    val wish: WishWithTags,
    val linkPreviewState: LinkPreviewState
)

fun WishWithTags.toDefaultWishItem(): WishItem {
    return if (link.isNotBlank()) {
        WishItem(this, Loading)
    } else {
        WishItem(this, None)
    }
}

fun WishWithTags.toWishItem(linkPreviewState: LinkPreviewState): WishItem =
    WishItem(this, linkPreviewState)



