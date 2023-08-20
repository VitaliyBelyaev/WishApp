package ru.vitaliy.belyaev.wishapp.navigation

const val ARG_WISH_ID = "wishId"
const val ARG_WISH_LINK = "wishLink"

object MainRoute {
    const val VALUE = "main"
}

object WishDetailedRoute {

    const val VALUE = "wish_detailed?$ARG_WISH_ID={$ARG_WISH_ID}&$ARG_WISH_LINK={$ARG_WISH_LINK}"

    fun buildRoute(
        wishId: String? = null,
        wishLink: String? = null
    ): String {
        return StringBuilder().apply {
            append("wish_detailed")
            var separator = "?"

            if (!wishId.isNullOrBlank()) {
                append("$separator$ARG_WISH_ID=$wishId")
                separator = "&"
            }

            if (!wishLink.isNullOrBlank()) {
                append("$separator$ARG_WISH_LINK=$wishLink")
            }
        }.toString()
    }
}

object SettingsRoute {
    const val VALUE = "settings"
}

object AboutAppRoute {
    const val VALUE = "about_app"
}

object PrivacyPolicyRoute {
    const val VALUE = "privacy_policy"
}

object EditTagRoute {
    const val VALUE = "edit_tag"
}

object WishTagsRoute {
    const val VALUE = "wish_tags/{$ARG_WISH_ID}"

    fun build(wishId: String): String {
        return "wish_tags/$wishId"
    }
}