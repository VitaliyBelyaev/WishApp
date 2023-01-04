package ru.vitaliy.belyaev.wishapp.ui.core.bottombar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.ReorderButtonState
import ru.vitaliy.belyaev.wishapp.ui.screens.main.entity.WishesFilter
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme

@Composable
fun WishAppBottomBar(
    wishesFilter: WishesFilter,
    cutoutShape: Shape? = null,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    reorderButtonState: ReorderButtonState,
    onReorderClick: () -> Unit,
) {

    BottomAppBar(
        cutoutShape = cutoutShape,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = if (MaterialTheme.colors.isLight) AppBarDefaults.BottomAppBarElevation else 0.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {
        IconButton(onClick = { onMenuClick() }) {
            ThemedIcon(Filled.Menu, contentDescription = "Tags")
        }
        Spacer(Modifier.weight(1f, true))

        when (reorderButtonState) {
            is ReorderButtonState.Visible -> {
                IconButton(onClick = { onReorderClick() }) {
                    val tint = if (reorderButtonState.isEnabled) {
                        localTheme.colors.invertedIconColor
                    } else {
                        localTheme.colors.iconPrimaryColor
                    }
                    ThemedIcon(
                        painter = painterResource(R.drawable.img_reorder_filled),
                        contentDescription = "Reorder",
                        tint = tint
                    )
                }
            }
            is ReorderButtonState.Hidden -> {
            }
        }


        when (wishesFilter) {
            is WishesFilter.ByTag,
            is WishesFilter.All -> {
                IconButton(onClick = { onShareClick() }) {
                    ThemedIcon(Filled.Share, contentDescription = "Share")
                }
            }
            is WishesFilter.Completed -> {
                // do nothing
            }
        }
    }
}