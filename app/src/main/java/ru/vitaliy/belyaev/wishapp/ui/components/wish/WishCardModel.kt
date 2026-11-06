package ru.vitaliy.belyaev.wishapp.ui.components.wish

import androidx.compose.runtime.Immutable
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone

/**
 * Что показывают карточка и строка хотелки. Собирается из `WishEntity` на экране списка,
 * сами они про базу ничего не знают.
 *
 * @param linkHost короткий домен первой ссылки, `null` если ссылок нет.
 * @param shownTag ярлык, показанный на карточке. В фильтре по ярлыку `null`, тогда внизу только `+N`
 * с числом остальных ярлыков.
 * @param photos модели для Coil, главное фото первым. Если фото не открылось, показывается следующее.
 * @param tone цвет карточки: показанный ярлык, без ярлыка [PaperTone.PEACH]. В фильтре цвет выбранного ярлыка.
 */
@Immutable
data class WishCardModel(
    val id: String,
    val title: String,
    val comment: String,
    val linkHost: String?,
    val linksCount: Int,
    val shownTag: String?,
    val otherTagsCount: Int,
    val photos: List<Any>,
    val isCompleted: Boolean,
    val tone: PaperTone,
)
