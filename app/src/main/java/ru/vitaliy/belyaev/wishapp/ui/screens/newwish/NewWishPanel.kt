package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.sheetScrim
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing

/**
 * Панель новой хотелки поверх экрана: затемнение и карточка у нижнего края. Это не `ModalBottomSheet`,
 * а обычный слой в окне экрана. Поэтому панель стоит на клавиатуре: отступ клавиатуры с Android 11
 * меняется каждый кадр, и панель едет вместе с ней. И шторка выбора ярлыков открывается поверх,
 * не закрывая панель.
 *
 * Панель — плавающая карточка с отступами со всех сторон. Открытая плюсом, она вырастает из него,
 * как в Todoist: плюс поднимается вместе с клавиатурой, растягивается в карточку и меняет цвет, а
 * содержимое проявляется ближе к концу. При закрытии всё обратно: содержимое тает, карточка
 * сжимается в плюс, пока уходит клавиатура. Открытая иначе, карточка подрастает от нижнего края.
 *
 * Фон карточки рисуется отдельно от содержимого, по границам между плюсом и местом панели.
 * Место панели берётся каждый кадр, поэтому фон едет и за клавиатурой, и за протяжкой ручки.
 */
@Composable
internal fun NewWishPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    origin: NewWishOrigin,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BackHandler(enabled = visible, onBack = onDismiss)
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) {
            origin.isLifted = origin.bounds != null
            // Пружина: быстро вырастает, чуть перелетает место и пружинит назад
            progress.animateTo(1f, spring(dampingRatio = OPEN_DAMPING, stiffness = OPEN_STIFFNESS))
        } else {
            // Поле держит клавиатуру, пока не уйдёт из композиции. Убираем её сразу: панель стоит
            // на клавиатуре и опустится вместе с ней
            focusManager.clearFocus(force = true)
            keyboard?.hide()
            progress.animateTo(0f, tween(MORPH_CLOSE_MILLIS, easing = MorphEasing))
            origin.isLifted = false
        }
    }
    // Фон держится, пока док не вернул плюс: иначе между ними пропадал кадр, и плюс мигал
    val isShown by remember { derivedStateOf { progress.value > 0f || origin.isLifted } }
    if (!visible && !isShown) return

    val geometry = remember { MorphGeometry() }
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value.coerceIn(0f, 1f) }
                .background(sheetScrim())
                .pointerInput(visible) { if (visible) detectTapGestures { onDismiss() } },
        )
        MorphBackground(progress = { progress.value }, origin = origin, geometry = geometry)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                )
                // Карточка стоит на клавиатуре, без неё — над системной навигацией
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom))
                .padding(horizontal = PANEL_GAP)
                .padding(bottom = PANEL_GAP),
        ) {
            PanelSurface(
                onDismiss = onDismiss,
                contentAlpha = { contentAlpha(progress.value) },
                // Содержимое видно только внутри растущего фона, не вылезает за него
                revealRect = {
                    val panel = geometry.panel
                    panel?.let { geometry.rect(progress.value, origin.bounds)?.translate(-it.topLeft) }
                },
                onBounds = { geometry.panel = it },
                content = content,
            )
        }
    }
}

/** Место панели и угол слоя фона, обновляются при каждой раскладке. */
@Stable
private class MorphGeometry {
    var panel: Rect? by mutableStateOf(null)
    var layerOrigin: Offset by mutableStateOf(Offset.Zero)

    /** Границы фона в координатах корня: от плюса или от подросшей снизу карточки к месту панели. */
    fun rect(progress: Float, button: Rect?): Rect? {
        val panel = panel ?: return null
        return lerp(button ?: panel.grownFromBottom(PANEL_START_SCALE), panel, progress)
    }
}

/** Фон карточки: от плюса (цвет, скругление, значок) к месту панели. */
@Composable
private fun MorphBackground(progress: () -> Float, origin: NewWishOrigin, geometry: MorphGeometry) {
    val colors = MaterialTheme.colorScheme
    val isDark = AppThemeExtras.isDark
    val panelColor = if (isDark) colors.surfaceContainerLow else colors.surface
    val outline = colors.outlineVariant
    val buttonColor = colors.primary
    val plus = painterResource(R.drawable.ic_ms_add)
    val plusTint = colors.onPrimary
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { geometry.layerOrigin = it.positionInRoot() }
            .drawBehind {
                val p = progress()
                val button = origin.bounds
                val rect = geometry.rect(p, button)?.translate(-geometry.layerOrigin) ?: return@drawBehind
                val colorFraction = if (button == null) 1f else (p / COLOR_SHIFT_PART).coerceIn(0f, 1f)
                val startCorner = if (button == null) PANEL_CORNER else BUTTON_CORNER
                val corner = CornerRadius(lerp(startCorner, PANEL_CORNER, p).toPx())
                val alpha = if (button == null) p.coerceIn(0f, 1f) else 1f
                drawRoundRect(
                    color = lerp(buttonColor, panelColor, colorFraction),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = corner,
                    alpha = alpha,
                )
                if (isDark) {
                    drawRoundRect(
                        color = outline,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        cornerRadius = corner,
                        style = Stroke(1.dp.toPx()),
                        alpha = alpha * colorFraction,
                    )
                }
                // Значок плюса гаснет в самом начале, пока карточка ещё похожа на кнопку
                val plusAlpha = if (button == null) 0f else 1f - (p / PLUS_FADE_PART).coerceIn(0f, 1f)
                if (plusAlpha > 0f) {
                    val iconSize = PLUS_ICON_SIZE.toPx()
                    translate(rect.center.x - iconSize / 2, rect.center.y - iconSize / 2) {
                        with(plus) {
                            draw(Size(iconSize, iconSize), alpha = plusAlpha, colorFilter = ColorFilter.tint(plusTint))
                        }
                    }
                }
            },
    )
}

