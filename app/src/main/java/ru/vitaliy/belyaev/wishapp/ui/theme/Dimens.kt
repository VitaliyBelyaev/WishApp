package ru.vitaliy.belyaev.wishapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Радиусы из токенов. */
object WishAppRadius {
    /** Фото внутри карточки, поля. */
    val sm: Dp = 12.dp

    /** Строки-кнопки, меню. */
    val md: Dp = 16.dp

    /** Карточка хотелки, кнопка добавления. */
    val lg: Dp = 24.dp

    /** Шторки, скругление контента под фото. */
    val xl: Dp = 32.dp
}

/** Отступы из токенов. */
object WishAppSpacing {
    /** Иконка и подпись. */
    val s1: Dp = 4.dp

    /** Зазор между карточками, чипами. */
    val s2: Dp = 8.dp

    /** Внутренний отступ карточки, боковое поле сетки. */
    val s3: Dp = 12.dp

    /** Боковое поле экрана, отступы шторок. */
    val s4: Dp = 16.dp

    /** Расстояние между секциями. */
    val s6: Dp = 24.dp

    /** Отступы пустых состояний. */
    val s8: Dp = 32.dp
}

/**
 * Формы Material 3 из радиусов: `small` поля и фото, `medium` меню и строки, `large` карточки,
 * `extraLarge` шторки. `radius-full` это `CircleShape`.
 */
internal val WishAppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(WishAppRadius.sm),
    medium = RoundedCornerShape(WishAppRadius.md),
    large = RoundedCornerShape(WishAppRadius.lg),
    extraLarge = RoundedCornerShape(WishAppRadius.xl),
)

/** Тень из токенов: в Compose это смещённая размытая тень цвета [color]. */
data class WishAppShadow(val offsetY: Dp, val blur: Dp, val color: Color)

object WishAppElevation {

    /** Плавающий док и снэкбар. */
    fun dock(darkTheme: Boolean) = WishAppShadow(
        offsetY = 6.dp,
        blur = 20.dp,
        color = if (darkTheme) Color.Black.copy(alpha = 0.5f) else Color(0xFF3C1E0A).copy(alpha = 0.16f),
    )

    /** Карточка, поднятая при перетаскивании. */
    fun lift(darkTheme: Boolean) = WishAppShadow(
        offsetY = 14.dp,
        blur = 30.dp,
        color = if (darkTheme) Color.Black.copy(alpha = 0.6f) else Color(0xFF3C1E0A).copy(alpha = 0.28f),
    )
}
