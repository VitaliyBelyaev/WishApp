package ru.vitaliy.belyaev.wishapp.shared.data.mapper

import ru.vitaliy.belyaev.wishapp.shared.data.database.GetWishTags
import ru.vitaliy.belyaev.wishapp.shared.data.database.Wish
import ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

object WishMapper {

    fun mapToDomain(
        wish: Wish,
        tags: List<GetWishTags>,
        images: List<ImageRef> = emptyList(),
    ): WishEntity {
        return with(wish) {
            WishEntity(
                id = wishId,
                title = title,
                link = link,
                links = LinksAdapter.getLinksListFromString(link),
                comment = comment,
                isCompleted = isCompleted,
                createdTimestamp = createdTimestamp,
                updatedTimestamp = updatedTimestamp,
                position = position,
                tags = tags.map(TagMapper::mapToDomain),
                images = images,
                completedTimestamp = completedTimestamp,
                shownTagId = TagMapper.shownTagId(tags),
            )
        }
    }
}
