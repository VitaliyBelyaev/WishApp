package ru.vitaliy.belyaev.wishapp.ui.theme

import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.domain.model.Theme

private val LocalExtraColors = staticCompositionLocalOf { tokenExtraColors(darkTheme = false) }
private val LocalIsDarkTheme = staticCompositionLocalOf { false }

/** «Как обои» доступно только с Android 12, где есть цвета из обоев. */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isWallpaperColorsAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Тема нового вида. Прежний вид живёт со своей темой в `ui/old/theme`.
 *
 * По умолчанию цвета «Хурма» из токенов на всех версиях Android. С [useWallpaperColors] на Android 12+
 * интерфейс берёт цвета из обоев, а тона карточек `paper-*` подстраиваются под `primary` обоев.
 */
@Composable
fun AppTheme(
    selectedTheme: Theme,
    useWallpaperColors: Boolean,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (selectedTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.DARK -> true
        Theme.LIGHT -> false
    }
    val context = LocalContext.current
    val wallpaper = useWallpaperColors && isWallpaperColorsAvailable()

    val colorScheme: ColorScheme
    val extraColors: WishAppExtraColors
    if (wallpaper) {
        val lightScheme = remember(context) { dynamicLightColorScheme(context) }
        colorScheme = remember(context, darkTheme) {
            val scheme = if (darkTheme) dynamicDarkColorScheme(context) else lightScheme
            scheme.withHarmonizedDone(lightPrimary = lightScheme.primary, darkTheme = darkTheme)
        }
        extraColors = remember(lightScheme, darkTheme) {
            wallpaperExtraColors(
                lightPrimary = lightScheme.primary,
                darkTheme = darkTheme,
                surfaceCard = if (darkTheme) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainerLowest,
            )
        }
    } else {
        colorScheme = if (darkTheme) tokenDarkColorScheme() else tokenLightColorScheme()
        extraColors = tokenExtraColors(darkTheme)
    }

    // Иконки системных панелей под тему приложения, а не системы: тема выбирается в настройках.
    // Навигационная панель прозрачная, контент нового вида рисуется под ней
    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(activity, darkTheme) {
        val style = if (darkTheme) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(scrim = AndroidColor.TRANSPARENT, darkScrim = AndroidColor.TRANSPARENT)
        }
        activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    CompositionLocalProvider(
        LocalExtraColors provides extraColors,
        LocalIsDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WishAppTypography,
            shapes = WishAppShapes,
            content = content,
        )
    }
}

object AppThemeExtras {

    val colors: WishAppExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtraColors.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkTheme.current
}

/** Тень из токенов `shadow-*`. */
fun Modifier.tokenShadow(shape: Shape, shadow: WishAppShadow): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = shadow.blur,
        color = shadow.color,
        offset = DpOffset(0.dp, shadow.offsetY),
    ),
)
