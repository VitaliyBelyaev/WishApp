package ru.vitaliy.belyaev.wishapp.ui.screens.photos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef

/**
 * Вход и выход просмотра. Если фото хотелки на экране ([PhotoHeroLink]), фото вырастает из своего
 * места и сжимается обратно, как в Telegram; на время перехода его рисует [ViewerHeroLayer], пейджер
 * спрятан. Иначе просмотр проявляется и гаснет целиком.
 */
@Stable
internal class ViewerTransition(private val link: PhotoHeroLink?, private val wishId: String) {

    /** 0 — фото на своём месте в хотелке (или просмотр не виден), 1 — на весь экран. */
    private val progress = Animatable(0f)

    /** Переход идёт через место фото на хотелке. */
    private var shared by mutableStateOf(false)

    /** Фото рисует слой перехода, пейджер спрятан. */
    var layerShown by mutableStateOf(false)
        private set

    /** Откуда фото начало сжиматься: на сколько его утащили свайпом. */
    var layerOffset by mutableFloatStateOf(0f)
        private set
    var closing by mutableStateOf(false)
        private set

    /** Вход доигран: только тогда проявляются кнопки. */
    var opened by mutableStateOf(false)
        private set

    /** Где просмотр в координатах корня: место фото хотелки считается от него. */
    var origin by mutableStateOf(Offset.Zero)

    /** Картинка открытого фото из кэша хотелки: по её размеру строится переход. */
    var painter by mutableStateOf<Painter?>(null)

    val progressValue: Float get() = progress.value
    val contentAlpha: Float get() = if (shared) 1f else progress.value
    val backgroundAlpha: Float get() = if (shared) progress.value else 1f

    /** Место фото на хотелке, если переход через него возможен. */
    fun hero(): HeroRects? = link?.heroOf(wishId, origin)?.takeIf { painter?.intrinsicSize?.isUsable() == true }

    val canCollapse: Boolean get() = hero() != null

    suspend fun enter(awaitImage: suspend () -> Unit) {
        awaitImage()
        val heroReady = withTimeoutOrNull(HERO_WAIT_MILLIS) { snapshotFlow { hero() }.first { it != null } } != null
        if (heroReady) {
            shared = true
            layerShown = true
            link?.viewerOpen = true
            progress.animateTo(1f, tween(OPEN_MILLIS, easing = HeroEasing))
            // Фон уже непрозрачный: фото хотелки под ним можно спрятать незаметно
            link?.heroPhotoHidden = true
            layerShown = false
        } else {
            opened = true
            progress.animateTo(1f, tween(FADE_MILLIS, easing = FastOutSlowInEasing))
            link?.viewerOpen = true
            link?.heroPhotoHidden = true
        }
        opened = true
    }

    /** Фото сжимается в своё место на хотелке, откуда бы его ни отпустили, или просмотр гаснет. */
    fun close(scope: CoroutineScope, fromOffset: Float, onClosed: () -> Unit) {
        if (closing) return
        closing = true
        scope.launch {
            if (hero() != null) {
                shared = true
                layerOffset = fromOffset
                layerShown = true
                progress.animateTo(0f, tween(CLOSE_MILLIS, easing = HeroEasing))
            } else {
                shared = false
                link?.viewerOpen = false
                link?.heroPhotoHidden = false
                progress.animateTo(0f, tween(FADE_MILLIS, easing = FastOutSlowInEasing))
            }
            finish(onClosed)
        }
    }

    /** Фото уже улетело и фон погас: убираем сразу. */
    fun closeNow(onClosed: () -> Unit) {
        if (closing) return
        closing = true
        finish(onClosed)
    }

    /** В том же кадре фото хотелки появляется, а слой просмотра исчезает. */
    private fun finish(onClosed: () -> Unit) {
        release()
        onClosed()
    }

    fun release() {
        link?.viewerOpen = false
        link?.heroPhotoHidden = false
        link?.viewedImageId = null
    }
}

