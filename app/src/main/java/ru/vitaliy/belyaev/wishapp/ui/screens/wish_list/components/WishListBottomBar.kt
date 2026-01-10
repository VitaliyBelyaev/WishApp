package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.ReorderButtonState
import ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.entity.WishesFilter

@Composable
fun WishListBottomBar(
    wishes: List<WishEntity>,
    wishesFilter: WishesFilter,
    sortMode: WishSortMode,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSortClick: () -> Unit,
    reorderButtonState: ReorderButtonState,
    onReorderClick: () -> Unit,
    onAddWishClicked: () -> Unit,
) {

    BottomAppBar(
        floatingActionButton = {
            FloatingActionButton(
                content = {
                    ThemedIcon(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add wish",
                    )
                },
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                onClick = onAddWishClicked,
            )
        },
        actions = {
            // Menu button (always visible)
            IconButton(onClick = onMenuClick) {
                ThemedIcon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = "Navigation menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sort button (visible when there are wishes)
            if (wishes.isNotEmpty()) {
                IconButton(onClick = onSortClick) {
                    val tint = if (sortMode !is WishSortMode.Default) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    ThemedIcon(
                        painter = painterResource(R.drawable.ic_sort_24),
                        contentDescription = "Sort wishes",
                        tint = tint
                    )
                }
            }

            // Share button (visible when there are wishes and not in completed filter)
            if (wishes.isNotEmpty() && wishesFilter !is WishesFilter.Completed) {
                IconButton(onClick = onShareClick) {
                    ThemedIcon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = "Share wishes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Spacer to push reorder button to the right (near FAB)
            Spacer(modifier = Modifier.weight(1f))

            // Reorder button (visible when wishes exist and reorderButtonState is Visible)
            if (wishes.isNotEmpty() && reorderButtonState is ReorderButtonState.Visible) {
                val isEnabled = reorderButtonState.isEnabled
                val isDisabledBySort = sortMode !is WishSortMode.Default
                val tint = when {
                    isDisabledBySort -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    isEnabled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                IconButton(
                    onClick = onReorderClick,
                    enabled = !isDisabledBySort
                ) {
                    ThemedIcon(
                        painter = painterResource(R.drawable.ic_reorder),
                        contentDescription = "Reorder wishes",
                        tint = tint
                    )
                }
                // Small gap before FAB
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    )
}
