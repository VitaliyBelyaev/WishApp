package ru.vitaliy.belyaev.wishapp.navigation


object MainScreen {
    const val ROUTE = "main"
}

object WishDetailedScreen {
    const val ROUTE_BASE = "wish_detailed"
    const val ARG_WISH_ID = "wishId"
    const val ROUTE = "$ROUTE_BASE/{$ARG_WISH_ID}"

    fun route(wishId: String): String {
        return "$ROUTE_BASE/$wishId"
    }
}