package ru.vitaliy.belyaev.wishapp.shared.data.mapper

import ru.vitaliy.belyaev.wishapp.shared.data.database.Image
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef

object ImageMapper {

    fun mapToDomain(image: Image): ImageEntity {
        return with(image) {
            ImageEntity(
                id,
                wishId,
                rawData
            )
        }
    }

    fun mapToRef(id: String, wishId: String): ImageRef = ImageRef(id, wishId)
}