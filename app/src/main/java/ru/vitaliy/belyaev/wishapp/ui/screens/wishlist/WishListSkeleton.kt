package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing

/**
 * Заготовки карточек, пока хотелки грузятся. Заставка ждёт список недолго (см. `AppActivity`),
 * и на медленном телефоне вместо пустого экрана видна сетка, которая мягко пульсирует.
 * Когда хотелки пришли, заготовки гаснут поверх настоящих карточек.
 *
 * Вид списка (карточки или строки) до загрузки неизвестен, поэтому заготовки всегда карточками.
 */
@Composable
internal fun ListSkeleton(isVisible: Boolean, contentPadding: PaddingValues) {
    AnimatedVisibility(
        visible = isVisible,
        enter = EnterTransition.None,
        exit = fadeOut(tween(FADE_OUT_MS)),
    ) {
        val pulse = rememberInfiniteTransition(label = "skeleton")
        val alpha = pulse.animateFloat(
            initialValue = 1f,
            targetValue = PULSE_MIN_ALPHA,
            animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = LinearEasing), RepeatMode.Reverse),
            label = "skeletonAlpha",
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .padding(contentPadding)
                .graphicsLayer { this.alpha = alpha.value }
                .clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        ) {
            SkeletonColumn(heights = LeftHeights, modifier = Modifier.weight(1f))
            SkeletonColumn(heights = RightHeights, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SkeletonColumn(heights: List<Dp>, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surfaceContainerHigh
    val shape = MaterialTheme.shapes.large
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
        heights.forEach { height ->
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(color, shape),
            )
        }
    }
}

// Разной высоты, как карточки с фото и без: сетка читается как список, а не как плитка
private val LeftHeights = listOf(250.dp, 130.dp, 210.dp, 150.dp, 240.dp)
private val RightHeights = listOf(160.dp, 240.dp, 120.dp, 230.dp, 170.dp)

private const val PULSE_MS = 800
private const val PULSE_MIN_ALPHA = 0.45f
private const val FADE_OUT_MS = 220
