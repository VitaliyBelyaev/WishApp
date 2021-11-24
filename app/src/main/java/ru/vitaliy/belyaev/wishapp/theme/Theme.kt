package ru.vitaliy.belyaev.wishapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
fun WishAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
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