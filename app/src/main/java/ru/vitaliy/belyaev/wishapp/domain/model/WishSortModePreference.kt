package ru.vitaliy.belyaev.wishapp.domain.model

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode

// Значения хранятся в DataStore: не менять и не переиспользовать
private const val INT_DEFAULT = 0
private const val INT_CREATED_NEWEST = 1
private const val INT_CREATED_OLDEST = 2
private const val INT_TITLE_AZ = 3
private const val INT_TITLE_ZA = 4

fun Int?.toWishSortMode(): WishSortMode =
    when (this) {
        INT_CREATED_NEWEST -> WishSortMode.CreatedDateNewest
        INT_CREATED_OLDEST -> WishSortMode.CreatedDateOldest
        INT_TITLE_AZ -> WishSortMode.TitleAZ
        INT_TITLE_ZA -> WishSortMode.TitleZA
        else -> WishSortMode.Default
    }

fun WishSortMode.toInt(): Int =
    when (this) {
        WishSortMode.Default -> INT_DEFAULT
        WishSortMode.CreatedDateNewest -> INT_CREATED_NEWEST
        WishSortMode.CreatedDateOldest -> INT_CREATED_OLDEST
        WishSortMode.TitleAZ -> INT_TITLE_AZ
        WishSortMode.TitleZA -> INT_TITLE_ZA
    }
