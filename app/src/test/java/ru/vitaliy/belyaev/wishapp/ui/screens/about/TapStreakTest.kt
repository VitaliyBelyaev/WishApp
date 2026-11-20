package ru.vitaliy.belyaev.wishapp.ui.screens.about

import org.junit.Assert.assertEquals
import org.junit.Test

/** Счёт быстрых нажатий для пасхалки «О приложении». */
class TapStreakTest {

    @Test
    fun sevenFastTapsPlayAndStartOver() {
        val streak = TapStreak()
        val taps = (0 until TAPS_TO_PLAY + 1).map { streak.tap(it * TAP_GAP_MILLIS) }
        assertEquals((1..TAPS_TO_PLAY).toList() + 1, taps)
    }

    @Test
    fun slowTapStartsTheCountAgain() {
        val streak = TapStreak()
        repeat(5) { streak.tap(it * 100L) }
        assertEquals(1, streak.tap(400L + TAP_GAP_MILLIS + 1))
        assertEquals(2, streak.tap(400L + TAP_GAP_MILLIS + 200))
    }
}
