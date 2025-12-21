package ru.vitaliy.belyaev.wishapp.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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

    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(key1 = isDark) {
        if (isDark) {
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(DefaultDarkScrim)
            )
        } else {
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(scrim = DefaultLightScrim, darkScrim = DefaultDarkScrim)
            )
        }

    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

private val DefaultLightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DefaultDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)