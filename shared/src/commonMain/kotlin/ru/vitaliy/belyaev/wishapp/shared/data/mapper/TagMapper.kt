package ru.vitaliy.belyaev.wishapp.shared.data.mapper

import ru.vitaliy.belyaev.wishapp.shared.data.database.GetWishTags
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity

object TagMapper {

    fun mapToDomain(tagId: String, title: String, color: String): TagEntity {
        return TagEntity(
            id = tagId,
            title = title,
            color = TagColor.fromStoredValue(color),
        )
    }

    fun mapToDomain(wishTag: GetWishTags): TagEntity = mapToDomain(wishTag.tagId, wishTag.title, wishTag.color)

    /** Показанный ярлык: отмеченный, иначе добавленный раньше других. */
    fun shownTagId(wishTags: List<GetWishTags>): String? {
        return (wishTags.firstOrNull { it.isShown } ?: wishTags.minByOrNull { it.relationOrder })?.tagId
    }
}
