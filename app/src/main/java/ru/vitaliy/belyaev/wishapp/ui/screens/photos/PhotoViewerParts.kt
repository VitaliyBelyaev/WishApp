package ru.vitaliy.belyaev.wishapp.ui.screens.photos

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.ui.screens.wish.FloatingButtonSize
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.label

/**
 * Одно фото: зум до 3× щипком и двойным нажатием. Пока фото не увеличено, его можно утащить вверх
 * или вниз, как в Telegram: оно едет строго по вертикали, не уменьшается и не поворачивается, а фон
 * гаснет. Отпустили дальше порога или бросили — просмотр закрывается, иначе
 * возвращается на место. Увеличенное фото тем же жестом двигается.
 */
@Composable
internal fun ViewerPage(
    image: ImageRef,
    isActive: Boolean,
    onTap: () -> Unit,
    onDragFraction: (Float) -> Unit,
    flyAway: Boolean,
    onDismiss: (offset: Float) -> Unit,
) {
    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = MAX_ZOOM, preventOverOrUnderZoom = true),
    )
    val zoomed = (zoomableState.zoomFraction ?: 0f) > 0f
    val context = LocalContext.current
    val request = remember(image.id) {
        // Пока полное фото грузится, видна уже загруженная картинка с хотелки: без чёрной вспышки
        ImageRequest.Builder(context)
            .data(image)
            .memoryCacheKey(image.id)
            .placeholderMemoryCacheKey(heroCacheKey(image.id))
            .crossfade(true)
            .build()
    }
    val scope = rememberCoroutineScope()
    val swipe = remember { SwipeAway() }
    var height by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val speeds = remember(density) {
        with(density) { SwipeSpeeds(fling = DISMISS_VELOCITY.toPx(), minAway = MIN_AWAY_SPEED.toPx()) }
    }
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentFlyAway by rememberUpdatedState(flyAway)
    val interactions = remember { MutableInteractionSource() }
    val settle: (Float) -> Unit = { velocity ->
        swipe.settle(scope, velocity, speeds.withHeight(height), flyAway = currentFlyAway) { currentOnDismiss(it) }
    }
    // Палец увели в зум вторым пальцем или жест сорвался: фото не должно застыть на полпути
    LaunchedEffect(interactions) {
        interactions.interactions.collect { if (it is DragInteraction.Cancel) settle(0f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { height = it.height }
            .draggable(
                state = rememberDraggableState { delta -> swipe.offset += delta },
                orientation = Orientation.Vertical,
                enabled = isActive && !zoomed,
                interactionSource = interactions,
                onDragStarted = { swipe.stop() },
                // Отпускание живёт в своей корутине: её не отменит ни новый кадр жеста, ни сам draggable
                onDragStopped = { velocity -> settle(velocity) },
            )
            .graphicsLayer { translationY = swipe.offset },
    ) {
        ZoomableAsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            state = rememberZoomableImageState(zoomableState),
            onClick = { onTap() },
        )
    }

    val currentOnDragFraction by rememberUpdatedState(onDragFraction)
    if (isActive) {
        LaunchedEffect(swipe) {
            snapshotFlow { if (height == 0) 0f else abs(swipe.offset) / height }.collect { currentOnDragFraction(it) }
        }
    } else {
        // Ушедшее фото возвращается к исходному размеру и на место
        LaunchedEffect(Unit) {
            swipe.stop()
            swipe.offset = 0f
            zoomableState.resetZoom(withAnimation = false)
        }
    }
}

private class SwipeSpeeds(val fling: Float, val minAway: Float, val height: Int = 0) {
    fun withHeight(height: Int) = SwipeSpeeds(fling, minAway, height)
}

/**
 * Вертикальный сдвиг фото при свайпе. Палец двигает его напрямую, без корутин: раньше каждый кадр
 * жеста запускал `snapTo`, и опоздавший кадр отменял анимацию после отпускания — фото застывало.
 */
private class SwipeAway {
    var offset by mutableFloatStateOf(0f)
    private var job: Job? = null

