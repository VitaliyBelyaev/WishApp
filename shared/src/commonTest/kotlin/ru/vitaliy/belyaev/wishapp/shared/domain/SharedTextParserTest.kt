package ru.vitaliy.belyaev.wishapp.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedTextParserTest {

    @Test
    fun onlyLink() {
        val draft = SharedTextParser.parse(subject = null, text = "https://example.com/item")

        assertEquals(SharedWishDraft(title = "", comment = "", links = listOf("https://example.com/item")), draft)
    }

    @Test
    fun titleFromFirstLineWhenLinkInsideText() {
        val draft = SharedTextParser.parse(
            subject = null,
            text = "Чайник Xiaomi Mi Smart Kettle за 2 990 ₽ на Маркете: https://market.yandex.ru/cc/abc"
        )

        assertEquals("Чайник Xiaomi Mi Smart Kettle за 2 990 ₽ на Маркете", draft.title)
        assertEquals("", draft.comment)
        assertEquals(listOf("https://market.yandex.ru/cc/abc"), draft.links)
    }

    @Test
    fun restOfTextGoesToComment() {
        val draft = SharedTextParser.parse(
            subject = null,
            text = "Наушники\nС шумоподавлением\n\nhttps://example.com/a\nЛучше чёрные"
        )

        assertEquals("Наушники", draft.title)
        assertEquals("С шумоподавлением\nЛучше чёрные", draft.comment)
        assertEquals(listOf("https://example.com/a"), draft.links)
    }

    @Test
    fun subjectBecomesTitle() {
        val draft = SharedTextParser.parse(
            subject = "Кофемашина",
            text = "Рожковая, с капучинатором https://example.com/coffee"
        )

        assertEquals("Кофемашина", draft.title)
        assertEquals("Рожковая, с капучинатором", draft.comment)
        assertEquals(listOf("https://example.com/coffee"), draft.links)
    }

    @Test
    fun subjectThatIsOnlyLinkIsNotTitle() {
        val draft = SharedTextParser.parse(
            subject = "https://example.com/a",
            text = "Настольная лампа\nhttps://example.com/a"
        )

        assertEquals("Настольная лампа", draft.title)
        assertEquals(listOf("https://example.com/a"), draft.links)
    }

    @Test
    fun subjectRepeatedInTextIsNotDuplicatedInComment() {
        val draft = SharedTextParser.parse(
            subject = "Настольная лампа",
            text = "Настольная лампа: https://example.com/a"
        )

        assertEquals("Настольная лампа", draft.title)
        assertEquals("", draft.comment)
    }

    @Test
    fun noLinks() {
        val draft = SharedTextParser.parse(subject = null, text = "Велосипед\nГорный, 29 дюймов")

        assertEquals(SharedWishDraft(title = "Велосипед", comment = "Горный, 29 дюймов", links = emptyList()), draft)
    }

    @Test
    fun separatorLineIsNotTitle() {
        val draft = SharedTextParser.parse(subject = null, text = "—\nhttps://example.com\nПлед")

        assertEquals("Плед", draft.title)
        assertEquals("", draft.comment)
    }

    @Test
    fun blankInputIsEmpty() {
        assertTrue(SharedTextParser.parse(subject = " ", text = "\n ").isEmpty)
        assertTrue(SharedTextParser.parse(subject = null, text = null).isEmpty)
    }
}
