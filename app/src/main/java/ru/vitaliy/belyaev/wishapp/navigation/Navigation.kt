package ru.vitaliy.belyaev.wishapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.vitaliy.belyaev.wishapp.ui.screens.main.MainScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsScreen
import ru.vitaliy.belyaev.wishapp.ui.screens.wishdetailed.WishDetailedScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainRoute.VALUE) {
        composable(MainRoute.VALUE) {
            MainScreen(
                onWishClicked = { navController.navigate(WishDetailedRouteWithArgs.build(it.id)) },
                onAddWishClicked = { navController.navigate(WishDetailedRoute.VALUE) },
                onSettingIconClicked = { navController.navigate(SettingsRoute.VALUE) }
            )
        }
        composable(WishDetailedRoute.VALUE) {
            WishDetailedScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(WishDetailedRouteWithArgs.VALUE) {
            WishDetailedScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(SettingsRoute.VALUE) {
            SettingsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}