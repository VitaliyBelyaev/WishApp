package ru.vitaliy.belyaev.wishapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfLinksTest {

    /** PDF как у `PdfDocument`: таблица адресов, листы без потоков, дерево листов в два уровня. */
    private fun samplePdf(): ByteArray {
        val objects = listOf(
            "<</Producer (test)>>",
            "<</Type /Page\n/MediaBox [0 0 595 842]\n/Parent 6 0 R>>",
            "<</Type /Page\n/MediaBox [0 0 595 842]\n/Parent 6 0 R>>",
            "<</Type /Catalog\n/Pages 5 0 R>>",
            "<</Type /Pages\n/Count 2\n/Kids [6 0 R]>>",
            "<</Type /Pages\n/Count 2\n/Kids [2 0 R 3 0 R]\n/Parent 5 0 R>>",
        )
        val out = StringBuilder("%PDF-1.4\n")
        val offsets = objects.mapIndexed { index, body ->
            val offset = out.length
            out.append(index + 1).append(" 0 obj\n").append(body).append("\nendobj\n")
            offset
        }
        val xref = out.length
        out.append("xref\n0 ").append(objects.size + 1).append("\n0000000000 65535 f \n")
        offsets.forEach { out.append(it.toString().padStart(10, '0')).append(" 00000 n \n") }
        out.append("trailer\n<</Size 7\n/Root 4 0 R\n/Info 1 0 R>>\nstartxref\n").append(xref).append("\n%%EOF")
        return out.toString().toByteArray(Charsets.ISO_8859_1)
    }

    @Test
    fun appendsLinkToTheRightPage() {
        val pdf = samplePdf()
        val update = PdfLinks.build(
            pdf,
            links = listOf(emptyList(), listOf(PdfLinks.Link(40f, 100f, 200f, 120f, "https://ozon.ru/т a"))),
            pageHeight = 842f,
        )
        val full = String(pdf + update, Charsets.ISO_8859_1)
        val added = String(update, Charsets.ISO_8859_1)

        assertTrue(added.contains("7 0 obj\n<</Type /Annot\n/Subtype /Link\n/Rect [40.00 722.00 200.00 742.00]"))
        val uri = "https://ozon.ru/%D1%82%20a".toByteArray().joinToString("") { "%02X".format(java.util.Locale.US, it) }
        assertTrue(added, added.contains("/URI <$uri>"))
        assertTrue(added.contains("3 0 obj\n<</Type /Page\n/MediaBox [0 0 595 842]\n/Parent 6 0 R\n/Annots [7 0 R]>>"))
        assertTrue(!added.contains("\n2 0 obj"))
        assertTrue(added.contains("/Size 8\n/Root 4 0 R\n/Info 1 0 R\n/Prev "))

        // Каждая запись новой таблицы указывает на начало своего объекта
        val xref = Regex("""startxref\n(\d+)\n%%EOF\n$""").find(full)!!.groupValues[1].toInt()
        Regex("""(\d+) 1\n(\d{10}) 00000 n \n""").findAll(full.substring(xref)).forEach {
            val (number, offset) = it.destructured
            assertTrue(full.startsWith("$number 0 obj\n", offset.toInt()))
        }
    }

    @Test
    fun targetAddsSchemeOnlyToDomains() {
        assertEquals("https://example.com/a", PdfLinks.target("https://example.com/a"))
        assertEquals("https://ozon.ru/t/1", PdfLinks.target("ozon.ru/t/1"))
        assertNull(PdfLinks.target("подарок маме"))
        assertNull(PdfLinks.target("nolink"))
    }
}
