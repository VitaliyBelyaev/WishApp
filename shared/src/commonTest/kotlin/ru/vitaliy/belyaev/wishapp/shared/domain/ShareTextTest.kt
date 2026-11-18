package ru.vitaliy.belyaev.wishapp.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

class ShareTextTest {

    private fun wish(title: String = "", comment: String = "", links: List<String> = emptyList()) = WishEntity(
        id = title + comment,
        title = title,
        link = links.joinToString("|"),
        links = links,
        comment = comment,
        isCompleted = false,
        createdTimestamp = 0,
        updatedTimestamp = 0,
        position = 0,
    )

    @Test
    fun compactListHasNoDividersAndShortLinks() {
        val text = ShareText.listText(
            header = "Вот мой список хотелок:",
            wishes = listOf(
                wish("Мультитул", links = listOf("https://www.example.com/tool")),
                wish("Робот пылесос", comment = "Чтобы с шерстью\n\nсправлялся", links = listOf("http://localhost/x")),
            ),
            photoOnlyLabel = "Фото",
            footer = "Сделано в Хотелках",
            compact = true,
        )

        assertEquals(
            "Вот мой список хотелок:\n\n" +
                "1. Мультитул\nexample.com/tool\n\n" +
                "2. Робот пылесос\nЧтобы с шерстью\nсправлялся\nhttp://localhost/x\n\n" +
                "Сделано в Хотелках",
            text,
        )
    }

    @Test
    fun spaciousListKeepsNumbersAndDividers() {
        val text = ShareText.listText(
            header = "Вот мой список хотелок:",
            wishes = listOf(
                wish("Мультитул", links = listOf("https://example.com/tool")),
                wish("Робот пылесос", comment = "Чтобы с шерстью справлялся"),
            ),
            photoOnlyLabel = "Фото",
            footer = "Сделано в Хотелках",
            compact = false,
        )

        assertEquals(
            "Вот мой список хотелок:\n\n" +
                "1. Мультитул\n\nhttps://example.com/tool\n" +
                "_____________\n\n" +
                "2. Робот пылесос\n\nЧтобы с шерстью справлялся",
            text,
        )
    }

    @Test
    fun untitledWishTakesCommentThenDomainThenPhotoLabel() {
        val text = ShareText.listText(
            header = "Вот мой список хотелок:",
            wishes = listOf(
                wish(comment = "Что-то тёплое на зиму\nШерсть, серый"),
                wish(links = listOf("https://www.example.com/item/42")),
                wish(),
            ),
            photoOnlyLabel = "Фото",
            footer = "",
            compact = true,
        )

        assertEquals(
            "Вот мой список хотелок:\n\n" +
                "1. Что-то тёплое на зиму\nШерсть, серый\n\n" +
                "2. example.com\nexample.com/item/42\n\n" +
                "3. Фото",
            text,
        )
    }

    @Test
    fun singleWishHasNoNumber() {
        assertEquals(
            "Вот что я хочу:\n\nПлед\nСерый\n\nСделано в Хотелках",
            ShareText.singleWishText(
                "Вот что я хочу:",
                wish("Плед", comment = "Серый"),
                "Фото",
                "Сделано в Хотелках",
                true,
            ),
        )
        assertEquals(
            "Вот что я хочу:\n\nПлед\n\nСерый",
            ShareText.singleWishText(
                "Вот что я хочу:",
                wish("Плед", comment = "Серый"),
                "Фото",
                "Сделано в Хотелках",
                false,
            ),
        )
    }

    @Test
    fun cleanerDropsOnlyTrackingParams() {
        assertEquals(
            "https://www.citilink.ru/product/1/?region_id=123062",
            ShareLinkCleaner.clean("https://www.citilink.ru/product/1/?region_id=123062&gclid=Cjw_BwE"),
        )
        assertEquals(
            "https://example.com/p?id=5#top",
            ShareLinkCleaner.clean("https://example.com/p?utm_source=x&id=5&UTM_Medium=y&fbclid=1&yclid=2#top"),
        )
        assertEquals("https://example.com/p", ShareLinkCleaner.clean("https://example.com/p?utm_source=x"))
        assertEquals("https://example.com/p#a", ShareLinkCleaner.clean("https://example.com/p?gclid=1#a"))
        assertEquals("https://example.com/p", ShareLinkCleaner.clean("https://example.com/p"))
    }

    @Test
    fun hostDropsSchemeWwwAndPath() {
        assertEquals("ozon.ru", linkHost("https://www.ozon.ru/t/abc?x=1"))
    }
}