@Composable
internal fun rememberViewerTransition(link: PhotoHeroLink?, wishId: String, current: ImageRef?): ViewerTransition {
    val transition = remember(link, wishId) { ViewerTransition(link, wishId) }
    val context = LocalContext.current
    val window = LocalWindowInfo.current.containerSize
    // Та же картинка, что на хотелке: из кэша сразу, пока полная грузится
    val request = remember(current?.id) {
        current?.let { image ->
            ImageRequest.Builder(context)
                .data(image)
                .memoryCacheKey(heroCacheKey(image.id))
                .placeholderMemoryCacheKey(heroCacheKey(image.id))
                .size(window.width.coerceAtLeast(1), window.height.coerceAtLeast(1))
                .build()
        }
    }
    val painter = rememberAsyncImagePainter(request)
    SideEffect { transition.painter = painter }
    DisposableEffect(transition) {
        onDispose { transition.release() }
    }
    return transition
}

/**
 * Фото между его местом на хотелке и просмотром. В начале оно ровно там, где было на хотелке:
 * обрезано по её фото и спрятано под шторкой. В конце — целиком на весь экран, сдвинутое туда,
 * куда его утащили свайпом.
 */
@Composable
internal fun ViewerHeroLayer(transition: ViewerTransition) {
    val painter = transition.painter
    val hero = if (transition.layerShown) transition.hero() else null
    if (painter == null || hero == null) return
    val offsetY = transition.layerOffset
    Canvas(modifier = Modifier.fillMaxSize()) {
        val image = painter.intrinsicSize
        if (!image.isUsable()) return@Canvas
        val p = transition.progressValue
        val screen = Rect(Offset(0f, offsetY), size)
        val rect = lerp(coverRect(image, hero.bounds), fitRect(image, screen), p)
        val clip = lerp(hero.bounds, screen, p)
        // Шторка хотелки закрывает низ фото со скруглёнными углами: пока фото растёт, она уезжает вниз
        val sheet = Path().apply {
            val top = hero.sheetTop + p * size.height
            val corner = CornerRadius(hero.sheetCorner)
            addRoundRect(
                RoundRect(
                    left = hero.bounds.left,
                    top = top,
                    right = hero.bounds.right,
                    bottom = top + size.height * 2,
                    topLeftCornerRadius = corner,
                    topRightCornerRadius = corner,
                ),
            )
        }
        clipRect(clip.left, clip.top, clip.right, clip.bottom) {
            clipPath(sheet, clipOp = ClipOp.Difference) {
                translate(rect.left, rect.top) {
                    with(painter) { draw(rect.size) }
                }
            }
        }
    }
}

/** Фото вырастает из хотелки и сжимается обратно, как в Telegram: быстро и с плавным концом. */
private const val OPEN_MILLIS = 300
private const val CLOSE_MILLIS = 260
private val HeroEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Проявление при входе и угасание при выходе, когда фото некуда сжиматься. */
private const val FADE_MILLIS = 220

/** Сколько ждём место фото на хотелке и картинку из кэша, прежде чем просто проявиться. */
private const val HERO_WAIT_MILLIS = 150L

/** Размер картинки известен: переход можно строить. */
internal fun Size.isUsable() = isSpecified && width > 0f && height > 0f

/** Картинка заполняет [bounds] целиком и обрезается по краям, как `ContentScale.Crop`. */
private fun coverRect(image: Size, bounds: Rect): Rect {
    val scale = max(bounds.width / image.width, bounds.height / image.height)
    return rectAround(bounds.center, image.width * scale, image.height * scale)
}

/** Картинка целиком внутри [bounds], по центру, как `ContentScale.Fit` в просмотре. */
private fun fitRect(image: Size, bounds: Rect): Rect {
    val scale = min(bounds.width / image.width, bounds.height / image.height)
    return rectAround(bounds.center, image.width * scale, image.height * scale)
}

private fun rectAround(center: Offset, width: Float, height: Float) =
    Rect(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2)
