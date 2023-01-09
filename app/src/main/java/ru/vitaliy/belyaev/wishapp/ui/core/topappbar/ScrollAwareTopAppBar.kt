package ru.vitaliy.belyaev.wishapp.ui.core.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.theme.material3.isLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollAwareTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    isScrollInInitialState: (() -> Boolean)? = null,
    topAppBarScrollBehavior: TopAppBarScrollBehavior? = null
) {

    val isScrolledFromIdle: Boolean = isScrollInInitialState?.invoke() == false

//    val systemUiController = rememberSystemUiController()
    val isLightTheme = MaterialTheme.colorScheme.isLight()
//    val statusBarColor = if (!isLightTheme && isScrolledFromIdle) {
//        MaterialTheme.colorScheme.surface
//    } else {
//        MaterialTheme.colorScheme.background
//    }
//
//    LaunchedEffect(key1 = isScrolledFromIdle) {
//        systemUiController.setStatusBarColor(
//            color = statusBarColor,
//            darkIcons = isLightTheme
//        )
//    }

//    val backgroundColor: Color = if (!isLightTheme && !isScrolledFromIdle) {
//        MaterialTheme.colorScheme.background
//    } else {
//        MaterialTheme.colorScheme.surface
//    }
//    val contentColor = contentColorFor(backgroundColor)
    val scrollColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

    val colors = TopAppBarDefaults
        .smallTopAppBarColors(

        )


    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = topAppBarScrollBehavior
    )
}