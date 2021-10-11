package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.vitaliy.belyaev.wishapp.Main
import ru.vitaliy.belyaev.wishapp.ui.wishdetailed.WishDetailed

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainScreen.ROUTE) {
        composable(MainScreen.ROUTE) { Main(navController) }
        composable(WishDetailedScreen.ROUTE) { navBackStackEntry ->
            WishDetailed(
                navController,
                navBackStackEntry.arguments?.getString(WishDetailedScreen.ARG_WISH_ID) ?: ""
            )
        }

    }
}