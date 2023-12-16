package ru.vitaliy.belyaev.wishapp.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object BytesSizeFormatter {

    private const val KILO_BYTE = 1000.0
    private const val MEGA_BYTE = 1000.0 * KILO_BYTE
    private const val GIGA_BYTE = 1000.0 * MEGA_BYTE

    private val decimalFormat = DecimalFormat(
        "#.#",
        DecimalFormatSymbols(Locale.ENGLISH)
    ).apply {
        roundingMode = RoundingMode.HALF_EVEN
    }

    fun format(bytes: Long): String {
        return when {
            bytes < KILO_BYTE -> "$bytes B"
            bytes < MEGA_BYTE -> "${bytesDividedByAndRounded(bytes, KILO_BYTE)} KB"
            bytes < GIGA_BYTE -> "${bytesDividedByAndRounded(bytes, MEGA_BYTE)} MB"
            else -> "${bytesDividedByAndRounded(bytes, GIGA_BYTE)} GB"
        }
    }

    private fun bytesDividedByAndRounded(
        bytes: Long,
        divider: Double
    ): Double {
        val rawNumber = bytes / divider
        return decimalFormat.format(rawNumber).toDouble()
    }
}