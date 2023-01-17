package ru.vitaliy.belyaev.wishapp.ui.core.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishAppTopBar(
    title: String = "",
    withBackIcon: Boolean = false,
    onBackPressed: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {

    val navIcon: @Composable (() -> Unit) =
        if (withBackIcon) {
            {
                IconButton(onClick = { onBackPressed?.invoke() }) {
                    ThemedIcon(Filled.ArrowBack, contentDescription = "Back")
                }
            }
        } else {
            {}
        }

    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = navIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
    )
}