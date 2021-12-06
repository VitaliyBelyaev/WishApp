package ru.vitaliy.belyaev.wishapp.navigation

const val ARG_WISH_ID = "wishId"

object MainRoute {
    const val VALUE = "main"
}

object WishDetailedRoute {
    const val VALUE = "wish_detailed"
}

object WishDetailedRouteWithArgs {
    const val VALUE = "${WishDetailedRoute.VALUE}/{$ARG_WISH_ID}"

    fun build(wishId: String): String {
        return "${WishDetailedRoute.VALUE}/$wishId"
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

object WishTagsRoute {
    const val VALUE = "wish_tags/{$ARG_WISH_ID}"

    fun build(wishId: String): String {
        return "wish_tags/$wishId"
    }
}