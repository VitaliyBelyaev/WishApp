package ru.vitaliy.belyaev.wishapp.ui.components.reveal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlin.math.hypot
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Смена темы и цвета кругом от нажатой кнопки. Экран в старой теме снимается в картинку, тема
 * меняется сразу, а поверх лежит снимок с дырой-кругом. Круг растёт от кнопки, пока не откроет
 * весь экран в новой теме.
 *
 * Корень нового вида рисует содержимое через [themeReveal] и отдаёт состояние через
 * [LocalThemeReveal]. Системные панели в снимок не попадают: их значки меняются, когда круг
 * доходит до верха экрана ([isCoveringTop]).
 * С выключенными в системе анимациями Compose заканчивает круг мгновенно.
 */
@Stable
class ThemeRevealState internal constructor(
    private val scope: CoroutineScope,
    internal val layer: GraphicsLayer,
) {
    internal var snapshot by mutableStateOf<ImageBitmap?>(null)
        private set
    internal var center by mutableStateOf(Offset.Zero)
        private set
    internal val radius = Animatable(0f)

    /** Что видно глазом: фон и акцент. Меняется, когда новая тема дошла до экрана. */
    internal var appearance by mutableStateOf<Any?>(null)

    /** Снимок старой темы ещё закрывает верх экрана: значки статус-бара пока не трогаем. */
    val isCoveringTop: Boolean by derivedStateOf {
        snapshot != null && radius.value < center.y
    }

    private var job: Job? = null

    /**
     * @param origin откуда расходится круг, в координатах корня.
     * @param changesLook поменяется ли вид. Например, «Авто» вместо «Тёмной» при тёмной системе
     * ничего не меняет: тогда без анимации.
     * @param apply сама смена настройки.
     */
    fun change(origin: Offset, changesLook: Boolean, apply: () -> Unit) {
        job?.cancel()
        if (!changesLook || layer.size == IntSize.Zero) {
            apply()
            return
        }
        val before = appearance
        job = scope.launch {
            var applied = false
            try {
                val bitmap = layer.toImageBitmap()
                center = origin
                radius.snapTo(0f)
                snapshot = bitmap
                apply()
                applied = true
                // Тема читается из DataStore, до экрана доходит не сразу. Круг пускаем, когда дошла
                withTimeoutOrNull(APPLY_TIMEOUT_MS) { snapshotFlow { appearance }.first { it != before } }
                radius.animateTo(farthestCorner(origin), tween(DURATION_MS, easing = FastOutSlowInEasing))
            } finally {
                snapshot = null
                if (!applied) apply()
            }
        }
    }

    private fun farthestCorner(origin: Offset): Float {
        val dx = max(origin.x, layer.size.width - origin.x)
        val dy = max(origin.y, layer.size.height - origin.y)
        return hypot(dx, dy)
    }

    private companion object {
        const val DURATION_MS = 550
        const val APPLY_TIMEOUT_MS = 1000L
    }
}

@Composable
fun rememberThemeRevealState(): ThemeRevealState {
    val scope = rememberCoroutineScope()
    val layer = rememberGraphicsLayer()
    return remember(scope, layer) { ThemeRevealState(scope, layer) }
}

/** Без состояния (вне нового вида) просто меняет настройку. */
fun ThemeRevealState?.changeOrApply(origin: Offset, changesLook: Boolean, apply: () -> Unit) {
    if (this == null) apply() else change(origin, changesLook, apply)
}

/** `null` вне нового вида: там тема меняется без анимации. */
val LocalThemeReveal = staticCompositionLocalOf<ThemeRevealState?> { null }

/** Рисует содержимое через слой, который можно снять, и снимок старой темы поверх. */
fun Modifier.themeReveal(state: ThemeRevealState): Modifier = drawWithContent {
    state.layer.record { this@drawWithContent.drawContent() }
    drawLayer(state.layer)
    val snapshot = state.snapshot ?: return@drawWithContent
    val hole = Path().apply { addOval(Rect(center = state.center, radius = state.radius.value)) }
    clipPath(hole, clipOp = ClipOp.Difference) { drawImage(snapshot) }
}

/** Центр кнопки, от которой пойдёт круг. Запоминается при раскладке, без перерисовки. */
class RevealOrigin {
    var center: Offset = Offset.Zero
        private set

    internal fun update(value: Offset) {
        center = value
    }
}

fun Modifier.revealOrigin(origin: RevealOrigin): Modifier = onGloballyPositioned {
    origin.update(it.boundsInRoot().center)
}
