package ru.vitaliy.belyaev.wishapp.ui.components.wish.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.vitaliy.belyaev.wishapp.ui.components.wish.PreviewSurface
import ru.vitaliy.belyaev.wishapp.ui.components.wish.PreviewWishes
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishCardModel
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishSelection
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing

/* Превью карточки: разделы как у превью WishCard в артефакте дизайн-системы. */

/** Карточки в две колонки по очереди, как в сетке списка. */
@Composable
private fun CardColumns(items: List<Pair<WishCardModel, WishSelection>>) {
    Row(horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
        listOf(0, 1).forEach { column ->
            Column(
                verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
                modifier = Modifier.weight(1f),
            ) {
                items.filterIndexed { index, _ -> index % 2 == column }.forEach { (card, selection) ->
                    WishCard(
                        model = card,
                        selection = selection,
                        onClick = {},
                        onLongClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun List<WishCardModel>.plain() = map { it to WishSelection.NONE }

private val ContentCards = with(PreviewWishes) {
    listOf(
        CoffeeMachine, Headphones, Plain, Coffee, LongTitle, VeryLongTitle, NoTag, TitleOnly, Done, DoneWithPhoto,
        CommentOnly, CommentOnlyLong, LinkOnly, LinksOnly, PhotoOnly, PhotoOnlyNoTag, LongTag,
    )
}

@Preview(name = "Карточка: содержимое", widthDp = 360, heightDp = 1900)
@Composable
private fun WishCardContentPreview() {
    PreviewSurface { CardColumns(ContentCards.plain()) }
}

@Preview(name = "Карточка: содержимое, тёмная", widthDp = 360, heightDp = 1900)
@Composable
private fun WishCardContentDarkPreview() {
    PreviewSurface(darkTheme = true) { CardColumns(ContentCards.plain()) }
}

@Preview(name = "Карточка: фото не загрузилось", widthDp = 360, heightDp = 760)
@Composable
private fun WishCardLostPhotoPreview() {
    PreviewSurface {
        with(PreviewWishes) { CardColumns(listOf(LostSingle, LostFirst, LostAll, LostPhotoOnly).plain()) }
    }
}

@Preview(name = "Карточка: режим выбора", widthDp = 360, heightDp = 1500)
@Composable
private fun WishCardSelectionPreview() {
    PreviewSurface {
        with(PreviewWishes) {
            CardColumns(
                listOf(
                    // Не выбрана: без изменений, счётчик фото справа
                    Plain to WishSelection.UNSELECTED,
                    Headphones to WishSelection.UNSELECTED,
                    // Выбрана: обводка и галочка, счётчик левее галочки
                    Plain.copy(id = "g2") to WishSelection.SELECTED,
                    Headphones.copy(id = "a2") to WishSelection.SELECTED,
                    PhotosOnlySeveral to WishSelection.SELECTED,
                    LostAll.copy(id = "lost5") to WishSelection.SELECTED,
                    LinksOnlyTwo to WishSelection.SELECTED,
                    LongTitle to WishSelection.SELECTED,
                    Done to WishSelection.SELECTED,
                ),
            )
        }
    }
}

@Preview(name = "Карточка: режим выбора, тёмная", widthDp = 360, heightDp = 1000)
@Composable
private fun WishCardSelectionDarkPreview() {
    PreviewSurface(darkTheme = true) {
        with(PreviewWishes) {
            CardColumns(
                listOf(
                    Plain to WishSelection.UNSELECTED,
                    Headphones to WishSelection.SELECTED,
                    LongTitle to WishSelection.SELECTED,
                    Done to WishSelection.UNSELECTED,
                ),
            )
        }
    }
}

@Preview(name = "Карточка: в фильтре", widthDp = 360, heightDp = 520)
@Composable
private fun WishCardFilterPreview() {
    PreviewSurface {
        with(PreviewWishes) { CardColumns(listOf(InFilter, InFilterSingleTag).plain()) }
    }
}

@Preview(name = "Карточка: шрифт 200%", widthDp = 360, heightDp = 1200, fontScale = 2f)
@Composable
private fun WishCardLargeFontPreview() {
    PreviewSurface {
        with(PreviewWishes) {
            CardColumns(
                listOf(
                    CoffeeMachine,
                    VeryLongTitle.copy(comment = "Лучше в твёрдой обложке", linkHost = "example.com", linksCount = 1),
                ).plain(),
            )
        }
    }
}
