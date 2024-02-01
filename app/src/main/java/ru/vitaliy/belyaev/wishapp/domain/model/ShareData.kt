package ru.vitaliy.belyaev.wishapp.domain.model

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

sealed class ShareData {

    abstract val wishesToShare: List<WishEntity>

    data class Text(override val wishesToShare: List<WishEntity>) : ShareData()

    data class Pdf(override val wishesToShare: List<WishEntity>) : ShareData()
}