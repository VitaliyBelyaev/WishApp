package ru.vitaliy.belyaev.wishapp.ui.core.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import ru.vitaliy.belyaev.wishapp.R
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
                    ThemedIcon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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