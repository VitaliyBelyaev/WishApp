package ru.vitaliy.belyaev.wishapp.shared.domain.entity

/**
 * Картинка без байтов. Байты лежат в базе и загружаются по [id] только когда нужны:
 * в памяти нельзя держать их для всех желаний сразу.
 */
data class ImageRef(
    val id: String,
    val wishId: String,
)
