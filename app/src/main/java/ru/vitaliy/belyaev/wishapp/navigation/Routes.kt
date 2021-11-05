package ru.vitaliy.belyaev.wishapp.navigation

object MainRoute {
    const val VALUE = "main"
}

object WishDetailedRoute {
    private const val ROUTE_BASE = "wish_detailed"
    const val ARG_WISH_ID = "wishId"
    const val VALUE = "$ROUTE_BASE/{$ARG_WISH_ID}"

    fun build(wishId: String): String {
        return "$ROUTE_BASE/$wishId"
    }
}

object ModifyWishRoute {
    const val VALUE = "modify_wish"
}

object ModifyWishRouteWithArgs {
    const val ARG_WISH_ID = "wishId"
    const val VALUE = "${ModifyWishRoute.VALUE}/{$ARG_WISH_ID}"

    fun build(wishId: String): String = "${ModifyWishRoute.VALUE}/$wishId"
}

object SettingsRoute {
    const val VALUE = "settings"
}