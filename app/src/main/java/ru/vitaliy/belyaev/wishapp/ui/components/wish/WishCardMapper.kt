package ru.vitaliy.belyaev.wishapp.ui.components.wish

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone

/**
 * Карточка из хотелки.
 *
 * Пока в базе нет цвета ярлыка и показанного ярлыка (часть 2.1), показан первый ярлык,
 * а цвет выбирается по его id. После 2.1 оба значения придут из `shared`.
 */
fun WishEntity.toWishCardModel(): WishCardModel {
    val shownTag = tags.firstOrNull()
    return WishCardModel(
        id = id,
        title = title,
        comment = comment,
        linkHost = links.firstOrNull()?.let { linkHost(it) }?.takeIf { it.isNotEmpty() },
        linksCount = links.size,
        shownTag = shownTag?.title,
        otherTagsCount = (tags.size - 1).coerceAtLeast(0),
        photos = images,
        isCompleted = isCompleted,
        tone = shownTag?.let { PaperTone.tagTones[Math.floorMod(it.id.hashCode(), PaperTone.tagTones.size)] }
            ?: PaperTone.PEACH,
    )
}

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
