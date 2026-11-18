package ru.vitaliy.belyaev.wishapp.utils

import java.io.File
import java.io.RandomAccessFile
import java.util.Locale
import timber.log.Timber

/**
 * Делает ссылки в готовом PDF нажимаемыми. Системный `PdfDocument` умеет только рисовать, а ссылка
 * в PDF — отдельная пометка страницы: «этот прямоугольник ведёт на такой-то адрес». Формат разрешает
 * дописывать изменения в конец файла, так и делаем: новые версии листов со списком пометок, сами
 * пометки, свой раздел адресов объектов и хвост со ссылкой на прежний.
 *
 * Разбирает только то, что пишет `PdfDocument`: таблицу адресов обычного вида и словари листов
 * без потоков. Если что-то пошло не так, файл остаётся как был, просто без нажимаемых ссылок.
 */
object PdfLinks {

    /** Нажимаемое место листа в координатах холста: начало сверху слева, единицы — пункты. */
    class Link(val left: Float, val top: Float, val right: Float, val bottom: Float, val url: String)

    /** Адрес для нажатия: со схемой как есть, без схемы — `https://`, если похоже на домен. */
    fun target(link: String): String? {
        val trimmed = link.trim()
        return when {
            trimmed.isEmpty() || trimmed.any { it.isWhitespace() } -> null
            "://" in trimmed -> trimmed
            '.' in trimmed.substringBefore('/') -> "https://$trimmed"
            else -> null
        }
    }

