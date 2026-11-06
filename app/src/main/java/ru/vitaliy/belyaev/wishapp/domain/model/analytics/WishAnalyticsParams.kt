package ru.vitaliy.belyaev.wishapp.domain.model.analytics

import java.util.concurrent.TimeUnit
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

/** Полных дней с создания хотелки, не меньше нуля. */
fun WishEntity.ageDays(nowMillis: Long): Int =
    TimeUnit.MILLISECONDS.toDays((nowMillis - createdTimestamp).coerceAtLeast(0)).toInt()
