package ru.vitaliy.belyaev.wishapp.shared.domain

/**
 * Находит ссылки в тексте. Чистые функции без платформенного кода, чтобы правила были
 * одинаковыми на Android и iOS.
 *
 * В свободном тексте ссылкой считается только адрес со схемой `http(s)://` или с `www.`:
 * иначе слова вроде «file.txt» или «т.е.» превращались бы в ссылки. Голый домен
 * («ozon.ru/t/abc») принимается, только когда это весь ввод в поле ссылки.
 */
object LinkParser {

    private val URL_IN_TEXT = Regex(
        pattern = """(?:https?://|www\.)[^\s<>"'«»“”„]+""",
        option = RegexOption.IGNORE_CASE
    )

    private val BARE_DOMAIN = Regex(
        pattern = """[\p{L}\p{N}](?:[\p{L}\p{N}-]*[\p{L}\p{N}])?""" +
            """(?:\.[\p{L}\p{N}](?:[\p{L}\p{N}-]*[\p{L}\p{N}])?)*""" +
            """\.\p{L}{2,24}(?::\d{1,5})?(?:[/?#]\S*)?"""
    )

    private val SCHEME = Regex("""^[a-z][a-z0-9+.-]*://""", RegexOption.IGNORE_CASE)

    private const val TRAILING_PUNCTUATION = ".,;:!?…'\"»”“„"

    /** Все адреса из текста по порядку, без дублей и знаков препинания на конце. */
    fun extractLinks(text: String): List<String> {
        return URL_IN_TEXT.findAll(text)
            .map { normalize(trimTrailingPunctuation(it.value)) }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
    }

    /**
     * Разбор ввода в поле ссылки. Если в нём есть адреса со схемой или `www.`, берём их.
     * Иначе весь ввод без пробелов может быть голым доменом, ему добавляется `https://`.
     * Пустой список значит, что ссылки во вводе нет.
     */
    fun parseLinkInput(input: String): List<String> {
        val links = extractLinks(input)
        if (links.isNotEmpty()) return links

        val candidate = trimTrailingPunctuation(input.trim())
        return if (BARE_DOMAIN.matches(candidate)) listOf(normalize(candidate)) else emptyList()
    }

    /** Текст с вырезанными адресами, остальное не меняется. */
    fun removeLinks(text: String): String = URL_IN_TEXT.replace(text, "")

    private fun normalize(link: String): String {
        val withScheme = if (SCHEME.containsMatchIn(link)) link else "https://$link"
        return LinksAdapter.sanitizeLink(withScheme)
    }

    private fun trimTrailingPunctuation(link: String): String {
        var result = link
        while (result.isNotEmpty()) {
            val last = result.last()
            result = when {
                last in TRAILING_PUNCTUATION -> result.dropLast(1)
                last == ')' && isUnbalanced(result, '(', ')') -> result.dropLast(1)
                last == ']' && isUnbalanced(result, '[', ']') -> result.dropLast(1)
                last == '}' && isUnbalanced(result, '{', '}') -> result.dropLast(1)
                else -> return result
            }
        }
        return result
    }

    private fun isUnbalanced(text: String, open: Char, close: Char): Boolean {
        return text.count { it == close } > text.count { it == open }
    }
}
