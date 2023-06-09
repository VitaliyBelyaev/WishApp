package ru.vitaliy.belyaev.wishapp.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LinksAdapterTest {

    @Test
    fun addLinkAndGetAccumulatedString() {
        // GIVEN
        val newLink = "https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html"
        val currentLinks = listOf(
            "https://kotlinlang.org/docs/multiplatform-run-tests.html",
            "https://kotlinlang.org/docs/gradle.html"
        )
        val expected = """
            https://kotlinlang.org/docs/multiplatform-run-tests.html|
            https://kotlinlang.org/docs/gradle.html|
            https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html
        """.trimIndent().replace("\n", "")

        // WHEN
        val actual = LinksAdapter.addLinkAndGetAccumulatedString(newLink, currentLinks)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun removeLinkAndGetAccumulatedString() {
        // GIVEN
        val linkToRemove = "https://kotlinlang.org/docs/gradle.html"
        val currentLinks = listOf(
            "https://kotlinlang.org/docs/multiplatform-run-tests.html",
            "https://kotlinlang.org/docs/gradle.html",
            "https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html"
        )
        val expected = """
            https://kotlinlang.org/docs/multiplatform-run-tests.html|
            https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html
        """.trimIndent().replace("\n", "")

        // WHEN
        val actual = LinksAdapter.removeLinkAndGetAccumulatedString(linkToRemove, currentLinks)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun getLinksListFromString() {
        // GIVEN
        val linksString = """
            https://kotlinlang.org/docs/multiplatform-run-tests.html|
            https://kotlinlang.org/docs/gradle.html|
            https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            "https://kotlinlang.org/docs/multiplatform-run-tests.html",
            "https://kotlinlang.org/docs/gradle.html",
            "https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html"
        )

        // WHEN
        val actual = LinksAdapter.getLinksListFromString(linksString)

        // THEN
        assertEquals(expected, actual)
    }
}