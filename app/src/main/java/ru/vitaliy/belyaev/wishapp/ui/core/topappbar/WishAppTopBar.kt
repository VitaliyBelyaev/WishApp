package ru.vitaliy.belyaev.wishapp.ui.core.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

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
                    ThemedIcon(Filled.ArrowBack, contentDescription = "Back")
                }
            }
        } else {
            null
        }

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = navIcon,
        actions = actions,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = if (MaterialTheme.colors.isLight) AppBarDefaults.TopAppBarElevation else 0.dp
    )
}