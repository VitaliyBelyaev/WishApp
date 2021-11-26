package ru.vitaliy.belyaev.wishapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.vitaliy.belyaev.wishapp.entity.Theme

private val DarkColorPalette = darkColors(
    primary = PrimaryNight,
    primaryVariant = PrimaryVariant,
    secondary = SecondaryNight,
    secondaryVariant = SecondaryVariant,
    surface = PrimaryNight
)

private val LightColorPalette = lightColors(
    primary = Primary,
    primaryVariant = Color.White,
    secondary = Secondary,
    secondaryVariant = SecondaryVariant,
    onPrimary = Color.Black
)

@Composable
fun WishAppTheme(selectedTheme: Theme, content: @Composable () -> Unit) {
    val isDark = when (selectedTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.DARK -> true
        Theme.LIGHT -> false
    }
    val colors = if (isDark) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}