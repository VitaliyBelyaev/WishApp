package ru.vitaliy.belyaev.wishapp.shared.domain

object LinksAdapter {

    private const val LINKS_SEPARATOR = "|"

    fun addLinkAndGetAccumulatedString(link: String, currentLinks: List<String>): String {
        val newList = currentLinks.toMutableList().apply {
            add(link)
        }
        return newList.joinToString(separator = LINKS_SEPARATOR)
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