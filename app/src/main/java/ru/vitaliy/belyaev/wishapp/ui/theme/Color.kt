package ru.vitaliy.belyaev.wishapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Цвета нового вида из токенов дизайн-системы, `tokens.json`. Сопоставление с Material 3
 * указано в токенах: `done` это `tertiary`, `danger` это `error`, `inverse-accent` это `inversePrimary`.
 */
internal object TokenColors {

    object Light {
        val surface = Color(0xFFFFF9F5)
        val surfaceLow = Color(0xFFFBF1EA)
        val surfaceCard = Color(0xFFFFFFFF)
        val surfaceHigh = Color(0xFFF4E6DC)
        val outline = Color(0xFFE3D3C8)
        val outlineStrong = Color(0xFF85736A)
        val ink = Color(0xFF241A15)
        val inkMuted = Color(0xFF66574E)
        val primary = Color(0xFFC2410C)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFFFDCCB)
        val onPrimaryContainer = Color(0xFF5A1D00)
        val done = Color(0xFF1B6B4F)
        val onDone = Color(0xFFFFFFFF)
        val doneContainer = Color(0xFFCFEEDD)
        val onDoneContainer = Color(0xFF0B3B2A)
        val danger = Color(0xFFB3261E)
        val onDanger = Color(0xFFFFFFFF)
        val inverse = Color(0xFF2F2622)
        val onInverse = Color(0xFFFFF3EC)
        val inverseAccent = Color(0xFFFFB595)
    }

    object Dark {
        val surface = Color(0xFF17120F)
        val surfaceLow = Color(0xFF1F1815)
        val surfaceCard = Color(0xFF2A211C)
        val surfaceHigh = Color(0xFF352A24)
        val outline = Color(0xFF4F433C)
        val outlineStrong = Color(0xFFA08E83)
        val ink = Color(0xFFF6ECE5)
        val inkMuted = Color(0xFFC7B6AB)
        val primary = Color(0xFFFFB595)
        val onPrimary = Color(0xFF5A1D00)
        val primaryContainer = Color(0xFF7A2E08)
        val onPrimaryContainer = Color(0xFFFFDCCB)
        val done = Color(0xFF8AD6B3)
        val onDone = Color(0xFF00382A)
        val doneContainer = Color(0xFF125240)
        val onDoneContainer = Color(0xFFCFEEDD)
        val danger = Color(0xFFFFB4AB)
        val onDanger = Color(0xFF690005)
        val inverse = Color(0xFFF6ECE5)
        val onInverse = Color(0xFF2F2622)
        val inverseAccent = Color(0xFFB53B0A)
    }
}

/** Тон карточки: `paper-peach` у карточки без ярлыка, остальные 10 это цвета ярлыков по слотам. */
enum class PaperTone(internal val light: Color, internal val dark: Color) {
    PEACH(Color(0xFFFFE3D3), Color(0xFF4A2C1E)),
    BUTTER(Color(0xFFFBEBB5), Color(0xFF40361A)),
    MINT(Color(0xFFD4EEDD), Color(0xFF1F3A2C)),
    SKY(Color(0xFFD5E8F5), Color(0xFF1E3344)),
    LILAC(Color(0xFFE8DFF5), Color(0xFF342B45)),
    ROSE(Color(0xFFFADDE3), Color(0xFF452A33)),
    LIME(Color(0xFFE6F0B8), Color(0xFF333A18)),
    AQUA(Color(0xFFCDEDEC), Color(0xFF173A3B)),
    IRIS(Color(0xFFD8DEFB), Color(0xFF2A2E50)),
    ORCHID(Color(0xFFF5D8F3), Color(0xFF4A2648)),
    SAND(Color(0xFFEBDFD0), Color(0xFF3B3026)),
    ;

    companion object {
        /** Тона ярлыков без `peach`, по порядку слотов 1–10. */
        val tagTones: List<PaperTone> = entries.filter { it != PEACH }
    }
}

/**
 * Цвета, которых нет в [ColorScheme] или которые там не однозначны. Цвет «Сбылось» лежит в `tertiary`.
 *
 * [surfaceCard] это фон плавающего дока: в светлой теме он белый, а в тёмной светлее фона,
 * поэтому одного `surfaceContainer*` на обе темы для него нет.
 */
@Immutable
data class WishAppExtraColors(
    private val papers: Map<PaperTone, Color>,
    val surfaceCard: Color,
) {
    fun paper(tone: PaperTone): Color = papers.getValue(tone)
}

internal fun tokenLightColorScheme(): ColorScheme = with(TokenColors.Light) {
    lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inverseAccent,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primaryContainer,
        onSecondaryContainer = onPrimaryContainer,
        tertiary = done,
        onTertiary = onDone,
        tertiaryContainer = doneContainer,
        onTertiaryContainer = onDoneContainer,
        background = surface,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceHigh,
        onSurfaceVariant = inkMuted,
        surfaceTint = primary,
        inverseSurface = inverse,
        inverseOnSurface = onInverse,
        error = danger,
        onError = onDanger,
        outline = outlineStrong,
        outlineVariant = outline,
        surfaceBright = surface,
        surfaceDim = surfaceHigh,
        surfaceContainerLowest = surfaceCard,
        surfaceContainerLow = surfaceLow,
        // Меню Material 3 берут surfaceContainer, а по токенам они на surface-card
        surfaceContainer = surfaceCard,
        surfaceContainerHigh = surfaceHigh,
        surfaceContainerHighest = surfaceHigh,
    )
}

internal fun tokenDarkColorScheme(): ColorScheme = with(TokenColors.Dark) {
    darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inverseAccent,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primaryContainer,
        onSecondaryContainer = onPrimaryContainer,
        tertiary = done,
        onTertiary = onDone,
        tertiaryContainer = doneContainer,
        onTertiaryContainer = onDoneContainer,
        background = surface,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceHigh,
        onSurfaceVariant = inkMuted,
        surfaceTint = primary,
        inverseSurface = inverse,
        inverseOnSurface = onInverse,
        error = danger,
        onError = onDanger,
        outline = outlineStrong,
        outlineVariant = outline,
        surfaceBright = surfaceHigh,
        surfaceDim = surface,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surfaceLow,
        // Меню Material 3 берут surfaceContainer, а по токенам они на surface-card
        surfaceContainer = surfaceCard,
        surfaceContainerHigh = surfaceHigh,
        surfaceContainerHighest = surfaceHigh,
    )
}

internal fun tokenExtraColors(darkTheme: Boolean): WishAppExtraColors {
    return WishAppExtraColors(
        papers = PaperTone.entries.associateWith { if (darkTheme) it.dark else it.light },
        surfaceCard = if (darkTheme) TokenColors.Dark.surfaceCard else TokenColors.Light.surfaceCard,
    )
}
