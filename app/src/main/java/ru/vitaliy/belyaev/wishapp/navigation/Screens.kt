package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import ru.vitaliy.belyaev.wishapp.Main
import ru.vitaliy.belyaev.wishapp.ui.wishdetailed.WishDetailed


object MainScreen {
    const val ROUTE = "main"
}

object WishDetailedScreen {
    const val ROUTE_BASE = "wish_detailed"
    const val ARG_WISH_ID = "wishId"
    const val ROUTE = "$ROUTE_BASE/{$ARG_WISH_ID}"
}