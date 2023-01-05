package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

import ru.vitaliy.belyaev.wishapp.entity.WishWithTags

data class MainScreenState(
    val wishes: List<WishWithTags> = emptyList(),
    val selectedIds: List<String> = emptyList(),
    val wishesFilter: WishesFilter = WishesFilter.All,
    val reorderButtonState: ReorderButtonState = ReorderButtonState.Visible(false),
    val isLoading: Boolean = true
)

sealed class ReorderButtonState {

    object Hidden : ReorderButtonState()

    data class Visible(val isEnabled: Boolean) : ReorderButtonState()
}
