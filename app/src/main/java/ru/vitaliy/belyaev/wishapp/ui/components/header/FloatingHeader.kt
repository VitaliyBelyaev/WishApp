package ru.vitaliy.belyaev.wishapp.ui.components.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.components.dock.floatingDarkOutline
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

// Общие части плавающих шапок нового вида: главная, режим выбора, поиск, хотелка, вложенные экраны

/**
 * Отступы шапки от системных панелей: сверху и по бокам. Низ не берём: `safeDrawing` включает
 * клавиатуру, и фон шапки поиска вырастал на её высоту, закрывая пол-экрана списка.
 */
internal val HeaderInsets: WindowInsets
    @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

/** Высота строки шапки под статус-баром. На столько же сдвинуто начало списка. */
internal val HeaderRowHeight: Dp = 56.dp

/** Кнопки шапки крупнее стандартных 44 dp, как в тулбаре системных экранов Samsung. */
internal val HeaderButtonSize = 48.dp

/** Высота шапки вместе со статус-баром: отступ сверху для списка. */
@Composable
internal fun headerHeight(): Dp {
    val density = LocalDensity.current
    val statusBar = with(density) { WindowInsets.safeDrawing.getTop(this).toDp() }
    return statusBar + HeaderRowHeight
}

/**
 * Затемнение у статус-бара, как у системных экранов Samsung: самое плотное у верхнего края,
 * гаснет уже внутри статус-бара и кончается на [ScrimTail] ниже него. Строку кнопок оно почти
 * не задевает, под капсулой контент читается. Затухание по smoothstep, без заметной границы.
 */
@Composable
internal fun HeaderScrim(progress: () -> Float) {
    val surface = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val statusBar = with(density) { WindowInsets.safeDrawing.getTop(this).toDp() }
    val total = statusBar + ScrimTail
    val plateau = (statusBar * SCRIM_PLATEAU / total).coerceIn(0f, 1f)
    val brush = remember(surface, plateau) {
        val stops = buildList {
            add(0f to surface.copy(alpha = SCRIM_TOP_ALPHA))
            for (i in 0..SCRIM_STEPS) {
                val u = i / SCRIM_STEPS.toFloat()
                val fade = 1f - u * u * (3f - 2f * u)
                add(plateau + (1f - plateau) * u to surface.copy(alpha = SCRIM_TOP_ALPHA * fade))
            }
        }
        Brush.verticalGradient(*stops.toTypedArray())
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(total)
            .graphicsLayer { alpha = (progress() * 3f).coerceIn(0f, 1f) }
            .background(brush),
    )
}

/** Насколько затемнение заходит ниже статус-бара. */
private val ScrimTail = 20.dp

/** Какая доля статус-бара сверху закрыта плотно, дальше начинается затухание. */
private const val SCRIM_PLATEAU = 0.4f
private const val SCRIM_TOP_ALPHA = 0.94f
private const val SCRIM_STEPS = 8

/**
 * Подложка плавающей капсулы шапки: `surface-card` с тенью дока, проявляется с прокруткой.
 * Ставится первой в `Box` с кнопками и занимает его размер.
 */
@Composable
internal fun BoxScope.CapsuleBackdrop(progress: () -> Float) {
    val capsule = CircleShape
    Box(
        modifier = Modifier
            .matchParentSize()
            .outset(CapsuleShadowRoom)
            .graphicsLayer { alpha = (progress() * 3f).coerceIn(0f, 1f) }
            .padding(CapsuleShadowRoom)
            .tokenShadow(capsule, WishAppElevation.dock(AppThemeExtras.isDark))
            .background(AppThemeExtras.colors.surfaceCard, capsule)
            .then(floatingDarkOutline()?.let { Modifier.border(it, capsule) } ?: Modifier),
    )
}

/** Запас под тень капсулы `shadow-dock`: размытие 20 dp и сдвиг 6 dp. */
private val CapsuleShadowRoom = 32.dp

/** Растягивает элемент на [extra] во все стороны за его место в раскладке. */
private fun Modifier.outset(extra: Dp): Modifier = layout { measurable, constraints ->
    val extraPx = extra.roundToPx()
    val placeable = measurable.measure(
        Constraints.fixed(constraints.maxWidth + extraPx * 2, constraints.maxHeight + extraPx * 2),
    )
    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(-extraPx, -extraPx) }
}
