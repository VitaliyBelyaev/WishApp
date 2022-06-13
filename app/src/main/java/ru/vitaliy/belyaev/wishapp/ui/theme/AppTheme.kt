package ru.vitaliy.belyaev.wishapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.ui.theme.color.ThemeColors
import ru.vitaliy.belyaev.wishapp.ui.theme.color.WishAppDayColors
import ru.vitaliy.belyaev.wishapp.ui.theme.color.WishAppNightColors

val LocalWishAppTheme: ProvidableCompositionLocal<WishAppTheme> = compositionLocalOf { DayWishAppTheme }

val localTheme: WishAppTheme
    @Composable get() = LocalWishAppTheme.current

interface WishAppTheme {
    val colors: ThemeColors
}

object DayWishAppTheme : WishAppTheme {
    override val colors: ThemeColors = WishAppDayColors()
}

object NightWishAppTheme : WishAppTheme {
    override val colors: ThemeColors = WishAppNightColors()
}

@Composable
fun WishAppTheme(selectedTheme: Theme, content: @Composable () -> Unit) {
    val isDark = when (selectedTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.DARK -> true
        Theme.LIGHT -> false
    }

    val wishAppTheme: WishAppTheme = if (isDark) {
        NightWishAppTheme
    } else {
        DayWishAppTheme
    }

    val colors = Colors(
        primary = wishAppTheme.colors.primaryColor,
        primaryVariant = wishAppTheme.colors.primaryColor,
        secondary = wishAppTheme.colors.primaryColor,
        secondaryVariant = wishAppTheme.colors.primaryColor,
        background = wishAppTheme.colors.backgroundColor,
        surface = wishAppTheme.colors.surfaceColor,
        error = wishAppTheme.colors.errorColor,
        onPrimary = wishAppTheme.colors.onPrimaryColor,
        onSecondary = wishAppTheme.colors.onSecondaryColor,
        onBackground = wishAppTheme.colors.onSurfaceColor,
        onSurface = wishAppTheme.colors.onSurfaceColor,
        onError = wishAppTheme.colors.onErrorColor,
        isLight = !isDark
    )

    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(
        color = wishAppTheme.colors.statusBarColor,
        darkIcons = !isDark
    )
    systemUiController.setNavigationBarColor(
        color = wishAppTheme.colors.navigationBarColor,
        darkIcons = !isDark
    )

    CompositionLocalProvider(
        LocalWishAppTheme provides wishAppTheme
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}