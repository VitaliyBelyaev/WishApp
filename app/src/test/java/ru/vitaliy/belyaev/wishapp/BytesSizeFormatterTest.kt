package ru.vitaliy.belyaev.wishapp

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.utils.BytesSizeFormatter

class BytesSizeFormatterTest {

    private fun en(bytes: Long) = BytesSizeFormatter.format(bytes, Locale.ENGLISH).replace(' ', ' ')
    private fun ru(bytes: Long) = BytesSizeFormatter.format(bytes, Locale("ru")).replace(' ', ' ')

    @Test
    fun formatSmallerThenKiloByte() {
        assertEquals("1 B", en(1))
        assertEquals("75 B", en(75))
        assertEquals("989 B", en(989))
    }

    @Test
    fun formatSmallerThenMegaByte() {
        assertEquals("1.0 KB", en(1000))
        assertEquals("1.4 KB", en(1400))
        assertEquals("988.9 KB", en(988939))
        assertEquals("54.5 KB", en(54473))
    }

    @Test
    fun formatSmallerThenGigaByte() {
        assertEquals("1.0 MB", en(1_000_000))
        assertEquals("1.6 MB", en(1_567_000))
        assertEquals("5.0 MB", en(5_040_000))
        assertEquals("89.1 MB", en(89_127_774))
    }

    @Test
    fun formatGreaterThenGigaByte() {
        assertEquals("1.0 GB", en(1_000_000_000))
        assertEquals("1.6 GB", en(1_567_000_000))
        assertEquals("5.0 GB", en(5_040_000_000))
        assertEquals("89.1 GB", en(89_127_774_000))
    }

    @Test
    fun formatRussianUnitsAndDecimalComma() {
        assertEquals("75 Б", ru(75))
        assertEquals("1,4 КБ", ru(1400))
        assertEquals("2,4 МБ", ru(2_400_000))
        assertEquals("1,6 ГБ", ru(1_567_000_000))
    }

    @Test
    fun formatFrenchUnits() {
        assertEquals("2,4 Mo", BytesSizeFormatter.format(2_400_000, Locale.FRENCH).replace(' ', ' '))
    }

    @Test
    fun separatesNumberAndUnitWithNoBreakSpace() {
        assertEquals("2.4 MB", BytesSizeFormatter.format(2_400_000, Locale.ENGLISH))
    }
}
