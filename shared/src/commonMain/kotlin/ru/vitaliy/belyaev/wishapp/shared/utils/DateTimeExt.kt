package ru.vitaliy.belyaev.wishapp.shared.utils

import kotlinx.datetime.Clock

fun Clock.System.nowEpochMillis(): Long {
    return now().toEpochMilliseconds()
}