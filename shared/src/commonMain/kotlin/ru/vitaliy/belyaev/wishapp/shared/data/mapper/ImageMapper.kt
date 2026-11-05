package ru.vitaliy.belyaev.wishapp.shared.data.mapper

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef

object ImageMapper {

    fun mapToRef(id: String, wishId: String): ImageRef = ImageRef(id, wishId)
}