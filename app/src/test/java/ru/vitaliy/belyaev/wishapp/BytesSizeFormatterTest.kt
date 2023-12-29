package ru.vitaliy.belyaev.wishapp

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.utils.BytesSizeFormatter

class BytesSizeFormatterTest {

    @Test
    fun formatSmallerThenKiloByte() {
        assertEquals("1 B", BytesSizeFormatter.format(1))
        assertEquals("75 B", BytesSizeFormatter.format(75))
        assertEquals("989 B", BytesSizeFormatter.format(989))
    }

    @Test
    fun formatSmallerThenMegaByte() {
        assertEquals("1.0 KB", BytesSizeFormatter.format(1000))
        assertEquals("1.4 KB", BytesSizeFormatter.format(1400))
        assertEquals("988.9 KB", BytesSizeFormatter.format(988939))
        assertEquals("54.5 KB", BytesSizeFormatter.format(54473))
    }

    @Test
    fun formatSmallerThenGigaByte() {
        assertEquals("1.0 MB", BytesSizeFormatter.format(1_000_000))
        assertEquals("1.6 MB", BytesSizeFormatter.format(1_567_000))
        assertEquals("5.0 MB", BytesSizeFormatter.format(5_040_000))
        assertEquals("89.1 MB", BytesSizeFormatter.format(89_127_774))
    }

    @Test
    fun formatGreaterThenGigaByte() {
        assertEquals("1.0 GB", BytesSizeFormatter.format(1_000_000_000))
        assertEquals("1.6 GB", BytesSizeFormatter.format(1_567_000_000))
        assertEquals("5.0 GB", BytesSizeFormatter.format(5_040_000_000))
        assertEquals("89.1 GB", BytesSizeFormatter.format(89_127_774_000))
    }
}