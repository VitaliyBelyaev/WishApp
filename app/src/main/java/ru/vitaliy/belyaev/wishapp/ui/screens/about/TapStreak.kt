package ru.vitaliy.belyaev.wishapp.ui.screens.about

/**
 * Пасхалка «О приложении»: [TAPS_TO_PLAY] быстрых нажатий на значок или название, каждое не позже
 * [TAP_GAP_MILLIS] после прежнего. Медленнее, и счёт начинается заново.
 */
internal class TapStreak(private val need: Int = TAPS_TO_PLAY, private val gapMillis: Long = TAP_GAP_MILLIS) {
    private var count = 0
    private var last = Long.MIN_VALUE

    /** Сколько нажатий подряд набрано с этим. На [need] счёт сбрасывается: пасхалка сработала. */
    fun tap(nowMillis: Long): Int {
        count = if (last != Long.MIN_VALUE && nowMillis - last <= gapMillis) count + 1 else 1
        last = nowMillis
        val reached = count
        if (count >= need) reset()
        return reached
    }

    fun reset() {
        count = 0
        last = Long.MIN_VALUE
    }
}

internal const val TAPS_TO_PLAY = 7
internal const val TAP_GAP_MILLIS = 450L
