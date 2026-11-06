package ru.vitaliy.belyaev.wishapp.ui.components.wish.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.vitaliy.belyaev.wishapp.ui.components.wish.PreviewSurface
import ru.vitaliy.belyaev.wishapp.ui.components.wish.PreviewWishes
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishCardModel
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishSelection
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing

/* Превью строки: разделы как у превью WishRow в артефакте дизайн-системы. */

@Composable
private fun Rows(items: List<Pair<WishCardModel, WishSelection>>) {
    Column(verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
        items.forEach { (model, selection) ->
            WishRow(
                model = model,
                selection = selection,
                onClick = {},
                onLongClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val RowCards = with(PreviewWishes) {
    listOf(
        CoffeeMachine, Plain, Coffee, VeryLongTitle, NoTag, DoneWithPhoto, CommentOnlyLong.copy(shownTag = "Дом"),
        LinksOnly.copy(shownTag = null), PhotoOnlyNoTag, LostSingle, LongTag, InFilter,
    ).map { it to WishSelection.NONE }
}

@Preview(name = "Строка: содержимое", widthDp = 360, heightDp = 1500)
@Composable
private fun WishRowContentPreview() {
    PreviewSurface { Rows(RowCards) }
}

@Preview(name = "Строка: содержимое, тёмная", widthDp = 360, heightDp = 1500)
@Composable
private fun WishRowContentDarkPreview() {
    PreviewSurface(darkTheme = true) { Rows(RowCards) }
}

@Preview(name = "Строка: режим выбора", widthDp = 360, heightDp = 640)
@Composable
private fun WishRowSelectionPreview() {
    PreviewSurface {
        with(PreviewWishes) {
            Rows(
                listOf(
                    Plain to WishSelection.UNSELECTED,
                    CoffeeMachine to WishSelection.SELECTED,
                    Plain.copy(id = "g2") to WishSelection.SELECTED,
                    VeryLongTitle to WishSelection.SELECTED,
                ),
            )
        }
    }
}
