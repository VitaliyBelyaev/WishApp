package ru.vitaliy.belyaev.wishapp.shared.domain

/** Хотелка, разобранная из текста, которым поделились из другого приложения. */
data class SharedWishDraft(
    val title: String,
    val comment: String,
    val links: List<String>,
) {
    val isEmpty: Boolean
        get() = title.isBlank() && comment.isBlank() && links.isEmpty()
}

/**
 * Разбор `EXTRA_SUBJECT` и `EXTRA_TEXT` из «Поделиться» другого приложения.
 *
 * - Все адреса из темы и текста идут в ссылки, без дублей и знаков препинания на конце.
 * - Название: тема, если она есть и это не только адрес. Иначе первая непустая строка
 *   текста без адресов.
 * - Остальной текст без адресов идёт в комментарий.
 * - Только адрес: название пустое, хотелка всё равно не пустая.
 */
object SharedTextParser {

    private const val TITLE_TRAILING_SEPARATORS = ":-–—|·•,"

    fun parse(subject: String?, text: String?): SharedWishDraft {
        val subjectText = subject.orEmpty()
        val bodyText = text.orEmpty()

        val links = (LinkParser.extractLinks(subjectText) + LinkParser.extractLinks(bodyText)).distinct()

        val subjectTitle = cleanTitle(LinkParser.removeLinks(subjectText))
        val bodyLines = LinkParser.removeLinks(bodyText)
            .lines()
            .map { cleanLine(it) }
            .filter { it.isNotEmpty() }

        val title: String
        val commentLines: List<String>
        if (subjectTitle.isNotEmpty()) {
            title = subjectTitle
            // Многие приложения дублируют тему первой строкой текста
            val firstLineRepeatsSubject = bodyLines.firstOrNull()?.let { cleanTitle(it) } == subjectTitle
            commentLines = if (firstLineRepeatsSubject) bodyLines.drop(1) else bodyLines
        } else {
            // Строка из одних разделителей вроде «—» названием быть не может
            val titleIndex = bodyLines.indexOfFirst { cleanTitle(it).isNotEmpty() }
            title = if (titleIndex >= 0) cleanTitle(bodyLines[titleIndex]) else ""
            commentLines = if (titleIndex >= 0) bodyLines.drop(titleIndex + 1) else emptyList()
        }

        return SharedWishDraft(
            title = title,
            comment = commentLines.joinToString(separator = "\n"),
            links = links,
        )
    }

    /** Пробелы, оставшиеся на месте вырезанных адресов, схлопываются. */
    private fun cleanLine(line: String): String {
        return line.replace(MULTIPLE_SPACES, " ").trim()
    }

    /** У названия ещё и разделители по краям: «Посмотри в Ozon: <адрес>» → «Посмотри в Ozon». */
    private fun cleanTitle(line: String): String {
        return cleanLine(line).trim { it.isWhitespace() || it in TITLE_TRAILING_SEPARATORS }
    }

    private val MULTIPLE_SPACES = Regex("""[ \t ]+""")
}
