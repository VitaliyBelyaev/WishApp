package ru.vitaliy.belyaev.wishapp.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Размер файла по правилам языка: «2,4 МБ» на русском, «2.4 MB» на английском, «2,4 Mo»
 * на французском. Число и единица разделены неразрывным пробелом.
 */
object BytesSizeFormatter {

    private const val KILO_BYTE = 1000.0
    private const val MEGA_BYTE = 1000.0 * KILO_BYTE
    private const val GIGA_BYTE = 1000.0 * MEGA_BYTE
    private const val NO_BREAK_SPACE = ' '

    private data class Units(val bytes: String, val kilo: String, val mega: String, val giga: String)

    private val LATIN_UNITS = Units("B", "KB", "MB", "GB")
    private val CYRILLIC_UNITS = Units("Б", "КБ", "МБ", "ГБ")
    private val FRENCH_UNITS = Units("o", "Ko", "Mo", "Go")

    fun format(bytes: Long, locale: Locale = Locale.getDefault()): String {
        val units = unitsFor(locale)
        return when {
            bytes < KILO_BYTE -> "$bytes$NO_BREAK_SPACE${units.bytes}"
            bytes < MEGA_BYTE -> "${divideAndFormat(bytes, KILO_BYTE, locale)}$NO_BREAK_SPACE${units.kilo}"
            bytes < GIGA_BYTE -> "${divideAndFormat(bytes, MEGA_BYTE, locale)}$NO_BREAK_SPACE${units.mega}"
            else -> "${divideAndFormat(bytes, GIGA_BYTE, locale)}$NO_BREAK_SPACE${units.giga}"
        }
    }

    private fun unitsFor(locale: Locale): Units {
        return when (locale.language) {
            "ru", "uk", "be" -> CYRILLIC_UNITS
            "fr" -> FRENCH_UNITS
            else -> LATIN_UNITS
        }
    }

    private fun divideAndFormat(bytes: Long, divider: Double, locale: Locale): String {
        // DecimalFormat не потокобезопасен, поэтому создаём его на каждый вызов
        val decimalFormat = DecimalFormat("0.0", DecimalFormatSymbols.getInstance(locale)).apply {
            roundingMode = RoundingMode.HALF_EVEN
        }
        return decimalFormat.format(bytes / divider)
    }
}
