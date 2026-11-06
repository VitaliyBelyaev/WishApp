package ru.vitaliy.belyaev.wishapp.ui.components.wish

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.ui.theme.signedHueDifference

class WishCardMapperTest {

    @Test
    fun linkHostDropsSchemeWwwPortAndPath() {
        assertEquals("ozon.ru", linkHost("https://www.ozon.ru/t/abc?x=1"))
        assertEquals("market.yandex.ru", linkHost("https://market.yandex.ru/cc/abc"))
        assertEquals("example.com", linkHost("http://user@Example.com:8080#top"))
        assertEquals("пример.рф", linkHost("https://пример.рф/товар"))
        assertEquals("shop.example.com", linkHost("shop.example.com/item"))
    }

    @Test
    fun hueDifferenceTakesShortestWay() {
        assertEquals(10.0, signedHueDifference(from = 350.0, to = 0.0), 1e-9)
        assertEquals(-10.0, signedHueDifference(from = 0.0, to = 350.0), 1e-9)
        assertEquals(15.0, signedHueDifference(from = 100.0, to = 115.0), 1e-9)
    }
}