    fun stop() {
        job?.cancel()
        job = null
    }

    /**
     * Отпустили дальше порога или бросили — просмотр закрывается: фото сжимается в своё место на хотелке,
     * а если его там нет ([flyAway]), уходит за край со скоростью пальца, но не резче [AWAY_MIN_MILLIS].
     * Иначе фото пружинит на место.
     */
    fun settle(
        scope: CoroutineScope,
        velocity: Float,
        speeds: SwipeSpeeds,
        flyAway: Boolean,
        onDismiss: (offset: Float) -> Unit,
    ) {
        stop()
        if (speeds.height == 0) {
            offset = 0f
            return
        }
        val thrown = abs(velocity) > speeds.fling
        job = scope.launch {
            if ((thrown || abs(offset) > speeds.height * DISMISS_THRESHOLD) && !flyAway) {
                // Фото сжимается в своё место на хотелке, это доигрывает экран
                onDismiss(offset)
            } else if (thrown || abs(offset) > speeds.height * DISMISS_THRESHOLD) {
                // Улетает туда, куда бросили, а без броска — куда тащили
                val target = sign(if (thrown) velocity else offset) * speeds.height
                val speed = max(abs(velocity), speeds.minAway)
                val millis = (abs(target - offset) / speed * MILLIS_IN_SECOND).toInt()
                    .coerceIn(AWAY_MIN_MILLIS, AWAY_MAX_MILLIS)
                val easing = if (thrown) LinearEasing else FastOutSlowInEasing
                animate(offset, target, animationSpec = tween(millis, easing = easing)) { value, _ -> offset = value }
                onDismiss(offset)
            } else {
                animate(offset, 0f, velocity, spring(stiffness = Spring.StiffnessMediumLow)) { value, _ ->
                    offset = value
                }
            }
        }
    }
}

/**
 * Кнопки поверх фото: «Назад» и счётчик сверху, действия снизу по центру. На окне шире своей высоты
 * действия справа колонкой, круглые, подпись «Сделать главным» остаётся только для TalkBack.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ViewerButtons(
    position: Int,
    count: Int,
    isMain: Boolean,
    wide: Boolean,
    onBack: () -> Unit,
    onMakeMain: () -> Unit,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
) {
    val insets = WindowInsets.systemBarsIgnoringVisibility.union(WindowInsets.displayCutout)
    Box(modifier = Modifier.fillMaxSize()) {
        TopScrim(insets)
        Row(
            modifier = Modifier
                .windowInsetsPadding(insets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
        ) {
            ViewerCircleButton(
                icon = R.drawable.ic_ms_arrow_back,
                description = stringResource(R.string.cd_back),
                onClick = onBack,
                modifier = Modifier.testTag(PhotoViewerTestTags.BACK),
            )
            // Счётчик — текст, не капсула: капсула значит «можно нажать». При одном фото его нет
            if (count > 1) {
                Text(
                    text = stringResource(R.string.photo_counter, position + 1, count),
                    style = MaterialTheme.typography.titleMedium.copy(shadow = CounterShadow),
                    color = Color.White,
                    modifier = Modifier.testTag(PhotoViewerTestTags.COUNTER),
                )
            }
        }
        val actions = @Composable {
            ViewerMainButton(isMain = isMain, showLabel = !wide, onClick = onMakeMain)
            ViewerCircleButton(
                icon = R.drawable.ic_ms_add_photo_alternate,
                description = stringResource(R.string.photo_add),
                onClick = onAdd,
                modifier = Modifier.testTag(PhotoViewerTestTags.ADD),
            )
            ViewerCircleButton(
                icon = R.drawable.ic_ms_delete,
                description = stringResource(R.string.photo_delete),
                onClick = onDelete,
                modifier = Modifier.testTag(PhotoViewerTestTags.DELETE),
            )
        }
        if (wide) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .windowInsetsPadding(insets.only(WindowInsetsSides.End))
                    .padding(end = WishAppSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { actions() }
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(insets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(bottom = WishAppSpacing.s6),
                horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) { actions() }
        }
    }
}

/**
 * Затемнение сверху, как в Google Фото: одно целое со статус-баром, плотное у края и плавно
 * гаснущее ниже кнопок, без заметной границы. Счётчик и значки читаются и на светлом фото.
 */