    /** [links] — по листам в порядке страниц, [pageHeight] — высота листа в пунктах. */
    fun append(file: File, links: List<List<Link>>, pageHeight: Float) {
        if (links.all { it.isEmpty() }) return
        try {
            val update = build(file.readBytes(), links, pageHeight)
            RandomAccessFile(file, "rw").use { out ->
                out.seek(out.length())
                out.write(update)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e, "PDF links were not added")
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "PDF links were not added")
        } catch (e: IndexOutOfBoundsException) {
            Timber.w(e, "PDF links were not added")
        }
    }

    /** Дописка в конец файла [pdf]: всё, что нужно добавить, начиная с перевода строки. */
    internal fun build(pdf: ByteArray, links: List<List<Link>>, pageHeight: Float): ByteArray {
        val text = String(pdf, Charsets.ISO_8859_1)
        val startXref = Regex("""startxref\s+(\d+)\s+%%EOF\s*$""").find(text)
            ?.groupValues?.get(1)?.toInt()
            ?: error("No startxref")
        val offsets = readXref(text, startXref)
        val trailer = text.substring(text.lastIndexOf("trailer"), text.lastIndexOf("startxref"))
        val size = intKey(trailer, "Size") ?: error("No /Size")
        val root = refKey(trailer, "Root") ?: error("No /Root")
        val info = refKey(trailer, "Info")

        val dict = { number: Int -> dict(text, offsets, number) }
        val pages = mutableListOf<Int>()
        collectPages(refKey(dict(root), "Pages") ?: error("No /Pages"), dict, pages)

        val out = StringBuilder("\n")
        val entries = sortedMapOf<Int, Int>()
        var next = size
        fun obj(number: Int, body: String) {
            entries[number] = pdf.size + out.length
            out.append(number).append(" 0 obj\n").append(body).append("\nendobj\n")
        }

        links.forEachIndexed { index, pageLinks ->
            if (pageLinks.isEmpty() || index >= pages.size) return@forEachIndexed
            val page = pages[index]
            val pageDict = dict(page)
            check("/Annots" !in pageDict) { "Page $page already has annotations" }
            val annots = pageLinks.map { link ->
                val number = next++
                obj(number, annotation(link, pageHeight))
                number
            }
            val refs = annots.joinToString(" ") { "$it 0 R" }
            obj(page, pageDict.removeSuffix(">>") + "\n/Annots [$refs]>>")
        }

        val xref = pdf.size + out.length
        out.append("xref\n")
        entries.forEach { (number, offset) ->
            out.append(number).append(" 1\n").append(offset.toString().padStart(XREF_DIGITS, '0')).append(" 00000 n \n")
        }
        out.append("trailer\n<</Size ").append(next)
            .append("\n/Root ").append(root).append(" 0 R")
        if (info != null) out.append("\n/Info ").append(info).append(" 0 R")
        out.append("\n/Prev ").append(startXref).append(">>\nstartxref\n").append(xref).append("\n%%EOF\n")
        return out.toString().toByteArray(Charsets.ISO_8859_1)
    }

    /** Словарь объекта без потока: у листов, дерева листов и каталога потоков нет. */
    private fun dict(text: String, offsets: Map<Int, Int>, number: Int): String {
        val start = offsets[number] ?: error("No object $number")
        val body = text.substring(start, text.indexOf("endobj", start))
        check("stream" !in body) { "Object $number has a stream" }
        return body.substringAfter("obj").trim()
    }

    /** Листы по порядку: обход дерева `/Pages` → `/Kids` в глубину. */
    private fun collectPages(node: Int, dict: (Int) -> String, pages: MutableList<Int>) {
        val nodeDict = dict(node)
        if (Regex("""/Type\s*/Pages\b""").containsMatchIn(nodeDict)) {
            val kids = nodeDict.substringAfter("/Kids").substringAfter('[').substringBefore(']')
            Regex("""(\d+)\s+0\s+R""").findAll(kids).forEach { collectPages(it.groupValues[1].toInt(), dict, pages) }
        } else {
            pages += node
        }
    }

    private fun annotation(link: Link, pageHeight: Float): String {
        // В PDF начало координат внизу слева
        val rect = listOf(link.left, pageHeight - link.bottom, link.right, pageHeight - link.top)
            .joinToString(" ") { "%.2f".format(Locale.US, it) }
        return "<</Type /Annot\n/Subtype /Link\n/Rect [$rect]\n/Border [0 0 0]\n" +
            "/A <</S /URI\n/URI <${hex(asciiUri(link.url))}>>>>>"
    }

    /** Адрес из ASCII: всё остальное, включая пробелы, кодируется `%XX` по UTF-8. */
    private fun asciiUri(url: String): String = buildString {
        url.toByteArray(Charsets.UTF_8).forEach { byte ->
            val code = byte.toInt() and BYTE_MASK
            if (code in PRINTABLE_ASCII) append(code.toChar()) else append('%').append("%02X".format(Locale.US, code))
        }
    }

    private fun hex(value: String): String =
        value.toByteArray(Charsets.ISO_8859_1).joinToString("") { "%02X".format(Locale.US, it.toInt() and BYTE_MASK) }

    /** Адреса объектов из таблицы `xref` и всех прежних по цепочке `/Prev`; новее — главнее. */
    private fun readXref(text: String, start: Int): Map<Int, Int> {
        val offsets = mutableMapOf<Int, Int>()
        var position: Int? = start
        while (position != null) {
            check(text.startsWith("xref", position)) { "Unsupported xref at $position" }
            val section = text.substring(position + "xref".length, text.indexOf("trailer", position))
            val lines = section.lines().map { it.trim() }.filter { it.isNotEmpty() }
            var number = 0
            lines.forEach { line ->
                val parts = line.split(Regex("""\s+"""))
                if (parts.size == 2) {
                    number = parts[0].toInt()
                } else {
                    if (parts[2] == "n") offsets.putIfAbsent(number, parts[0].toInt())
                    number++
                }
            }
            val trailer = text.substring(text.indexOf("trailer", position), text.indexOf("startxref", position))
            position = intKey(trailer, "Prev")
        }
        return offsets
    }

    private fun intKey(dict: String, key: String): Int? =
        Regex("""/$key\s+(\d+)""").find(dict)?.groupValues?.get(1)?.toInt()

    private fun refKey(dict: String, key: String): Int? =
        Regex("""/$key\s+(\d+)\s+0\s+R""").find(dict)?.groupValues?.get(1)?.toInt()

    private const val XREF_DIGITS = 10
    private const val BYTE_MASK = 0xFF
    private val PRINTABLE_ASCII = 0x21..0x7E
}
