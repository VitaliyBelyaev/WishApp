package ru.vitaliy.belyaev.wishapp.utils

import java.util.Optional

fun <T> Optional<T>.toValueOfNull(): T? =
    if (isPresent) {
        get()
    } else {
        null
    }
