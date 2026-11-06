package ru.vitaliy.belyaev.wishapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.vitaliy.belyaev.wishapp.R

/** Unbounded: короткие акцентные заголовки. */
internal val DisplayFontFamily = FontFamily(
    Font(R.font.unbounded_600, FontWeight.SemiBold),
    Font(R.font.unbounded_700, FontWeight.Bold),
)

/** Onest: весь остальной текст. */
internal val SansFontFamily = FontFamily(
    Font(R.font.onest_400, FontWeight.Normal),
    Font(R.font.onest_500, FontWeight.Medium),
    Font(R.font.onest_600, FontWeight.SemiBold),
    Font(R.font.onest_700, FontWeight.Bold),
)

private fun sans(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = SansFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)

private fun display(size: Int, lineHeight: Int, weight: FontWeight, letterSpacingPx: Double = 0.0) = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    // В токенах трекинг в px при кегле 32px, в em он не зависит от масштаба шрифта
    letterSpacing = (letterSpacingPx / size).em,
)

/**
 * Типографика из токенов. Названия токенов и стилей Material 3:
 *
 * | Токен | Material 3 |
 * |---|---|
 * | display-l | headlineLarge |
 * | display-m | headlineSmall |
 * | title-l | titleLarge |
 * | title-m | titleMedium |
 * | body | bodyLarge |
 * | body-s | bodyMedium |
 * | label | labelLarge |
 * | label-s | labelMedium |
 *
 * Остальные стили Material 3 в дизайне не используются, им достаётся Onest с размерами по умолчанию.
 * Для читаемости в коде есть псевдонимы: `MaterialTheme.typography.displayL` и другие.
 */
internal val WishAppTypography: Typography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = DisplayFontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = DisplayFontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = DisplayFontFamily),
        headlineLarge = display(size = 32, lineHeight = 38, weight = FontWeight.Bold, letterSpacingPx = -0.5),
        headlineMedium = base.headlineMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
        headlineSmall = display(size = 22, lineHeight = 28, weight = FontWeight.SemiBold),
        titleLarge = sans(size = 24, lineHeight = 30, weight = FontWeight.SemiBold),
        titleMedium = sans(size = 16, lineHeight = 21, weight = FontWeight.SemiBold),
        titleSmall = sans(size = 14, lineHeight = 18, weight = FontWeight.SemiBold),
        bodyLarge = sans(size = 15, lineHeight = 21, weight = FontWeight.Normal),
        bodyMedium = sans(size = 13, lineHeight = 18, weight = FontWeight.Normal),
        bodySmall = sans(size = 12, lineHeight = 16, weight = FontWeight.Normal),
        labelLarge = sans(size = 14, lineHeight = 18, weight = FontWeight.SemiBold),
        labelMedium = sans(size = 12, lineHeight = 16, weight = FontWeight.Medium),
        labelSmall = sans(size = 11, lineHeight = 16, weight = FontWeight.Medium),
    )
}

val Typography.displayL: TextStyle get() = headlineLarge
val Typography.displayM: TextStyle get() = headlineSmall
val Typography.titleL: TextStyle get() = titleLarge
val Typography.titleM: TextStyle get() = titleMedium
val Typography.body: TextStyle get() = bodyLarge
val Typography.bodyS: TextStyle get() = bodyMedium
val Typography.label: TextStyle get() = labelLarge
val Typography.labelS: TextStyle get() = labelMedium