@Composable
private fun TopScrim(insets: WindowInsets) {
    val density = LocalDensity.current
    val top = with(density) { insets.getTop(this).toDp() }
    val total = top + TopScrimBelowBar
    val brush = remember(top) {
        val plateau = (top / total * SCRIM_PLATEAU).coerceIn(0f, 1f)
        val colors = List(SCRIM_STEPS + 1) { i ->
            val y = i / SCRIM_STEPS.toFloat()
            val u = ((y - plateau) / (1f - plateau)).coerceIn(0f, 1f)
            Color.Black.copy(alpha = SCRIM_ALPHA * (1f - u * u * (3f - 2f * u)))
        }
        Brush.verticalGradient(colors)
    }
    Box(modifier = Modifier.fillMaxWidth().height(total).background(brush))
}

/** Круглая кнопка просмотра: чёрная полупрозрачная подложка без обводки, на чёрном фоне её не видно. */
@Composable
private fun ViewerCircleButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(FloatingButtonSize)
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = ViewerButtonColor,
        contentColor = tint,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(icon), contentDescription = null)
        }
    }
}

/** «Сделать главным» капсулой с подписью; у главного фото звезда залита и подпись «Главное фото». */
@Composable
private fun ViewerMainButton(isMain: Boolean, showLabel: Boolean, onClick: () -> Unit) {
    val icon = if (isMain) R.drawable.ic_ms_star_filled else R.drawable.ic_ms_star
    val label = stringResource(if (isMain) R.string.photo_main else R.string.photo_make_main)
    val tint = if (isMain) MainPhotoColor else Color.White
    val modifier = Modifier.testTag(PhotoViewerTestTags.MAKE_MAIN)
    if (!showLabel) {
        ViewerCircleButton(icon = icon, description = label, onClick = onClick, modifier = modifier, tint = tint)
        return
    }
    Surface(
        onClick = onClick,
        modifier = modifier.height(FloatingButtonSize),
        shape = CircleShape,
        color = ViewerButtonColor,
        contentColor = tint,
    ) {
        Row(
            modifier = Modifier.padding(start = WishAppSpacing.s3, end = WishAppSpacing.s4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        ) {
            Icon(painter = painterResource(icon), contentDescription = null)
            Text(text = label, style = MaterialTheme.typography.label)
        }
    }
}

private const val MAX_ZOOM = 3f

/** Доля высоты, после которой отпущенное фото закрывает просмотр. Резкий бросок закрывает и раньше. */
private const val DISMISS_THRESHOLD = 0.15f

/** Скорость броска, которая закрывает просмотр с любого места. */
private val DISMISS_VELOCITY = 1_000.dp

/** Без броска фото уходит не медленнее этого. */
private val MIN_AWAY_SPEED = 1_600.dp

/** Сколько фото улетает за край после того, как его отпустили: быстрый бросок не мгновенный, медленный не тянется. */
private const val AWAY_MIN_MILLIS = 240
private const val AWAY_MAX_MILLIS = 380
private const val MILLIS_IN_SECOND = 1_000

/** Подложка кнопок `rgba(0,0,0,.55)`. */
private val ViewerButtonColor = Color.Black.copy(alpha = 0.55f)

/** Звезда главного фото, тёплый жёлтый на чёрном, как в макете. */
private val MainPhotoColor = Color(0xFFFFD27A)

/** Затемнение заходит ниже статус-бара: строка кнопок 56 dp и плавный хвост. */
private val TopScrimBelowBar = 104.dp
private const val SCRIM_ALPHA = 0.55f
private const val SCRIM_PLATEAU = 0.6f
private const val SCRIM_STEPS = 16
private val CounterShadow = Shadow(color = Color.Black.copy(alpha = 0.7f), blurRadius = 6f)
