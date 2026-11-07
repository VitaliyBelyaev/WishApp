package ru.vitaliy.belyaev.wishapp.ui.components.wish

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone

/**
 * Карточка из хотелки.
 *
 * В обычном списке на карточке показанный ярлык (`shared` выбирает его: отмеченный или первый добавленный),
 * и цвет карточки это его цвет. В фильтре по ярлыку [filterTag] цвет карточки это цвет выбранного ярлыка,
 * а внизу только `+N` с числом остальных ярлыков.
 */
fun WishEntity.toWishCardModel(filterTag: TagEntity? = null): WishCardModel {
    val shownTag = if (filterTag == null) tags.firstOrNull { it.id == shownTagId } ?: tags.firstOrNull() else null
    val toneTag = filterTag ?: shownTag
    return WishCardModel(
        id = id,
        title = title,
        comment = comment,
        linkHost = links.firstOrNull()?.let { linkHost(it) }?.takeIf { it.isNotEmpty() },
        linksCount = links.size,
        shownTag = shownTag?.title,
        otherTagsCount = if (filterTag == null) {
            (tags.size - 1).coerceAtLeast(0)
        } else {
            tags.count { it.id != filterTag.id }
        },
        photos = images,
        isCompleted = isCompleted,
        tone = toneTag?.color?.toPaperTone() ?: PaperTone.PEACH,
    )
}

/** Тон карточки по цвету ярлыка: имена совпадают, `peach` у ярлыков не бывает. */
fun TagColor.toPaperTone(): PaperTone = PaperTone.valueOf(name)

/** Домен ссылки без схемы, `www.`, порта и пути: `https://www.ozon.ru/t/abc` → `ozon.ru`. */
internal fun linkHost(link: String): String {
    return link.trim()
        .substringAfter("://")
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('@')
        .substringBefore(':')
        .removePrefix("www.")
        .lowercase()
}
