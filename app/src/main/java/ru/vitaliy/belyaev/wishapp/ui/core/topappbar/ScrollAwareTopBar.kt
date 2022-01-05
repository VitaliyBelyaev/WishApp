package ru.vitaliy.belyaev.wishapp.ui.core.topappbar

import android.app.Activity
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.utils.setStatusBarColor

@Composable
fun ScrollAwareTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    lazyListState: LazyListState? = null
) {

    val context = LocalContext.current
    val surfaceColorInt = remember {
        context.resources.getColor(ru.vitaliy.belyaev.wishapp.R.color.surfaceColor, context.theme)
    }
    val backgroundColorInt = remember {
        context.resources.getColor(ru.vitaliy.belyaev.wishapp.R.color.backgroundColor, context.theme)
    }

    val firstVisibleIndex = lazyListState?.firstVisibleItemIndex ?: 0
    val firstVisibleItemScrollOffset = lazyListState?.firstVisibleItemScrollOffset ?: 0
    val isScrolledFromIdle: Boolean = if (firstVisibleIndex == 0) {
        firstVisibleItemScrollOffset > 5
    } else {
        true
    }

    val statusBarColorInt = if (MaterialTheme.colors.isLight) {
        surfaceColorInt
    } else {
        if (isScrolledFromIdle) {
            surfaceColorInt
        } else {
            backgroundColorInt
        }
    }
    (context as? Activity)?.setStatusBarColor(statusBarColorInt)

    val toolbarElevation: Dp = if (MaterialTheme.colors.isLight) {
        if (isScrolledFromIdle) {
            AppBarDefaults.TopAppBarElevation
        } else {
            0.dp
        }
    } else {
        0.dp
    }

    val backgroundColor: Color = if (MaterialTheme.colors.isLight) {
        Color(surfaceColorInt)
    } else {
        if (isScrolledFromIdle) {
            Color(surfaceColorInt)
        } else {
            Color(backgroundColorInt)
        }
    }
    val contentColor = contentColorFor(backgroundColor)

    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = toolbarElevation
    )
}