package ru.vitaliy.belyaev.wishapp.domain.model

enum class Theme {
    SYSTEM, DARK, LIGHT
}

private const val INT_SYSTEM = 0
private const val INT_DARK = 1
private const val INT_LIGHT = 2

fun Int?.toTheme(): Theme =
    when (this) {
        INT_SYSTEM -> Theme.SYSTEM
        INT_DARK -> Theme.DARK
        INT_LIGHT -> Theme.LIGHT
        else -> Theme.SYSTEM
    }

fun Theme.toInt(): Int =
    when (this) {
        Theme.SYSTEM -> INT_SYSTEM
        Theme.DARK -> INT_DARK
        Theme.LIGHT -> INT_LIGHT
    }
