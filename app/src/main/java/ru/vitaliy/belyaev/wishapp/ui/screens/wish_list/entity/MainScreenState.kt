package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

data class MainScreenState(
    val wishes: List<WishEntity> = emptyList(),
    val selectedIds: List<String> = emptyList(),
    val wishesFilter: WishesFilter = WishesFilter.All,
    val reorderButtonState: ReorderButtonState = ReorderButtonState.Visible(false),
    val isLoading: Boolean = true,
    val isShareAsPdfLoading: Boolean = false,
)

sealed class ReorderButtonState {

    object Hidden : ReorderButtonState()

    data class Visible(val isEnabled: Boolean) : ReorderButtonState()
}
