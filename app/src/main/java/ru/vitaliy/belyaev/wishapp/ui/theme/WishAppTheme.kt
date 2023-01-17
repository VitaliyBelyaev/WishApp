package ru.vitaliy.belyaev.wishapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.vitaliy.belyaev.wishapp.entity.Theme

@Composable
fun WishAppTheme(selectedTheme: Theme, content: @Composable () -> Unit) {
    val isDark = when (selectedTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.DARK -> true
        Theme.LIGHT -> false
    }

    val isDynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        isDynamicColorAvailable && isDark -> dynamicDarkColorScheme(LocalContext.current)
        isDynamicColorAvailable && !isDark -> dynamicLightColorScheme(LocalContext.current)
        isDark -> DarkColors
        else -> LightColors
    }

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = colorScheme.background,
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