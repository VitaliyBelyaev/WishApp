package ru.vitaliy.belyaev.wishapp.shared.domain.entity

/**
 * Новая хотелка из шторки: всё, что в ней набрали, пишется одним разом. Ссылки уже разобраны и
 * без дублей, ярлыки по порядку выбора, первый из них показан на карточке. Фото уже уменьшены.
 */
class NewWish(
    val title: String,
    val comment: String,
    val links: List<String>,
    val tagIds: List<String>,
    val images: List<ByteArray>,
)
