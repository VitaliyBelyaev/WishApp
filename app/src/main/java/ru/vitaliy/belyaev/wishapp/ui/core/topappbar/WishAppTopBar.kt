package ru.vitaliy.belyaev.wishapp.ui.core.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun WishAppTopBar(
    title: String = "",
    withBackIcon: Boolean = false,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {

    val navIcon: @Composable (() -> Unit)? =
        if (withBackIcon) {
            {
                IconButton(onClick = { onBackPressed?.invoke() }) {
                    Icon(Filled.ArrowBack, contentDescription = "Back")
                }
            }
        } else {
            null
        }
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = navIcon,
        actions = actions,
        backgroundColor = colorResource(R.color.toolbarColor)
    )
}