/** Содержимое проявляется на последней части пути, когда карточка почти на месте, и первым тает. */
private fun contentAlpha(progress: Float): Float =
    ((progress - CONTENT_START) / (1f - CONTENT_START)).coerceIn(0f, 1f)

private fun Rect.grownFromBottom(scale: Float): Rect {
    val w = width * scale
    val h = height * scale
    return Rect(center.x - w / 2, bottom - h, center.x + w / 2, bottom)
}

@Composable
private fun PanelSurface(
    onDismiss: () -> Unit,
    contentAlpha: () -> Float,
    /** Границы фона относительно самой панели. */
    revealRect: () -> Rect?,
    onBounds: (Rect) -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val dismissDistance = with(LocalDensity.current) { DRAG_DISMISS_DISTANCE.toPx() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffset.value.roundToInt()) }
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .drawWithContent {
                val reveal = revealRect()
                if (reveal == null || (reveal.topLeft == Offset.Zero && reveal.size == size)) {
                    drawContent()
                } else {
                    val path = Path().apply { addRoundRect(RoundRect(reveal, CornerRadius(PANEL_CORNER.toPx()))) }
                    clipPath(path) { this@drawWithContent.drawContent() }
                }
            }
            .graphicsLayer { alpha = contentAlpha() }
            // Нажатие по самой панели не должно закрывать её через затемнение
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(horizontal = WishAppSpacing.s4)
            .padding(bottom = WishAppSpacing.s3),
    ) {
        // Ручка: протянуть вниз дальше порога — закрыть, ближе — вернуть на место
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .draggable(
                    state = rememberDraggableState { delta ->
                        scope.launch { dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f)) }
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        if (dragOffset.value > dismissDistance || velocity > DRAG_DISMISS_VELOCITY) {
                            onDismiss()
                        } else {
                            dragOffset.animateTo(0f)
                        }
                    },
                )
                .padding(top = 10.dp, bottom = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
            )
        }
        content()
    }
}

/**
 * Тонкий ползунок справа, пока содержимое не помещается: видно, что текст листается и где ты в нём.
 * Ставится до `verticalScroll`, рисуется в отступе справа от текста.
 */
internal fun Modifier.verticalScrollbar(state: ScrollState, color: Color): Modifier = drawWithContent {
    drawContent()
    val max = state.maxValue
    if (max <= 0 || max == Int.MAX_VALUE) return@drawWithContent
    val viewport = size.height
    val thumb = (viewport * viewport / (viewport + max)).coerceAtLeast(SCROLLBAR_MIN_THUMB.toPx())
    val width = SCROLLBAR_WIDTH.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width + SCROLLBAR_GAP.toPx(), (viewport - thumb) * state.value / max),
        size = Size(width, thumb),
        cornerRadius = CornerRadius(width / 2),
    )
}

/** Перелёт около 4 %: заметный толчок в конце, без болтанки. */
private const val OPEN_DAMPING = 0.92f
private const val OPEN_STIFFNESS = 620f
private const val MORPH_CLOSE_MILLIS = 380

/** Закрытие: карточка быстро сжимается и мягко садится на плюс. */
private val MorphEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private const val CONTENT_START = 0.6f
private const val COLOR_SHIFT_PART = 0.7f
private const val PLUS_FADE_PART = 0.15f
private const val PANEL_START_SCALE = 0.92f
private val PANEL_GAP = 8.dp
private val PANEL_CORNER = 28.dp

/** Скругление плюса в доке. */
private val BUTTON_CORNER = 24.dp
private val PLUS_ICON_SIZE = 30.dp

private const val DRAG_DISMISS_VELOCITY = 1500f
private val DRAG_DISMISS_DISTANCE = 96.dp
private val SCROLLBAR_WIDTH = 3.dp
private val SCROLLBAR_GAP = 6.dp
private val SCROLLBAR_MIN_THUMB = 24.dp
