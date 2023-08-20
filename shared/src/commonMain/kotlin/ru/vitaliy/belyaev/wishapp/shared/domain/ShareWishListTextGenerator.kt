package ru.vitaliy.belyaev.wishapp.shared.domain

import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

object ShareWishListTextGenerator {

    fun generateFormattedWishListText(title: String, wishes: List<WishEntity>): String {
        val builder = StringBuilder().apply {
            append(title)
            append("\n\n")
        }

        wishes.forEachIndexed { index, wish ->
            val number = index + 1
            builder.append("$number. ${wish.title}\n")
            if (wish.comment.isNotBlank()) {
                builder.append("\n")
                builder.append("${wish.comment}\n")
            }
            if (wish.links.isNotEmpty()) {
                for (link in wish.links) {
                    builder.append("\n")
                    builder.append("${link}\n")
                }
            }
            if (index != wishes.lastIndex) {
                builder.append("_____________")
                builder.append("\n\n")
            }
        }
        return builder.toString()
    }
}