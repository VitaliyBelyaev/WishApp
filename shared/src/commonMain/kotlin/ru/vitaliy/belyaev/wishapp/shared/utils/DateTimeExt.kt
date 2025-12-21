package ru.vitaliy.belyaev.wishapp.shared.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Clock.System.nowEpochMillis(): Long {
    return now().toEpochMilliseconds()
}