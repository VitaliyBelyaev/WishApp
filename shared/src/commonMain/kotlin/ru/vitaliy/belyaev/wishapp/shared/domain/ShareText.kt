package ru.vitaliy.belyaev.wishapp.shared.domain

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

/**
 * Текст «Поделиться» нового вида. Заголовок говорит, что отправлено, ссылки без рекламных меток,
 * у хотелки без названия нет пустого пункта.
 *
 * Два формата, выбирает [COMPACT]:
 * - компактный: пункты через одну пустую строку, комментарий и ссылки сразу под названием,
 *   ссылки без `https://` и `www.`, в конце подпись [footer];
 * - прежний: пустые строки внутри пункта и черта между пунктами, подписи нет.
 *
 * Строки берёт платформа: [header] вроде «Вот мой список хотелок:», [photoOnlyLabel] — «Фото»,
 * [footer] — «Сделано в Хотелках».
 */
object ShareText {

    /** Переключатель формата: `true` — компактный, `false` — прежний, с чертами. */
    const val COMPACT = true

    private const val DIVIDER = "_____________"

    fun listText(
        header: String,
        wishes: List<WishEntity>,
        photoOnlyLabel: String,
        footer: String,
        compact: Boolean = COMPACT,
    ): String = buildString {
        append(header)
        append("\n\n")
        wishes.forEachIndexed { index, wish ->
            val item = ShareItem.of(wish, photoOnlyLabel)
            if (compact) {
                if (index > 0) append("\n")
                appendCompact(item, number = index + 1)
            } else {
                appendSpacious(item, number = index + 1)
                if (index != wishes.lastIndex) {
                    append(DIVIDER)
                    append("\n\n")
                }
            }
        }
        if (compact) appendFooter(footer)
    }.trimEnd()

    /** Одна хотелка из её «⋮»: заголовок и пункт без номера. */
    fun singleWishText(
        header: String,
        wish: WishEntity,
        photoOnlyLabel: String,
        footer: String,
        compact: Boolean = COMPACT,
    ): String = buildString {
        append(header)
        append("\n\n")
        val item = ShareItem.of(wish, photoOnlyLabel)
        if (compact) {
            appendCompact(item, number = null)
            appendFooter(footer)
        } else {
            appendSpacious(item, number = null)
        }
    }.trimEnd()

    private fun StringBuilder.appendCompact(item: ShareItem, number: Int?) {
        if (number != null) append("$number. ")
        append(item.title)
        append("\n")
        // Пустая строка внутри комментария выглядела бы как граница пунктов
        item.comment.lineSequence().filter { it.isNotBlank() }.forEach { line ->
            append(line.trimEnd())
            append("\n")
        }
        item.links.forEach { link ->
            append(shortLink(link))
            append("\n")
        }
    }

    private fun StringBuilder.appendFooter(footer: String) {
        if (footer.isEmpty()) return
        append("\n")
        append(footer)
    }

    private fun StringBuilder.appendSpacious(item: ShareItem, number: Int?) {
        if (number != null) append("$number. ")
        append(item.title)
        append("\n")
        if (item.comment.isNotEmpty()) {
            append("\n")
            append(item.comment)
            append("\n")
        }
        item.links.forEach { link ->
            append("\n")
            append(link)
            append("\n")
        }
    }

    /**
     * Ссылка без `https://`, `http://` и `www.`: мессенджеры и так делают её нажимаемой.
     * Если после схемы не домен с точкой, ссылка остаётся как была.
     */
    internal fun shortLink(link: String): String {
        val scheme = listOf("https://", "http://").firstOrNull { link.startsWith(it, ignoreCase = true) }
            ?: return link
        val rest = link.substring(scheme.length)
        val short = if (rest.startsWith("www.", ignoreCase = true)) rest.substring("www.".length) else rest
        val host = short.substringBefore('/').substringBefore('?').substringBefore('#')
        return if ('.' in host) short else link
    }
}

/**
 * Пункт списка для текста и PDF. Если названия нет, его заменяет то, что видно на карточке:
 * первая строка комментария, затем домен ссылки, а у хотелки из одних фото — [photoOnlyLabel].
 * Строка, ставшая названием, в комментарии не повторяется. Ссылки уже без рекламных меток.
 */
class ShareItem(
    val title: String,
    val comment: String,
    val links: List<String>,
) {
    companion object {
        fun of(wish: WishEntity, photoOnlyLabel: String): ShareItem {
            val links = wish.links.map { ShareLinkCleaner.clean(it.trim()) }.filter { it.isNotEmpty() }
            val comment = wish.comment.trim()
            val title = wish.title.trim()
            return when {
                title.isNotEmpty() -> ShareItem(title, comment, links)
                comment.isNotEmpty() -> ShareItem(
                    title = comment.lineSequence().first().trim(),
                    comment = comment.substringAfter('\n', missingDelimiterValue = "").trim(),
                    links = links,
                )
                links.isNotEmpty() -> ShareItem(linkHost(links.first()), "", links)
                else -> ShareItem(photoOnlyLabel, "", links)
            }
        }
    }
}

/**
 * Убирает рекламные метки из ссылки при отправке: `utm_*`, `gclid`, `fbclid`, `yclid`.
 * Остальные параметры не трогает, от них может зависеть страница. В хотелке ссылка остаётся как есть.
 */
object ShareLinkCleaner {

    private val TRACKING = setOf("gclid", "fbclid", "yclid")

    fun clean(link: String): String {
        val queryStart = link.indexOf('?')
        if (queryStart < 0) return link
        val fragmentStart = link.indexOf('#', startIndex = queryStart).takeIf { it >= 0 } ?: link.length
        val base = link.substring(0, queryStart)
        val fragment = link.substring(fragmentStart)
        val kept = link.substring(queryStart + 1, fragmentStart)
            .split('&')
            .filter { it.isNotEmpty() && !isTracking(it.substringBefore('=')) }
        return if (kept.isEmpty()) base + fragment else base + "?" + kept.joinToString("&") + fragment
    }

    private fun isTracking(name: String): Boolean {
        val key = name.lowercase()
        return key.startsWith("utm_") || key in TRACKING
    }
}

/** Домен ссылки без схемы, `www.`, порта и пути: `https://www.ozon.ru/t/abc` → `ozon.ru`. */
fun linkHost(link: String): String {
    return link.trim()
        .substringAfter("://")
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('@')
        .substringBefore(':')
        .removePrefix("www.")
        .lowercase()
}
