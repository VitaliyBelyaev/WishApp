package ru.vitaliy.belyaev.wishapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.utils.isAndroidVersionSOrAbove

@Composable
fun WishAppTheme(selectedTheme: Theme, content: @Composable () -> Unit) {
    val isDark = when (selectedTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.DARK -> true
        Theme.LIGHT -> false
    }

    val isDynamicColorAvailable = isAndroidVersionSOrAbove
    val colorScheme = when {
        isDynamicColorAvailable && isDark -> dynamicDarkColorScheme(LocalContext.current)
        isDynamicColorAvailable && !isDark -> dynamicLightColorScheme(LocalContext.current)
        isDark -> DarkColors
        else -> LightColors
    }

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = !isDark
    )
    systemUiController.setNavigationBarColor(
        color = CommonColors.navBarColor(colorScheme),
        darkIcons = !isDark
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}