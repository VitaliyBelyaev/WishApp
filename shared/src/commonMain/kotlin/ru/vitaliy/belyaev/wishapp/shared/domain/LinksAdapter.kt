package ru.vitaliy.belyaev.wishapp.shared.domain

object LinksAdapter {

    private const val LINKS_SEPARATOR = "|"
    private const val ENCODED_SEPARATOR = "%7C"

    /**
     * `|` внутри адреса разрезал бы ссылку при чтении. В адресе он равнозначен `%7C`,
     * поэтому перед сохранением заменяем его.
     */
    fun sanitizeLink(link: String): String = link.replace(LINKS_SEPARATOR, ENCODED_SEPARATOR)

    fun addLinkAndGetAccumulatedString(link: String, currentLinks: List<String>): String {
        val newList = currentLinks.toMutableList().apply {
            add(sanitizeLink(link))
        }
        return newList.joinToString(separator = LINKS_SEPARATOR)
    }

    fun addLinksAndGetAccumulatedString(links: List<String>, currentLinks: List<String>): String {
        return (currentLinks + links.map(::sanitizeLink)).joinToString(separator = LINKS_SEPARATOR)
    }

    fun removeLinkAndGetAccumulatedString(link: String, currentLinks: List<String>): String {
        val newList = currentLinks.filter { it != link }
        return newList.joinToString(separator = LINKS_SEPARATOR)
    }

    fun getLinksListFromString(linksString: String): List<String> {
        return if (linksString.isBlank()) {
            emptyList()
        } else {
            linksString.split(LINKS_SEPARATOR)
        }
    }
}