package ru.vitaliy.belyaev.wishapp.ui.core.bottombar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@Composable
fun WishAppBottomBar(
    cutoutShape: Shape? = null,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit
) {

    BottomAppBar(
        cutoutShape = cutoutShape,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = if (MaterialTheme.colors.isLight) AppBarDefaults.BottomAppBarElevation else 0.dp
    ) {
        IconButton(onClick = { onMenuClick() }) {
            ThemedIcon(Filled.Menu, contentDescription = "Tags")
        }
        Spacer(Modifier.weight(1f, true))
        IconButton(onClick = { onShareClick() }) {
            ThemedIcon(Filled.Share, contentDescription = "Share")
        }
    }
}