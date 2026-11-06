package ru.vitaliy.belyaev.wishapp.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LinkParserTest {

    @Test
    fun extractsLinkFromTextAroundIt() {
        val text = "Смотри, что нашёл https://www.ozon.ru/product/123/?sku=1 очень круто"

        assertEquals(listOf("https://www.ozon.ru/product/123/?sku=1"), LinkParser.extractLinks(text))
    }

    @Test
    fun extractsSeveralLinksWithoutDuplicates() {
        val text = "https://a.com/1\nhttps://b.com/2 и ещё раз https://a.com/1"

        assertEquals(listOf("https://a.com/1", "https://b.com/2"), LinkParser.extractLinks(text))
    }

    @Test
    fun cutsTrailingPunctuation() {
        val text = "Вот: https://example.com/item. А вот (https://example.com/other), «https://example.com/q»!"

        assertEquals(
            listOf("https://example.com/item", "https://example.com/other", "https://example.com/q"),
            LinkParser.extractLinks(text)
        )
    }

    @Test
    fun keepsBalancedParenthesesInsideLink() {
        val text = "https://en.wikipedia.org/wiki/Kotlin_(programming_language)."

        assertEquals(
            listOf("https://en.wikipedia.org/wiki/Kotlin_(programming_language)"),
            LinkParser.extractLinks(text)
        )
    }

    @Test
    fun addsSchemeToWwwLink() {
        assertEquals(listOf("https://www.example.com/a"), LinkParser.extractLinks("тут www.example.com/a"))
    }

    @Test
    fun ignoresBareDomainsInFreeText() {
        assertEquals(emptyList(), LinkParser.extractLinks("Файл report.txt, т.е. отчёт"))
    }

    @Test
    fun keepsCyrillicPathAndDomain() {
        assertEquals(
            listOf("https://пример.рф/товар/чайник"),
            LinkParser.extractLinks("https://пример.рф/товар/чайник")
        )
    }

    @Test
    fun parseLinkInputAcceptsBareDomain() {
        assertEquals(listOf("https://ozon.ru/t/abc"), LinkParser.parseLinkInput("  ozon.ru/t/abc "))
        assertEquals(listOf("https://пример.рф"), LinkParser.parseLinkInput("пример.рф"))
    }

    @Test
    fun parseLinkInputTakesLinkFromSharedText() {
        assertEquals(
            listOf("https://market.yandex.ru/cc/abc"),
            LinkParser.parseLinkInput("Чайник Xiaomi за 2 990 ₽ на Маркете: https://market.yandex.ru/cc/abc")
        )
    }

    @Test
    fun parseLinkInputRejectsPlainText() {
        assertEquals(emptyList(), LinkParser.parseLinkInput("просто текст"))
        assertEquals(emptyList(), LinkParser.parseLinkInput("ozon"))
        assertEquals(emptyList(), LinkParser.parseLinkInput("ozon ru"))
        assertEquals(emptyList(), LinkParser.parseLinkInput(""))
    }

    @Test
    fun parseLinkInputKeepsOtherScheme() {
        assertEquals(listOf("http://example.com"), LinkParser.parseLinkInput("http://example.com"))
    }
}
