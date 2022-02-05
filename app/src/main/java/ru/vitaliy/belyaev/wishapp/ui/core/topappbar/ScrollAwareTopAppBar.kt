package ru.vitaliy.belyaev.wishapp.ui.core.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ScrollAwareTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    isScrollInInitialState: (() -> Boolean)? = null,
) {

    val isScrolledFromIdle: Boolean = isScrollInInitialState?.invoke() == false

    val systemUiController = rememberSystemUiController()
    val isLightTheme = MaterialTheme.colors.isLight
    val statusBarColor = if (!isLightTheme && isScrolledFromIdle) {
        MaterialTheme.colors.surface
    } else {
        MaterialTheme.colors.background
    }

    LaunchedEffect(key1 = isScrolledFromIdle) {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = isLightTheme
        )
    }

    val toolbarElevation: Dp = if (isLightTheme) {
        if (isScrolledFromIdle) {
            AppBarDefaults.TopAppBarElevation
        } else {
            0.dp
        }
    } else {
        0.dp
    }

    val backgroundColor: Color = if (!isLightTheme && !isScrolledFromIdle) {
        MaterialTheme.colors.background
    } else {
        MaterialTheme.colors.surface
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