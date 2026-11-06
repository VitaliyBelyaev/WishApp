package ru.vitaliy.belyaev.wishapp.ui.theme

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.MaterialColors
import com.google.android.material.color.utilities.Hct

/**
 * Тона `paper-*` для настройки «Как обои»: каждый тон поворачивается к `primary` обоев,
 * как `MaterialColors.harmonize`, не больше чем на 15°.
 *
 * Поворот считается один раз по светлому тону и светлому `primary` и применяется в обеих темах.
 * Иначе тон, который почти напротив цвета обоев, днём и вечером ушёл бы в разные стороны.
 *
 * `Hct` в библиотеке Material помечен `RestrictTo`, но `MaterialColors.harmonize` построен на нём же,
 * а публичного способа повернуть тёмный тон на тот же угол нет.
 */
internal fun wallpaperExtraColors(lightPrimary: Color, darkTheme: Boolean, surfaceCard: Color): WishAppExtraColors {
    val papers = PaperTone.entries.associateWith { tone ->
        harmonizeToWallpaper(light = tone.light, dark = tone.dark, lightPrimary = lightPrimary, darkTheme = darkTheme)
    }
    return WishAppExtraColors(papers = papers, surfaceCard = surfaceCard)
}

/**
 * «Сбылось» зелёное при любых обоях: это смысл, а не украшение. В динамической схеме `tertiary`
 * берётся из обоев, поэтому возвращаем цвета `done` из токенов, подстроив их под обои так же, как `paper-*`.
 */
internal fun ColorScheme.withHarmonizedDone(lightPrimary: Color, darkTheme: Boolean): ColorScheme {
    fun harmonized(light: Color, dark: Color) =
        harmonizeToWallpaper(light = light, dark = dark, lightPrimary = lightPrimary, darkTheme = darkTheme)
    return copy(
        tertiary = harmonized(TokenColors.Light.done, TokenColors.Dark.done),
        onTertiary = harmonized(TokenColors.Light.onDone, TokenColors.Dark.onDone),
        tertiaryContainer = harmonized(TokenColors.Light.doneContainer, TokenColors.Dark.doneContainer),
        onTertiaryContainer = harmonized(TokenColors.Light.onDoneContainer, TokenColors.Dark.onDoneContainer),
    )
}

@SuppressLint("RestrictedApi")
private fun harmonizeToWallpaper(light: Color, dark: Color, lightPrimary: Color, darkTheme: Boolean): Color {
    val harmonizedLight = MaterialColors.harmonize(light.toArgb(), lightPrimary.toArgb())
    if (!darkTheme) return Color(harmonizedLight)

    val rotation = signedHueDifference(from = Hct.fromInt(light.toArgb()).hue, to = Hct.fromInt(harmonizedLight).hue)
    val darkHct = Hct.fromInt(dark.toArgb())
    val hue = (darkHct.hue + rotation + 360.0) % 360.0
    return Color(Hct.from(hue, darkHct.chroma, darkHct.tone).toInt())
}

/** Разница оттенков со знаком в пределах -180..180. */
internal fun signedHueDifference(from: Double, to: Double): Double {
    val difference = (to - from + 540.0) % 360.0 - 180.0
    return difference
}
