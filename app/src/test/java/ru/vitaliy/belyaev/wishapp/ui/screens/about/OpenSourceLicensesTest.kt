package ru.vitaliy.belyaev.wishapp.ui.screens.about

import org.junit.Assert.assertEquals
import org.junit.Test

/** Разбор файлов `oss-licenses-plugin`: смещения в байтах, повторы и битые строки. */
class OpenSourceLicensesTest {

    @Test
    fun readsTextsByByteOffsetsSortedAndWithoutDuplicates() {
        val apache = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        val ofl = "Copyright © Onest\nSIL OFL 1.1"
        val texts = (apache + ofl).encodeToByteArray()
        val apacheBytes = apache.encodeToByteArray().size
        val oflBytes = ofl.encodeToByteArray().size
        val metadata = listOf(
            "0:$apacheBytes Kotlin Coroutines",
            "$apacheBytes:$oflBytes Onest",
            "0:$apacheBytes kotlin coroutines",
            "broken line",
            "0:${texts.size + 1} Out Of Range",
            "",
        ).joinToString("\n")

        val licenses = parseLicenses(metadata, texts)

        assertEquals(
            listOf(OpenSourceLicense("Kotlin Coroutines", apache), OpenSourceLicense("Onest", ofl)),
            licenses,
        )
    }
}
