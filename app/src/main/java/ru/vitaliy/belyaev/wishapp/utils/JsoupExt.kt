package ru.vitaliy.belyaev.wishapp.utils

import org.jsoup.nodes.Document

fun Document.getLinkTitle(): String = getMetaPropertyContent("meta[property=og:title]")

fun Document.getLinkDescription(): String = getMetaPropertyContent("meta[property=og:description]")

fun Document.getLinkImage(): String = getMetaPropertyContent("meta[property=og:image]")

private fun Document.getMetaPropertyContent(cssQuery: String): String {
    return select(cssQuery).first()?.attr("content") ?: ""
}