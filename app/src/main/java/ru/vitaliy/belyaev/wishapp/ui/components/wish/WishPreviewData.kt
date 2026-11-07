package ru.vitaliy.belyaev.wishapp.ui.components.wish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.ui.theme.AppTheme
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing

/* Примеры хотелок и фон для превью карточки и строки: как в превью компонентов артефакта дизайн-системы. */

internal fun previewWish(
    id: String,
    title: String = "",
    comment: String = "",
    linkHost: String? = null,
    linksCount: Int = if (linkHost != null) 1 else 0,
    shownTag: String? = null,
    otherTagsCount: Int = 0,
    photosCount: Int = 0,
    lostPhotos: Set<Int> = emptySet(),
    isCompleted: Boolean = false,
    tone: PaperTone = PaperTone.PEACH,
) = WishCardModel(
    id = id,
    title = title,
    comment = comment,
    linkHost = linkHost,
    linksCount = linksCount,
    shownTag = shownTag,
    otherTagsCount = otherTagsCount,
    // В превью фото не грузится, карточка рисует вместо него заливку, а пропавшее как в приложении
    photos = List(photosCount) { if (it in lostPhotos) PREVIEW_LOST_PHOTO else "preview://$id/$it" },
    isCompleted = isCompleted,
    tone = tone,
)

internal object PreviewWishes {
    const val LONG_TITLE = "Подарочное издание всех романов Булгакова с иллюстрациями в твёрдой обложке " +
        "с закладкой-ляссе и золотым тиснением на корешке"

    val CoffeeMachine = previewWish(
        id = "b",
        title = "Кофемашина",
        comment = "Рожковая, с капучинатором",
        linkHost = "example.com",
        shownTag = "Техника",
        otherTagsCount = 2,
        photosCount = 1,
        tone = PaperTone.SKY,
    )
    val Headphones = previewWish(
        id = "a", title = "Беспроводные наушники", comment = "С шумоподавлением, лучше чёрные. Хотелось бы к отпуску.",
        linkHost = "example.com", linksCount = 2, shownTag = "Техника", otherTagsCount = 3, photosCount = 3,
        tone = PaperTone.SKY,
    )
    val Plain = previewWish(
        id = "g",
        title = "Беговые кроссовки",
        comment = "Размер 42, для асфальта",
        shownTag = "Спорт",
        tone = PaperTone.LIME,
    )
    val Coffee = previewWish(
        id = "x1",
        title = "Набор для домашней обжарки кофе",
        comment = "Хочу попробовать обжаривать зёрна дома: небольшая ростер-машина, термометр и контейнеры " +
            "для хранения. Смотрела разные варианты, пока больше всего нравится модель из второй ссылки, но она дорогая.",
        linkHost = "example.com",
        linksCount = 5,
        shownTag = "Кухня",
        otherTagsCount = 2,
        tone = PaperTone.BUTTER,
    )
    val LongTitle = previewWish(
        id = "x2",
        title = "Подарочное издание всех романов Булгакова с иллюстрациями",
        comment = "Лучше всего в твёрдой обложке, с закладкой-ляссе и крупным шрифтом, чтобы было приятно держать " +
            "в руках и перечитывать вечерами",
        linkHost = "knizhnyi-magazin-moskva.example",
        linksCount = 3,
        shownTag = "Книги",
        otherTagsCount = 1,
        tone = PaperTone.ROSE,
    )
    val VeryLongTitle = previewWish(id = "t5", title = LONG_TITLE, shownTag = "Книги", tone = PaperTone.ROSE)
    val NoTag = previewWish(id = "t", title = "Плед", comment = "Шерстяной")
    val TitleOnly = previewWish(id = "t4", title = "Плед")
    val Done = previewWish(
        id = "h",
        title = "Велосипед",
        comment = "Горный, 29 дюймов",
        shownTag = "Спорт",
        isCompleted = true,
        tone = PaperTone.LIME,
    )
    val DoneWithPhoto = previewWish(
        id = "i",
        title = "Настольная лампа",
        shownTag = "Дом",
        photosCount = 1,
        isCompleted = true,
        tone = PaperTone.BUTTER,
    )
    val CommentOnly = previewWish(
        id = "t1",
        comment = "Что-то тёплое на зиму, посмотреть в выходные",
        shownTag = "Дом",
        tone = PaperTone.BUTTER,
    )
    val CommentOnlyLong = previewWish(
        id = "t6",
        comment = "Что-то тёплое на зиму, посмотреть в выходные, можно шерсть или кашемир, главное чтобы не " +
            "кололось и было длинным",
    )
    val LinkOnly = previewWish(id = "t2", linkHost = "shop.example.com")
    val LinksOnly = previewWish(
        id = "t7",
        linkHost = "shop.example.com",
        linksCount = 3,
        shownTag = "Дом",
        tone = PaperTone.BUTTER,
    )
    val PhotoOnly = previewWish(id = "t3", photosCount = 1, shownTag = "Дом", tone = PaperTone.BUTTER)
    val PhotoOnlyNoTag = previewWish(id = "t8", photosCount = 1)
    val PhotosOnlySeveral = previewWish(id = "t12", photosCount = 2)
    val LinksOnlyTwo = previewWish(
        id = "t13",
        linkHost = "shop.example.com",
        linksCount = 2,
        shownTag = "Дом",
        tone = PaperTone.BUTTER,
    )
    val LongTag = previewWish(
        id = "t9",
        title = "Кеды",
        shownTag = "Подарки на новый год для всей семьи",
        otherTagsCount = 1,
        tone = PaperTone.MINT,
    )

    val LostSingle = CoffeeMachine.copy(id = "lost1", photos = listOf(PREVIEW_LOST_PHOTO))
    val LostFirst = Headphones.copy(
        id = "lost2",
        photos = listOf(PREVIEW_LOST_PHOTO, "preview://a/1", "preview://a/2"),
    )
    val LostAll = Headphones.copy(id = "lost3", photos = List(3) { PREVIEW_LOST_PHOTO })
    val LostPhotoOnly = PhotoOnlyNoTag.copy(id = "lost4", photos = listOf(PREVIEW_LOST_PHOTO))

    /** В фильтре «Подарки»: цвет выбранного ярлыка, внизу только `+N` остальных. */
    val InFilter = Headphones.copy(id = "f1", shownTag = null, otherTagsCount = 3, tone = PaperTone.ROSE)
    val InFilterSingleTag = Plain.copy(id = "f2", shownTag = null, otherTagsCount = 0, tone = PaperTone.ROSE)
}

@Composable
internal fun PreviewSurface(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    AppTheme(selectedTheme = if (darkTheme) Theme.DARK else Theme.LIGHT, useWallpaperColors = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(WishAppSpacing.s4)) { content() }
        }
    }
}
