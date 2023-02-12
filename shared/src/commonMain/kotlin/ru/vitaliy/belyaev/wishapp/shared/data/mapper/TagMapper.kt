package ru.vitaliy.belyaev.wishapp.shared.data.mapper

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

object TagMapper {

    fun mapToDomain(tagId: String, title: String): TagEntity {
        return TagEntity(
            id = tagId,
            title = title
        )
    }
}