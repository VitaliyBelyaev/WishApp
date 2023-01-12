package ru.vitaliy.belyaev.wishapp.ui.core.bottombar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.ReorderButtonState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishesFilter

@Composable
fun WishAppBottomBar(
    wishes: List<WishWithTags>,
    wishesFilter: WishesFilter,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    reorderButtonState: ReorderButtonState,
    onReorderClick: () -> Unit,
) {

    BottomAppBar {
        IconButton(onClick = { onMenuClick() }) {
            ThemedIcon(Filled.Menu, contentDescription = "Tags")
        }
        Spacer(Modifier.weight(1f, true))

        if (wishes.isNotEmpty() && reorderButtonState is ReorderButtonState.Visible) {
            IconButton(onClick = { onReorderClick() }) {
                val tint = if (reorderButtonState.isEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    LocalContentColor.current
                }
                ThemedIcon(
                    painter = painterResource(R.drawable.img_reorder_filled),
                    contentDescription = "Reorder",
                    tint = tint
                )
            }
        }

        if (wishes.isNotEmpty() && wishesFilter !is WishesFilter.Completed) {
            IconButton(onClick = { onShareClick() }) {
                ThemedIcon(Filled.Share, contentDescription = "Share")
            }
        }
    }
}
