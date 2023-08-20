package ru.vitaliy.belyaev.wishapp.shared.data.mapper

import ru.vitaliy.belyaev.wishapp.shared.data.database.Tag
import ru.vitaliy.belyaev.wishapp.shared.data.database.Wish
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

object WishMapper {

    fun mapToDomain(wish: Wish, tags: List<Tag>): WishEntity {
        return with(wish) {
            WishEntity(
                wishId,
                title,
                link,
                LinksAdapter.getLinksListFromString(link),
                comment,
                isCompleted,
                createdTimestamp,
                updatedTimestamp,
                position,
                tags.map { TagMapper.mapToDomain(it.tagId, it.title) }
            )
        }
    }
}