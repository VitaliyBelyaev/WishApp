package ru.vitaliy.belyaev.wishapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.Theme

@Composable
fun WishAppTheme(selectedTheme: Theme, content: @Composable () -> Unit) {
    val isDark = when (selectedTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.DARK -> true
        Theme.LIGHT -> false
    }

    val colors = Colors(
        primary = colorResource(R.color.primaryColor),
        primaryVariant = colorResource(R.color.primaryColor),
        secondary = colorResource(R.color.primaryColor),
        secondaryVariant = colorResource(R.color.primaryColor),
        background = colorResource(R.color.backgroundColor),
        surface = colorResource(R.color.surfaceColor),
        error = colorResource(R.color.errorColor),
        onPrimary = colorResource(R.color.onPrimaryColor),
        onSecondary = colorResource(R.color.onSecondaryColor),
        onBackground = colorResource(R.color.onSurfaceColor),
        onSurface = colorResource(R.color.onSurfaceColor),
        onError = colorResource(R.color.onErrorColor),
        isLight = !isDark
    )

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}