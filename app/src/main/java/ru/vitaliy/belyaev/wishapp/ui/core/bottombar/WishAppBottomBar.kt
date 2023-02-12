package ru.vitaliy.belyaev.wishapp.ui.core.bottombar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
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
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.ReorderButtonState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishesFilter

@Composable
fun WishAppBottomBar(
    wishes: List<WishEntity>,
    wishesFilter: WishesFilter,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    reorderButtonState: ReorderButtonState,
    onReorderClick: () -> Unit,
    onAddWishClicked: () -> Unit,
) {

    BottomAppBar(
        floatingActionButton = {
            FloatingActionButton(
                content = {
                    ThemedIcon(
                        modifier = Modifier.size(26.dp),
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add wish",
                    )
                },
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                onClick = onAddWishClicked,
            )
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                ThemedIcon(
                    imageVector = Filled.Menu,
                    contentDescription = "Navigation menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (wishes.isNotEmpty() && reorderButtonState is ReorderButtonState.Visible) {
                IconButton(onClick = { onReorderClick() }) {
                    val tint = if (reorderButtonState.isEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    ThemedIcon(
                        painter = painterResource(R.drawable.img_reorder_filled),
                        contentDescription = "Reorder wishes",
                        tint = tint
                    )
                }
            }
            if (wishes.isNotEmpty() && wishesFilter !is WishesFilter.Completed) {
                IconButton(onClick = onShareClick) {
                    ThemedIcon(
                        imageVector = Filled.Share,
                        contentDescription = "Share wishes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}
