package ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

data class WishDetailedState(
    val wish: WishEntity,
    val isAddLinkButtonEnabled: Boolean,
)