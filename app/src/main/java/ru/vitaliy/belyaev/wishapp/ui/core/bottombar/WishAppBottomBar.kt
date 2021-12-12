package ru.vitaliy.belyaev.wishapp.ui.core.bottombar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@Composable
fun WishAppBottomBar(
    cutoutShape: Shape? = null,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit
) {

    BottomAppBar(
        cutoutShape = cutoutShape,
    ) {
        IconButton(onClick = { onMenuClick() }) {
            Icon(Filled.Menu, contentDescription = "Tags", tint = MaterialTheme.colors.onSurface)
        }
        Spacer(Modifier.weight(1f, true))
        IconButton(onClick = { onShareClick() }) {
            Icon(Filled.Share, contentDescription = "Share", tint = MaterialTheme.colors.onSurface)
        }
    }
}