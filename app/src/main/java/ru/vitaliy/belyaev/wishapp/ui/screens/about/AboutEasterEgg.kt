package ru.vitaliy.belyaev.wishapp.ui.screens.about

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Значок выпрыгивает со своего места: искры на взлёте, два прыжка вправо за край экрана, из-за левого края
 * ещё два прыжка обратно и приземление на место. На каждом приземлении пыль из кружков и звёздочек.
 * Пока прыгает, настоящий значок спрятан ([onPlaying]).
 */
@Composable
internal fun AboutEasterEgg(
    play: Int,
    iconBounds: Rect,
    onPlaying: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
) {
    if (play == 0 || iconBounds.isEmpty) return
    val dp = LocalDensity.current.density
    val time = remember(play) { Animatable(0f) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var width by remember { mutableFloatStateOf(0f) }
    var running by remember(play) { mutableStateOf(true) }
    val currentOnPlaying by rememberUpdatedState(onPlaying)
    val colors = EggColors.current()
    val start = remember(play) { iconBounds }
    LaunchedEffect(play) {
        currentOnPlaying(true)
        try {
            time.animateTo(TOTAL_MILLIS, tween(TOTAL_MILLIS.toInt(), easing = LinearEasing))
        } finally {
            running = false
            currentOnPlaying(false)
        }
    }
    if (!running) return

    val center = start.center - origin
    val plan = remember(play, width, center) { HopPlan(center, width, start.width, dp) }
    val particles = remember(play, width, center) { plan.particles(Random(play), colors.size) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(AboutTestTags.EASTER_EGG)
            .onGloballyPositioned {
                origin = it.boundsInRoot().topLeft
                width = it.size.width.toFloat()
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = time.value
            particles.forEach { drawParticle(it, t, colors, dp) }
        }
        Box(
            modifier = Modifier.graphicsLayer {
                val pose = plan.pose(time.value)
                translationX = pose.center.x - start.width / 2
                translationY = pose.center.y - start.height / 2
                scaleX = pose.scaleX
                scaleY = pose.scaleY
                rotationZ = pose.rotation
                transformOrigin = if (pose.rotation == 0f) BottomCenter else TransformOrigin.Center
            },
        ) { icon() }
    }
}

/** Где значок и как он сплющен в момент [t]. */
private class Pose(val center: Offset, val scaleX: Float = 1f, val scaleY: Float = 1f, val rotation: Float = 0f)

/** Отрезок прыжка: откуда, куда, высота дуги и полный оборот в воздухе. */
private class Hop(val from: Offset, val to: Offset, val heightDp: Float, val spin: Boolean, val startMs: Float)

/** Прыжки и пыль, посчитанные от места значка и ширины экрана; [dp] — один dp в пикселях. */
private class HopPlan(private val home: Offset, width: Float, size: Float, private val dp: Float) {
    private val hops: List<Hop>
    private val landings: List<Pair<Float, Offset>>

    init {
        val rightMid = Offset(home.x + (width - home.x) * RIGHT_LANDING, home.y)
        val offRight = Offset(width + size, home.y)
        val offLeft = Offset(-size, home.y)
        val leftMid = Offset(home.x * LEFT_LANDING, home.y)
        hops = listOf(
            Hop(home, rightMid, HIGH_HOP_DP, spin = false, startMs = WIND_UP_MS),
            Hop(rightMid, offRight, MID_HOP_DP, spin = true, startMs = WIND_UP_MS + HOP_MS + SQUASH_MS),
            Hop(offLeft, leftMid, MID_HOP_DP, spin = false, startMs = WIND_UP_MS + 2 * HOP_MS + SQUASH_MS),
            Hop(leftMid, home, LOW_HOP_DP, spin = false, startMs = WIND_UP_MS + 3 * HOP_MS + 2 * SQUASH_MS),
        )
        val bottom = size / 2
        // Приземления на экране: после первого, третьего и последнего прыжка, второй уводит за край
        landings =
            hops.filterIndexed { index, _ -> index != 1 }.map { it.startMs + HOP_MS to it.to + Offset(0f, bottom) }
    }

    fun pose(t: Float): Pose {
        val air = hops.firstOrNull { (t - it.startMs) / HOP_MS in 0f..1f }
        val ground = hops.zipWithNext().firstOrNull { (a, b) -> t > a.startMs + HOP_MS && t < b.startMs }
        val landed = t - (hops.last().startMs + HOP_MS)
        return when {
            t < WIND_UP_MS -> squashed(home, sin(PI.toFloat() * t / WIND_UP_MS) * WIND_UP_SQUASH)
            air != null -> inAir(air, (t - air.startMs) / HOP_MS)
            ground != null -> {
                val (a, b) = ground
                val u = (t - a.startMs - HOP_MS) / (b.startMs - a.startMs - HOP_MS)
                squashed(a.to, sin(PI.toFloat() * u) * LANDING_SQUASH)
            }
            landed >= 0f -> {
                val u = (landed / FINAL_SQUASH_MS).coerceIn(0f, 1f)
                // Сплющился, подпрыгнул чуть выше обычного и встал: затухающая волна
                squashed(home, sin(2 * PI.toFloat() * u) * (1 - u) * FINAL_SQUASH)
            }
            else -> Pose(home)
        }
    }

    /** Дуга-парабола, в воздухе значок чуть вытянут. */
    private fun inAir(hop: Hop, p: Float): Pose {
        val x = hop.from.x + (hop.to.x - hop.from.x) * p
        val y = hop.from.y - hop.heightDp * dp * PARABOLA * p * (1 - p)
        val stretch = 1f + AIR_STRETCH * sin(PI.toFloat() * p)
        return Pose(
            center = Offset(x, y),
            scaleX = 2f - stretch,
            scaleY = stretch,
            rotation = if (hop.spin) FULL_TURN * p else 0f,
        )
    }

    private fun squashed(at: Offset, amount: Float) = Pose(at, scaleX = 1f + amount * WIDEN, scaleY = 1f - amount)

    /** Искры на взлёте разлетаются во все стороны, пыль на приземлениях — в стороны и чуть вверх. */
    fun particles(random: Random, colorCount: Int): List<Particle> {
        val burst = List(BURST_COUNT) {
            val angle = random.nextFloat() * 2 * PI.toFloat()
            val speed = (BURST_SPEED_MIN + random.nextFloat() * BURST_SPEED_RANGE) * dp
            Particle(
                origin = home,
                velocity = Offset(cos(angle) * speed, sin(angle) * speed - BURST_LIFT * dp),
                startMs = WIND_UP_MS,
                lifeMs = BURST_LIFE_MS + random.nextFloat() * BURST_LIFE_MS / 2,
                look = Look(

                    radius = (BURST_RADIUS_MIN + random.nextFloat() * BURST_RADIUS_RANGE) * dp,

                    color = random.nextInt(colorCount),

                    isStar = random.nextFloat() < STAR_SHARE,

                    spin = random.nextFloat() * SPIN_RANGE - SPIN_RANGE / 2,

                ),
            )
        }
        val dust = landings.flatMapIndexed { index, (at, point) ->
            val count = if (index == landings.lastIndex) FINAL_DUST_COUNT else DUST_COUNT
            List(count) { i ->
                val side = if (i % 2 == 0) 1f else -1f
                val speed = (DUST_SPEED_MIN + random.nextFloat() * DUST_SPEED_RANGE) * dp
                Particle(
                    origin = point,
                    velocity = Offset(side * speed, -(DUST_LIFT_MIN + random.nextFloat() * DUST_LIFT_RANGE) * dp),
                    startMs = at,
                    lifeMs = DUST_LIFE_MS,
                    look = Look(

                        radius = (DUST_RADIUS_MIN + random.nextFloat() * DUST_RADIUS_RANGE) * dp,

                        color = random.nextInt(colorCount),

                        isStar = index == landings.lastIndex && random.nextFloat() < STAR_SHARE,

                        spin = random.nextFloat() * SPIN_RANGE - SPIN_RANGE / 2,

                    ),
                )
            }
        }
        return burst + dust
    }
}

private class Particle(
    val origin: Offset,
    val velocity: Offset,
    val startMs: Float,
    val lifeMs: Float,
    val look: Look,
)

/** Как выглядит искра: размер, номер цвета, кружок или звёздочка и скорость вращения звёздочки, °/с. */
private class Look(val radius: Float, val color: Int, val isStar: Boolean, val spin: Float)

private fun DrawScope.drawParticle(particle: Particle, t: Float, colors: List<Color>, dp: Float) {
    val age = t - particle.startMs
    if (age < 0f || age > particle.lifeMs) return
    val s = age / MILLIS_IN_SECOND
    val position = particle.origin + particle.velocity * s + Offset(0f, GRAVITY_DP * dp * s * s / 2)
    val fade = 1f - (age / particle.lifeMs).let { it * it }
    val radius = particle.look.radius * (1f - age / particle.lifeMs / 2)
    val color = colors[particle.look.color].copy(alpha = fade)
    if (particle.look.isStar) {
        rotate(degrees = particle.look.spin * s, pivot = position) { drawPath(starPath(position, radius * 2), color) }
    } else {
        drawCircle(color, radius, position)
    }
}

/** Четырёхлучевая звёздочка: загаданное сбылось. */
private fun starPath(center: Offset, radius: Float) = Path().apply {
    val inner = radius * STAR_INNER
    repeat(STAR_POINTS * 2) { i ->
        val r = if (i % 2 == 0) radius else inner
        val angle = PI.toFloat() * i / STAR_POINTS - PI.toFloat() / 2
        val point = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
        if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
    }
    close()
}

/** Цвета искр: хурма, зелёный «Сбылось», светлый акцент и золото. */
private object EggColors {
    @Composable
    fun current(): List<Color> {
        val scheme = MaterialTheme.colorScheme
        return remember(scheme) { listOf(scheme.primary, scheme.tertiary, scheme.inversePrimary, Gold) }
    }
}

private val Gold = Color(0xFFF5B83D)
private val BottomCenter = TransformOrigin(0.5f, 1f)

private const val WIND_UP_MS = 140f
private const val HOP_MS = 440f
private const val SQUASH_MS = 100f
private const val FINAL_SQUASH_MS = 320f
private const val TOTAL_MILLIS = WIND_UP_MS + 4 * HOP_MS + 2 * SQUASH_MS + 900f
private const val MILLIS_IN_SECOND = 1000f

private const val RIGHT_LANDING = 0.55f
private const val LEFT_LANDING = 0.45f
private const val HIGH_HOP_DP = 140f
private const val MID_HOP_DP = 110f
private const val LOW_HOP_DP = 70f
private const val FULL_TURN = 360f

/** Парабола 4p(1-p): в середине прыжка ровно его высота. */
private const val PARABOLA = 4f
private const val AIR_STRETCH = 0.1f
private const val WIND_UP_SQUASH = 0.18f
private const val LANDING_SQUASH = 0.16f
private const val FINAL_SQUASH = 0.2f
private const val WIDEN = 0.6f

private const val GRAVITY_DP = 1400f
private const val BURST_COUNT = 26
private const val BURST_SPEED_MIN = 160f
private const val BURST_SPEED_RANGE = 260f
private const val BURST_LIFT = 180f
private const val BURST_LIFE_MS = 900f
private const val BURST_RADIUS_MIN = 2.5f
private const val BURST_RADIUS_RANGE = 3.5f
private const val DUST_COUNT = 6
private const val FINAL_DUST_COUNT = 12
private const val DUST_SPEED_MIN = 60f
private const val DUST_SPEED_RANGE = 120f
private const val DUST_LIFT_MIN = 80f
private const val DUST_LIFT_RANGE = 140f
private const val DUST_LIFE_MS = 600f
private const val DUST_RADIUS_MIN = 2f
private const val DUST_RADIUS_RANGE = 2.5f
private const val STAR_SHARE = 0.4f
private const val SPIN_RANGE = 540f
private const val STAR_POINTS = 4
private const val STAR_INNER = 0.38f
