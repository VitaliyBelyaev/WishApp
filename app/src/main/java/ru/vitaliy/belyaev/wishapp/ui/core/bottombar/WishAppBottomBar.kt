package ru.vitaliy.belyaev.wishapp.ui.core.bottombar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@Composable
fun WishAppBottomBar(
    cutoutShape: Shape? = null
) {

    BottomAppBar(cutoutShape = cutoutShape) {
        Spacer(Modifier.weight(1f, true))
        IconButton(onClick = { /* doSomething() */ }) {
            Icon(Filled.Share, contentDescription = "Share")
        }
    }
}